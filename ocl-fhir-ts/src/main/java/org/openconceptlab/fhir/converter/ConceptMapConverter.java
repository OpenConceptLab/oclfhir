package org.openconceptlab.fhir.converter;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.PublicationStatus;
import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.Mapping;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.model.UserProfile;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

@Component
public class ConceptMapConverter extends BaseConverter {

    private static final Log log = LogFactory.getLog(ConceptMapConverter.class);
    private static final String updateMappingSql = "update mappings set version = ?, mnemonic = ?, versioned_object_id = ?, uri = ? where id = ?";
    private static final String insertMappingsSources = "insert into mappings_sources (mapping_id,source_id) values (?,?) on conflict do nothing";

    public ConceptMapConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                               UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                               AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                               OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
                               MappingRepository mappingRepository) {
        super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
                userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, mappingRepository);
    }

    public List<ConceptMap> convertToConceptMap(List<Source> sources, boolean includeMappings, int page, StringBuilder hasNext) {
        List<ConceptMap> conceptMaps = new ArrayList<>();
        if (!includeMappings) {
            int offset = page * 10;
            int count = 10;
            if (page == 0) {
                if (sources.size() > count) hasNext.append(True);
            } else if (page < sources.size()/count) {
                hasNext.append(True);
            }
            sources = paginate(sources, offset, count);
        }
        sources.forEach(source -> {
            // convert to base concept map
            ConceptMap conceptMap = toConceptMap(source);
            if (includeMappings) {
                // populate mappings
                addMappingsToConceptMap(conceptMap, source.getId(), page, hasNext);
            }
            conceptMaps.add(conceptMap);
        });
        return conceptMaps;
    }

    private ConceptMap toConceptMap(final Source source) {
        ConceptMap conceptMap = new ConceptMap();
        // Url
        if(StringUtils.isNotBlank(source.getCanonicalUrl())) {
            conceptMap.setUrl(source.getCanonicalUrl());
        }
        // id
        conceptMap.setId(source.getMnemonic());
        // version
        conceptMap.setVersion(source.getVersion());
        // name
        conceptMap.setName(source.getName());
        // title
        if(StringUtils.isNotBlank(source.getFullName())) {
            conceptMap.setTitle(source.getFullName());
        }
        // status
        addStatus(conceptMap, source.getRetired() != null ? source.getRetired() : False,
                source.getReleased() != null ? source.getReleased() : False);
        // description
        if(StringUtils.isNotBlank(source.getDescription()))
            conceptMap.setDescription(source.getDescription());
        // publisher
        if (isValid(source.getPublisher()))
            conceptMap.setPublisher(source.getPublisher());
        // identifier, contact, jurisdiction
        addJsonFields(conceptMap, isValid(source.getIdentifier()) && !EMPTY_JSON.equals(source.getIdentifier()) ?
                        source.getIdentifier() : EMPTY, source.getContact(), source.getJurisdiction(), source.getText(),
                source.getMeta());
        // add lastUpdated date is not populated in conceptMap.meta.lastUpdated
        if (conceptMap.getMeta().getLastUpdated() == null) {
            conceptMap.getMeta().setLastUpdated(source.getUpdatedAt());
        }
        // add accession identifier if not present
        if (conceptMap.getIdentifier().isEmpty() || !conceptMap.getIdentifier().getType().hasCoding(ACSN_SYSTEM, ACSN)) {
            getIdentifier(source.getUri().replace(SOURCES, CONCEPTMAP))
                    .ifPresent(conceptMap::setIdentifier);
        }
        // purpose
        if (isValid(source.getPurpose()))
            conceptMap.setPurpose(source.getPurpose());
        // copyright
        if (isValid(source.getCopyright()))
            conceptMap.setCopyright(source.getCopyright());
        // revision date
        if (source.getRevisionDate() != null)
            conceptMap.setDate(source.getRevisionDate());
        // experimental
        if (source.isExperimental() != null) conceptMap.setExperimental(source.isExperimental());
        return conceptMap;
    }

    private void addMappingsToConceptMap(final ConceptMap conceptMap, final Long sourceId, int page, StringBuilder hasNext) {
        Page<Mapping> mappings = mappingRepository.findMappings(sourceId, PageRequest.of(page, 100));
        if (page < mappings.getTotalPages() - 1) hasNext.append(True);
        if (!mappings.getContent().isEmpty()) addMappings(conceptMap, mappings.getContent());
    }

    private void addMappings(final ConceptMap conceptMap, final List<Mapping> mappings) {
        // creates ConceptMapGroups by grouping source/sourceversion/target/targetversion
        Map<Object, List<ConceptMapGroup>> map = mappings.stream().map(ConceptMapGroup::new)
        .filter(m -> isValid(m.getFromSystemUrl()) && isValid(m.getFromCode()) &&
                isValid(m.getToSystemUrl()) && isValid(m.getToCode()) &&
                isValid(m.getEquivalence()))
        .collect(Collectors.groupingBy(m -> m.getFromSystemUrl() + m.getFromSystemVersion() + m.getToSystemUrl() + m.getToSystemVersion()));
        map.forEach((k,groups) -> {
            Optional<ConceptMap.ConceptMapGroupComponent> component = toConceptMapGroupComponent(groups);
            component.ifPresent(c -> conceptMap.getGroup().add(c));
        });
    }

    private Optional<ConceptMap.ConceptMapGroupComponent> toConceptMapGroupComponent(List<ConceptMapGroup> conceptMapGroups) {
        if (conceptMapGroups != null && !conceptMapGroups.isEmpty()) {
            // all conceptMapGroups are for a single parent group
            ConceptMap.ConceptMapGroupComponent component = new ConceptMap.ConceptMapGroupComponent();
            conceptMapGroups.stream().findAny().ifPresent(conceptMapGroup -> {
                component.setSource(conceptMapGroup.getFromSystemUrl());
                component.setTarget(conceptMapGroup.getToSystemUrl());
                if (isValid(conceptMapGroup.getFromSystemVersion()))
                    component.setSourceVersion(conceptMapGroup.getFromSystemVersion());
                if (isValid(conceptMapGroup.getToSystemVersion()))
                    component.setTargetVersion(conceptMapGroup.getToSystemVersion());
            });
            // 1 source code -> * target codes
            // group by source code
            Map<Object, List<ConceptMapGroup>> codeMap =
                    new TreeMap<>(conceptMapGroups.stream().collect(Collectors.groupingBy(ConceptMapGroup::getFromCode)));
            codeMap.forEach((code, groups) -> {
                // one source code
                ConceptMap.SourceElementComponent sourceElementComponent = new ConceptMap.SourceElementComponent();
                // add source code and display
                groups.stream().findAny().ifPresent(group -> {
                    sourceElementComponent.setCode(group.getFromCode());
                    if (isValid(group.getFromDisplay()))
                        sourceElementComponent.setDisplay(group.getFromDisplay());
                });
                // add multiple target codes
                groups.parallelStream().forEach(group -> {
                    ConceptMap.TargetElementComponent targetElementComponent = new ConceptMap.TargetElementComponent();
                    targetElementComponent.setCode(group.getToCode());
                    if (isValid(group.getToDisplay()))
                        targetElementComponent.setDisplay(group.getToDisplay());
                    Optional<Enumerations.ConceptMapEquivalence> conceptMapEquivalence = Arrays.stream(Enumerations.ConceptMapEquivalence.values())
                            .filter(e -> e.toCode().equals(group.getEquivalence())).findAny();
                    if (conceptMapEquivalence.isPresent()) {
                        targetElementComponent.setEquivalence(conceptMapEquivalence.get());
                    } else {
                        targetElementComponent.getExtensionFirstRep().setUrl("http://fhir.openconceptlab.org/ConceptMap/equivalence")
                                .setValue(newStringType(group.getEquivalence()));
                    }

                    sourceElementComponent.getTarget().add(targetElementComponent);
                });
                component.getElement().add(sourceElementComponent);
            });
            return Optional.of(component);
        }
        return Optional.empty();
    }

    static class ConceptMapGroup {

        private String fromSystemUrl;
        private String fromSystemVersion;
        private String fromCode;
        private String fromDisplay;
        private String toSystemUrl;
        private String toSystemVersion;
        private String toCode;
        private String toDisplay;
        private String equivalence;

        public ConceptMapGroup(final Mapping mapping) {
            String fromSourceUrl = mapping.getFromSourceUrl();
            if (!isValid(fromSourceUrl)) {
                Source source = mapping.getFromSource();
                if (source != null)
                    this.fromSystemUrl = source.getCanonicalUrl();
            } else {
                this.fromSystemUrl = fromSourceUrl;
            }

            String toSourceUrl = mapping.getToSourceUrl();
            if (!isValid(toSourceUrl)) {
                Source source = mapping.getToSource();
                if (source != null)
                    this.toSystemUrl = source.getCanonicalUrl();
            } else {
                this.toSystemUrl = toSourceUrl;
            }

            String fromConceptCode = mapping.getFromConceptCode();
            if (!isValid(fromConceptCode)) {
                Concept fromConcept = mapping.getFromConcept();
                if (fromConcept != null)
                    this.fromCode = fromConcept.getMnemonic();
            } else {
                this.fromCode = fromConceptCode;
            }

            String toConceptCode = mapping.getToConceptCode();
            if (!isValid(toConceptCode)) {
                Concept toConcept = mapping.getToConcept();
                if (toConcept != null)
                    this.toCode = toConcept.getMnemonic();
            } else {
                this.toCode = toConceptCode;
            }

            this.fromDisplay = mapping.getFromConceptName();
            this.toDisplay = mapping.getToConceptName();
            this.equivalence = mapping.getMapType();
            this.fromSystemVersion = mapping.getFromSourceVersion();
            this.toSystemVersion = mapping.getToSourceVersion();
        }

        public String getFromSystemUrl() {
            return fromSystemUrl;
        }

        public String getToSystemUrl() {
            return toSystemUrl;
        }

        public String getFromCode() {
            return fromCode;
        }

        public String getFromDisplay() {
            return fromDisplay;
        }

        public String getToCode() {
            return toCode;
        }

        public String getToDisplay() {
            return toDisplay;
        }

        public String getEquivalence() {
            return equivalence;
        }

        public String getFromSystemVersion() {
            return fromSystemVersion;
        }

        public String getToSystemVersion() {
            return toSystemVersion;
        }
    }

    public Parameters translate(Source source, UriType sourceSystem, StringType sourceVersion, CodeType sourceCode,
                                UriType targetSystem, List<String> access) {
        Parameters parameters = new Parameters();
        parameters.addParameter(RESULT, False);

        String fromSourceUrl = sourceSystem.getValue();
        // let's get local url if canonical url is given
        String localFromSourceUri = null;
        if (sourceSystem.getValue().startsWith("http")) {
            Source fromSource = null;
            if (!isValid(sourceVersion)) {
                // get most recent released version
                fromSource = oclFhirUtil.getLatestSourceByUrl(newStringType(sourceSystem.getValue()), access);
            } else {
                // get a given version
                fromSource = sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(sourceSystem.getValue(), sourceVersion.getValue(), access);
            }
            localFromSourceUri = fromSource != null ? toLocalUri(fromSource) : EMPTY;
        } else if (formatExpression(sourceSystem.getValue()).matches(OWNER_REGEX)) {
            localFromSourceUri = formatExpression(sourceSystem.getValue());
        }

        String toSourceUrl = null;
        String localToSourceUri = null;
        if (isValid(targetSystem)) {
            toSourceUrl = targetSystem.getValue();
            if (targetSystem.getValue().startsWith("http")) {
                Source toSource = oclFhirUtil.getLatestSourceByUrl(newStringType(targetSystem.getValue()), access);
                localToSourceUri = toSource != null ? toLocalUri(toSource) : EMPTY;
            } else if (formatExpression(targetSystem.getValue()).matches(OWNER_REGEX)) {
                localToSourceUri = formatExpression(targetSystem.getValue());
            }
        }
        final String finalToSourceUrl = toSourceUrl;
        final String finalLocalToSourceUri = localToSourceUri;

        // Dynamic search based on Local uri as well as canonical url
        // Search in the ConceptMap (source.Id) repository
        // Match on:
        // 1. Local fromSource uri OR given fromSource canonical url
        // 2. given concept code
        // 3. Local toSource uri OR given toSource canonical url
        // 4. fromSource version if given else any/empty version
        List<Mapping> matches = mappingRepository.findMappingsForCode(source.getId(), localFromSourceUri, fromSourceUrl, getCode(sourceCode))
                .stream()
                .filter(f -> !isValid(sourceVersion) || sourceVersion.getValue().equals(f.getFromSourceVersion()))
                .filter(f -> isValid(f.getToSourceUrl()))
                .filter(f -> !isValid(targetSystem) || f.getToSourceUrl().equals(finalToSourceUrl) || f.getToSourceUrl().equals(finalLocalToSourceUri))
                .collect(Collectors.toList());
        if (!matches.isEmpty()) {
            parameters.setParameter(RESULT, True);
            parameters.setParameter(MESSAGE, "Matches found!");
            matches.forEach(match -> {
                parameters.addParameter().setName("match").setPart(getMatchParameter(
                        match.getToSourceUrl(), match.getToSourceVersion(), match.getToConceptCode(), match.getToConceptName(), match.getMapType()
                ));
            });
        }
        return parameters;
    }

    private String toLocalUri(Source source) {
        if (source == null) return EMPTY;
        String ownerType = source.getOrganization() != null ? ORGS : USERS;
        String owner = source.getOrganization() != null ? source.getOrganization().getMnemonic()
                : source.getUserId().getUsername();
        String sourceId = source.getMnemonic();
        return FS + ownerType + FS + owner + FS + SOURCES + FS + sourceId + FS;
    }

    private List<Parameters.ParametersParameterComponent> getMatchParameter(String toSystemUrl, String version, String toCode,
                                                                            String toName, String type) {
        List<Parameters.ParametersParameterComponent> componentList = new ArrayList<>();
        if (isValid(type))
            componentList.add(getParameter("equivalence", type));
        Coding coding = new Coding();
        coding.setSystem(toSystemUrl).setVersion(version).setCode(toCode).setDisplay(toName);
        componentList.add(getParameter("concept", coding));
        return componentList;
    }

    public void createConceptMap(ConceptMap conceptMap, String accessionId, String authToken) {
        // validate and authenticate
        OclEntity oclEntity = new OclEntity(conceptMap, accessionId, authToken, true);
        UserProfile user = oclEntity.getUserProfile();
        // base source
        Source source = toBaseSource(conceptMap, user, oclEntity.getAccessionId());
        // add parent and access
        addParent(source, oclEntity.getOwner());
        // add identifier, contact and jurisdiction
        addJsonStrings(conceptMap, source);

        // add mappings
        List<Mapping> mappings = getMappings(conceptMap, source, user);

        // save given version
        saveSource(source, mappings);
        // save HEAD version
        source.setId(null);
        source.setVersion(HEAD);
        source.setIsLatestVersion(false);
        source.setReleased(false);
        source.setUri(removeVersion(source.getUri()));
        saveSource(source, mappings);

        // populate index
        oclFhirUtil.populateIndex(getToken(), SOURCES, MAPPINGS);
    }

    @Transactional
    protected void saveSource(Source source, List<Mapping> mappings) {
        // save source
        sourceRepository.saveAndFlush(source);
        log.info("saved source - " + source.getMnemonic());

        // save mappings
        saveMappings(source, mappings);
    }

    private List<Mapping> toMappings(List<ConceptMap.ConceptMapGroupComponent> components) {
        List<Mapping> mappings = new CopyOnWriteArrayList<>();
        for (ConceptMap.ConceptMapGroupComponent component : components) {
            String sourceUrl = component.getSource();
            String sourceVersion = sourceUrl.split("\\|").length == 2 ?
                    sourceUrl.split("\\|")[1] : component.getSourceVersion();
            if (!isValid(sourceUrl))
                throw new InvalidRequestException("ConceptMap.group.source can not be empty.");
            Long sourceId = null;
            List<ConceptIdMnemonic> sourceConcepts = new ArrayList<>();
            try {
                List<Source> sourceSrc = oclFhirUtil.getSourceByUrl(newStringType(sourceUrl), newStringType(sourceVersion),
                        publicAccess);
                sourceId = sourceSrc.get(0).getId();
                sourceConcepts.addAll(conceptRepository.findConceptIds(sourceId));
            } catch (ResourceNotFoundException e) {
                // skip if source is not hosted in OCL
            }

            String targetUrl = component.getTarget();
            String targetVersion = targetUrl.split("\\|").length == 2 ?
                    targetUrl.split("\\|")[1] : component.getTargetVersion();
            if (!isValid(targetUrl))
                throw new InvalidRequestException("ConceptMap.group.target can not be empty.");
            Long targetId = null;
            List<ConceptIdMnemonic> targetConcepts = new ArrayList<>();
            try {
                List<Source> targetSrc = oclFhirUtil.getSourceByUrl(newStringType(targetUrl), newStringType(targetVersion),
                        publicAccess);
                targetId = targetSrc.get(0).getId();
                targetConcepts.addAll(conceptRepository.findConceptIds(targetId));
            } catch (ResourceNotFoundException e) {
                // skip if source is not hosted in OCL
            }

            List<ConceptMap.SourceElementComponent> elements = component.getElement();
            for (ConceptMap.SourceElementComponent element : elements) {
                String sourceCode = element.getCode();
                String sourceDisplay = element.getDisplay();
                Long sourceCodeId = null;
                if (!isValid(sourceCode))
                    throw new InvalidRequestException("ConceptMap.group.element.code can not be empty.");
                if (sourceId != null) {
                    Optional<ConceptIdMnemonic> sourceConcept = getConcept(sourceConcepts, sourceCode);
                    if (sourceConcept.isPresent()) sourceCodeId = sourceConcept.get().getId();
                }

                List<ConceptMap.TargetElementComponent> targetElements = element.getTarget();
                for (ConceptMap.TargetElementComponent targetElement : targetElements) {
                    String targetCode = targetElement.getCode();
                    String targetDisplay = targetElement.getDisplay();
                    String equivalence = targetElement.getEquivalence() != null ? targetElement.getEquivalence().toCode()
                            : targetElement.getExtensionString("http://fhir.openconceptlab.org/ConceptMap/equivalence");
                    if (!isValid(targetCode))
                        throw new InvalidRequestException("ConceptMap.group.element.target.code can not be empty.");
                    if (!isValid(equivalence))
                        throw new InvalidRequestException("ConceptMap.group.element.target.equivalence can not be empty.");
                    Long targetCodeId = null;
                    if (targetId != null) {
                        Optional<ConceptIdMnemonic> targetConcept = getConcept(targetConcepts, targetCode);
                        if (targetConcept.isPresent()) targetCodeId = targetConcept.get().getId();
                    }
                    mappings.add(toMapping(sourceUrl, sourceVersion, sourceCode, sourceDisplay, targetUrl, targetVersion, targetCode,
                            targetDisplay, equivalence, sourceId, sourceCodeId, targetId, targetCodeId));
                }
            }
        }
        return mappings;
    }

    private Optional<ConceptIdMnemonic> getConcept(List<ConceptIdMnemonic> concepts, String code) {
        return concepts.parallelStream().filter(c -> c.getMnemonic().equals(code)).findAny();
    }

    private Mapping toMapping(String fromSourceUrl, String fromSourceVersion, String fromConcept, String fromDisplay,
                              String toSourceUrl, String toSourceVersion, String toCode, String toDisplay,
                              String equivalence, Long fromSourceId, Long fromSourceCodeId, Long toSourceId,
                              Long toSourceCodeId) {
        final Mapping mapping = new Mapping();
        mapping.setFromSourceUrl(fromSourceUrl);
        if (isValid(fromSourceVersion)) mapping.setFromSourceVersion(fromSourceVersion);
        mapping.setFromConceptCode(fromConcept);
        if (isValid(fromDisplay)) mapping.setFromConceptName(fromDisplay);
        mapping.setToSourceUrl(toSourceUrl);
        if (isValid(toSourceVersion)) mapping.setToSourceVersion(toSourceVersion);
        mapping.setToConceptCode(toCode);
        if (isValid(toDisplay)) mapping.setToConceptName(toDisplay);
        mapping.setMapType(equivalence);
        if (fromSourceId != null) mapping.setFromSource(getSource(fromSourceId));
        if (fromSourceCodeId != null) mapping.setFromConcept(getConcept(fromSourceCodeId));
        if (toSourceId != null) mapping.setToSource(getSource(toSourceId));
        if (toSourceCodeId != null) mapping.setToConcept(getConcept(toSourceCodeId));
        return mapping;
    }

    private Source getSource(Long id) {
        final Source source = new Source();
        source.setId(id);
        return source;
    }

    private Concept getConcept(Long id) {
        final Concept concept = new Concept();
        concept.setId(id);
        return concept;
    }

    private void populateBaseMappingField(List<Mapping> mappings, Source source, UserProfile user) {
        //(mnemonic, version, parent_id) is unique in db
        mappings.forEach(c -> {
            c.setParent(source);
            c.setPublicAccess(source.getPublicAccess());
            c.setIsLatestVersion(True);
            c.setReleased(false);
            c.setRetired(false);
            c.setIsActive(true);
            c.setIsLatestVersion(true);
            c.setCreatedBy(user);
            c.setUpdatedBy(user);
            c.setVersionedObject(c);
            // will be replaced with generated id
            c.setVersion(UUID.randomUUID().toString());
            // will be replaced with generated id
            c.setMnemonic(UUID.randomUUID().toString());
            c.setExtras(EMPTY_JSON);
        });
    }

    private Map<String, Object> toMap(Mapping obj) {
        Map<String, Object> map = new HashMap<>();
        map.put(FROM_SOURCE_URL, obj.getFromSourceUrl());
        map.put(FROM_SOURCE_VERSION, obj.getFromSourceVersion());
        if (obj.getFromSource() != null)
            map.put(FROM_SOURCE_ID, obj.getFromSource().getId());
        map.put(FROM_CONCEPT_CODE, obj.getFromConceptCode());
        map.put(FROM_CONCEPT_NAME, obj.getFromConceptName());
        if (obj.getFromConcept() != null)
            map.put(FROM_CONCEPT_ID, obj.getFromConcept().getId());
        map.put(TO_SOURCE_URL, obj.getToSourceUrl());
        map.put(TO_SOURCE_VERSION, obj.getToSourceVersion());
        if (obj.getToSource() != null)
            map.put(TO_SOURCE_ID, obj.getToSource().getId());
        map.put(TO_CONCEPT_CODE, obj.getToConceptCode());
        map.put(TO_CONCEPT_NAME, obj.getToConceptName());
        if (obj.getToConcept() != null)
            map.put(TO_CONCEPT_ID, obj.getToConcept().getId());
        map.put(MAP_TYPE, obj.getMapType());
        map.put(VERSION, obj.getVersion());
        map.put(MNEMONIC, obj.getMnemonic());
        map.put(PUBLIC_ACCESS, obj.getPublicAccess());
        map.put(IS_ACTIVE, obj.getIsActive());
        map.put(EXTRAS, obj.getExtras());
        map.put(RELEASED, obj.getReleased());
        map.put(RETIRED, obj.getRetired());
        map.put(IS_LATEST_VERSION, obj.getIsLatestVersion());
        if (isValid(obj.getComment()))
            map.put(COMMENT, obj.getComment());
        map.put(CREATED_BY_ID, obj.getCreatedBy().getId());
        map.put(UPDATED_BY_ID, obj.getUpdatedBy().getId());
        map.put(PARENT_ID, obj.getParent().getId());
        map.put(CREATED_AT, obj.getParent().getCreatedAt());
        map.put(UPDATED_AT, obj.getParent().getUpdatedAt());
        return map;
    }

    private void saveMappings(Source source, List<Mapping> mappings) {
        String versionLessSourceUri = getVersionLessSourceUri(source);
        Long sourceId = source.getId();
        List<Integer> mappingIds = new CopyOnWriteArrayList<>();
        // save initial mapping and populate mappingIds
        persistMappings(mappings, mappingIds);
        // update mappings in batch; updates version, uri, mnemonic, version_object_id
        List<List<Integer>> mappingIdBatches = updateMappings(mappingIds, versionLessSourceUri);
        // save mappings sources in batch
        saveMappingsSources(sourceId, mappingIdBatches);
        log.info("saved " + mappingIds.size() + " mappings");
    }

    private void persistMappings(List<Mapping> mappings, List<Integer> mappingIds) {
        List<List<Mapping>> mappingBatches = ListUtils.partition(mappings, 1000);
        int i = 1;
        for (List<Mapping> mb: mappingBatches) {
            log.info("Saving " + mb.size() + " mappings, batch " + i + " of " + mappingBatches.size());
            batchMappings(mb, mappingIds);
            i++;
        }
    }

    private void batchMappings(List<Mapping> mappings, List<Integer> mappingIds) {
        mappings.forEach(m -> {
            Integer mappingId = insert(insertMapping, toMap(m)).intValue();
            mappingIds.add(mappingId);
        });
    }

    private List<List<Integer>> updateMappings(List<Integer> mappingsIds, String versionLessSourceUri) {
        List<List<Integer>> mappingIdBatches = ListUtils.partition(mappingsIds, 1000);
        mappingIdBatches.forEach(b -> batchUpdateMapping(b, versionLessSourceUri));
        return mappingIdBatches;
    }

    private void batchUpdateMapping(List<Integer> mappingsIds, String versionLessSourceUri) {
        this.jdbcTemplate.batchUpdate(updateMappingSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setInt(1, mappingsIds.get(i));
                ps.setInt(2, mappingsIds.get(i));
                ps.setInt(3, mappingsIds.get(i));
                ps.setString(4, versionLessSourceUri + MAPPINGS + FS + mappingsIds.get(i) + FS);
                ps.setInt(5, mappingsIds.get(i));
            }
            public int getBatchSize() {
                return mappingsIds.size();
            }
        });
    }

    private void saveMappingsSources(Long sourceId, List<List<Integer>> mappingIdBatches) {
        // save mappings sources
        mappingIdBatches.forEach(b -> batchUpdateMappingsSources(b, sourceId));
    }

    private void batchUpdateMappingsSources(List<Integer> mappingIds, Long sourceId) {
        this.jdbcTemplate.batchUpdate(insertMappingsSources, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setInt(1, mappingIds.get(i));
                ps.setLong(2, sourceId);
            }
            public int getBatchSize() {
                return mappingIds.size();
            }
        });
    }

    public void updateConceptMap(final ConceptMap conceptMap, final Source source, final String accessionId, final String authToken) {
        // update and save in database
        boolean newMappings = update(conceptMap, source, accessionId, authToken);
        // update indexes
        oclFhirUtil.updateIndex(getToken(), SOURCES, source.getMnemonic());
        if (newMappings) {
            oclFhirUtil.populateIndex(getToken(), MAPPINGS);
        }
    }

    @Transactional
    boolean update(final ConceptMap conceptMap, final Source source, final String accessionId, final String authToken) {
        final OclEntity oclEntity = new OclEntity(conceptMap, accessionId, authToken, false);
        UserProfile user = oclEntity.getUserProfile();
        // update status
        if (conceptMap.getStatus() != null) {
            if (PublicationStatus.DRAFT.toCode().equals(conceptMap.getStatus().toCode()) ||
                    PublicationStatus.UNKNOWN.toCode().equals(conceptMap.getStatus().toCode())) {
                source.setReleased(False);
            } else if (PublicationStatus.ACTIVE.toCode().equals(conceptMap.getStatus().toCode())) {
                source.setReleased(True);
            } else if (PublicationStatus.RETIRED.toCode().equals(conceptMap.getStatus().toCode())) {
                source.setRetired(True);
                source.setReleased(False);
            }
        }
        // update canonical url
        if (isValid(conceptMap.getUrl()))
            source.setCanonicalUrl(conceptMap.getUrl());
        // update language
        if (isValid(conceptMap.getLanguage()))
            source.setDefaultLocale(conceptMap.getLanguage());
        // update name
        if (isValid(conceptMap.getName()))
            source.setName(conceptMap.getName());
        // copyright
        if (isValid(conceptMap.getCopyright()))
            source.setCopyright(conceptMap.getCopyright());
        // description
        if (isValid(conceptMap.getDescription()))
            source.setDescription(conceptMap.getDescription());
        // title
        if (isValid(conceptMap.getTitle()))
            source.setFullName(conceptMap.getTitle());
        // publisher
        if (isValid(conceptMap.getPublisher()))
            source.setPublisher(conceptMap.getPublisher());
        // purpose
        if (isValid(conceptMap.getPurpose()))
            source.setPurpose(conceptMap.getPurpose());
        // revision date
        if (conceptMap.getDate() != null)
            source.setRevisionDate(conceptMap.getDate());
        // updated by
        source.setUpdatedBy(oclEntity.getUserProfile());
        // updated at
        source.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        // we can not allow updating identifiers since ConceptMap can only have one Identifier and
        // it has to be accessionId, and we don't allow updating accessionId.
        conceptMap.setIdentifier(new Identifier());
        // update contact and jurisdiction
        addJsonStrings(conceptMap, source);
        // experimental
        if (conceptMap.getExperimentalElement().getValue() != null)
            source.setExperimental(conceptMap.getExperimentalElement().booleanValue());
        // source_type
        String sourceType = getSourceType(conceptMap);
        if (isValid(sourceType))
            source.setSourceType(sourceType);
        // update base source resource
        sourceRepository.saveAndFlush(source);
        log.info("updated conceptmap - " + source.getMnemonic());

        // save new mappings
        List<Mapping> newMappings = new ArrayList<>();
        List<Mapping> mappings = getMappings(conceptMap, source, user);
        Page<Mapping> allExisting = mappingRepository.findMappings(source.getId(), PageRequest.of(0, Integer.MAX_VALUE));
        if (!allExisting.getContent().isEmpty()) {
            List<String> existing = allExisting.getContent()
                    .parallelStream()
                    .map(this::getFilterStr)
                    .collect(Collectors.toList());
            newMappings.addAll(mappings.stream().filter(m -> !existing.contains(getFilterStr(m))).collect(Collectors.toList()));
        } else {
            newMappings.addAll(mappings);
        }

        if (!newMappings.isEmpty()) {
            saveMappings(source, newMappings);
            return true;
        }
        return false;
    }

    private String getFilterStr(Mapping mapping) {
        return mapping.getFromSourceUrl() + "|" + mapping.getFromSourceVersion() + "|" + mapping.getFromConceptCode() + "|" +
                mapping.getFromSourceUrl() + "|" + mapping.getToSourceVersion() + "|" + mapping.getToConceptCode() + "|" +
                mapping.getMapType();
    }

    private List<Mapping> getMappings(ConceptMap conceptMap, Source source, UserProfile user) {
        // add mappings
        List<Mapping> mappings = toMappings(conceptMap.getGroup());
        populateBaseMappingField(mappings, source, user);

        // validate mappings
        long uniqueMapping = mappings.parallelStream().map(this::getFilterStr).distinct().count();
        if (mappings.size() != uniqueMapping)
            throw new InvalidRequestException("The combination of source_url,source_version," +
                    "source_code,target_url,target_version,target_code,equivalence should be unique.");
        return mappings;
    }

}
