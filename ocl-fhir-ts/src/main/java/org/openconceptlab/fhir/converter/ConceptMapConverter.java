package org.openconceptlab.fhir.converter;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Enumerations;
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

    private String getFromConceptCode(final Mapping mapping) {
        String fromConceptCode = mapping.getFromConceptCode();
        if (!isValid(fromConceptCode)) {
            Concept fromConcept = mapping.getFromConcept();
            if (fromConcept != null)
                return fromConcept.getMnemonic();
        } else {
            return fromConceptCode;
        }
        return EMPTY;
    }

    private String getToConceptCode(final Mapping mapping) {
        String toConceptCode = mapping.getToConceptCode();
        if (!isValid(toConceptCode)) {
            Concept toConcept = mapping.getToConcept();
            if (toConcept != null)
                return toConcept.getMnemonic();
        } else {
            return toConceptCode;
        }
        return EMPTY;
    }

    private String getFromSourceUrl(final Mapping mapping) {
        String fromSourceUrl = mapping.getFromSourceUrl();
        if (!isValid(fromSourceUrl) || !fromSourceUrl.startsWith("http")) {
            // only reason to resolve to canonical url from local url is to make sure compatibility with OCL imported
            // data, since user can create mapping using local url in OCL. However, this should not be an issue when user
            // creates mapping using OCL FHIR service because only canonical url is allowed as source/target system.
            Source source = mapping.getFromSource();
            if (source != null)
                return source.getCanonicalUrl();
        } else {
            return fromSourceUrl;
        }
        return EMPTY;
    }

    private String getToSourceUrl(final Mapping mapping) {
        String toSourceUrl = mapping.getToSourceUrl();
        if (!isValid(toSourceUrl) || !toSourceUrl.startsWith("http")) {
            Source source = mapping.getToSource();
            if (source != null)
                return source.getCanonicalUrl();
        } else {
            return toSourceUrl;
        }
        return EMPTY;
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
            if (!isValid(fromSourceUrl) || !fromSourceUrl.startsWith("http")) {
                // only reason to resolve to canonical url from local url is to make sure compatibility with OCL imported
                // data, since user can create mapping using local url in OCL. However, this should not be an issue when user
                // creates mapping using OCL FHIR service because only canonical url is allowed as source/target system.
                Source source = mapping.getFromSource();
                if (source != null)
                    this.fromSystemUrl = source.getCanonicalUrl();
            } else {
                this.fromSystemUrl = fromSourceUrl;
            }

            String toSourceUrl = mapping.getToSourceUrl();
            if (!isValid(toSourceUrl) || !toSourceUrl.startsWith("http")) {
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

        public void setFromSystemUrl(String fromSystemUrl) {
            this.fromSystemUrl = fromSystemUrl;
        }

        public String getToSystemUrl() {
            return toSystemUrl;
        }

        public void setToSystemUrl(String toSystemUrl) {
            this.toSystemUrl = toSystemUrl;
        }

        public String getFromCode() {
            return fromCode;
        }

        public void setFromCode(String fromCode) {
            this.fromCode = fromCode;
        }

        public String getFromDisplay() {
            return fromDisplay;
        }

        public void setFromDisplay(String fromDisplay) {
            this.fromDisplay = fromDisplay;
        }

        public String getToCode() {
            return toCode;
        }

        public void setToCode(String toCode) {
            this.toCode = toCode;
        }

        public String getToDisplay() {
            return toDisplay;
        }

        public void setToDisplay(String toDisplay) {
            this.toDisplay = toDisplay;
        }

        public String getEquivalence() {
            return equivalence;
        }

        public void setEquivalence(String equivalence) {
            this.equivalence = equivalence;
        }

        public String getFromSystemVersion() {
            return fromSystemVersion;
        }

        public void setFromSystemVersion(String fromSystemVersion) {
            this.fromSystemVersion = fromSystemVersion;
        }

        public String getToSystemVersion() {
            return toSystemVersion;
        }

        public void setToSystemVersion(String toSystemVersion) {
            this.toSystemVersion = toSystemVersion;
        }
    }

}
