package org.openconceptlab.fhir.converter;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.Mapping;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.model.UserProfile;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.isValid;

@Component
public class ConceptMapConverter extends BaseConverter {

    public ConceptMapConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                               UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                               AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                               OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
                               MappingRepository mappingRepository) {
        super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
                userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, mappingRepository);
    }

    public List<ConceptMap> convertToConceptMap(List<Source> sources, boolean includeMappings, int page) {
        List<ConceptMap> conceptMaps = new ArrayList<>();
        if (!includeMappings) {
            int offset = page * 10;
            int count = 10;
            sources = paginate(sources, offset, count);
        }
        sources.forEach(source -> {
            // convert to base concept map
            ConceptMap conceptMap = toConceptMap(source);
            if (includeMappings) {
                // populate mappings
                addMappingsToConceptMap(conceptMap, source.getId(), page);
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
        // identifier
        getIdentifier(source.getUri().replace("sources", "ConceptMap"))
                .ifPresent(conceptMap::setIdentifier);
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
        // override default identifier with database value
        // identifier, contact, jurisdiction
        addJsonFields(conceptMap, isValid(source.getIdentifier()) && !EMPTY_JSON.equals(source.getIdentifier()) ?
                        source.getIdentifier() : EMPTY, source.getContact(), source.getJurisdiction());
        // purpose
        if (isValid(source.getPurpose()))
            conceptMap.setPurpose(source.getPurpose());
        // copyright
        if (isValid(source.getCopyright()))
            conceptMap.setCopyright(source.getCopyright());
        // revision date
        if (source.getRevisionDate() != null)
            conceptMap.setDate(source.getRevisionDate());
        return conceptMap;
    }

    private void addMappingsToConceptMap(final ConceptMap conceptMap, final Long sourceId, int page) {
        List<Mapping> mappings = mappingRepository.findMappings(sourceId, PageRequest.of(page, 100));
        if (mappings != null && !mappings.isEmpty())
            addMappings(conceptMap, mappings);
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
                fromSource = oclFhirUtil.getMostRecentReleasedSourceByUrl(newStringType(sourceSystem.getValue()), access);
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
                Source toSource = oclFhirUtil.getMostRecentReleasedSourceByUrl(newStringType(targetSystem.getValue()), access);
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

}
