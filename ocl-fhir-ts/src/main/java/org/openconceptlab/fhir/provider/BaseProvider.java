package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StringType;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.converter.ConceptMapConverter;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

public class BaseProvider {

    SourceRepository sourceRepository;
    CodeSystemConverter codeSystemConverter;
    CollectionRepository collectionRepository;
    ValueSetConverter valueSetConverter;
    ConceptMapConverter conceptMapConverter;
    OclFhirUtil oclFhirUtil;

    @Autowired
    public BaseProvider(SourceRepository sourceRepository, CodeSystemConverter codeSystemConverter,
                        CollectionRepository collectionRepository, ValueSetConverter valueSetConverter,
                        ConceptMapConverter conceptMapConverter, OclFhirUtil oclFhirUtil) {
        this.sourceRepository = sourceRepository;
        this.codeSystemConverter = codeSystemConverter;
        this.collectionRepository = collectionRepository;
        this.valueSetConverter = valueSetConverter;
        this.conceptMapConverter = conceptMapConverter;
        this.oclFhirUtil = oclFhirUtil;
    }

    protected List<Source> getSources(List<String> access) {
        return sourceRepository.findAllMostRecentReleased(access).stream().sorted(Comparator.comparing(Source::getMnemonic))
                .collect(Collectors.toList());
    }

    protected List<Source> getSourceByUrl(StringType url, StringType version, List<String> access) {
        return oclFhirUtil.getSourceByUrl(url, version, access);
    }

    protected List<Source> getSourceByOwner(StringType owner, List<String> access) {
        if (!isValid(owner))
            return new ArrayList<>();
        List<Source> sources = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (ORG.equals(ownerType)) {
            sources.addAll(sourceRepository.findByOrganizationMnemonicAndPublicAccessIn(value, access));
        } else {
            sources.addAll(sourceRepository.findByUserIdUsernameAndPublicAccessIn(value, access));
        }
        Multimap<String, Source> map = ArrayListMultimap.create();
        sources.forEach(s -> map.put(s.getMnemonic(), s));
        List<Source> filtered = new ArrayList<>();
        map.asMap().forEach((k,v) -> {
            v.stream().filter(s -> (s.getReleased() != null && s.getReleased())).max(Comparator.comparing(Source::getCreatedAt)).stream().findFirst().ifPresent(filtered::add);
        });
        return filtered.stream().sorted(Comparator.comparing(Source::getMnemonic)).collect(Collectors.toList());
    }

    protected List<Collection> getCollectionByOwnerAndIdAndVersion(String id, StringType owner, String version, List<String> access) {
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        final Collection collection;
            // get a given version
            if (ORG.equals(ownerType)) {
                collection = collectionRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(
                        id, version, value, access);
            } else {
                collection = collectionRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(
                        id, version, value, access);
            }
        return collection == null ? new ArrayList<>() : Collections.singletonList(collection);
    }

    protected List<Source> getSourceByOwnerAndIdAndVersion(String id, String owner, String version, List<String> access) {
        return getSourceByOwnerAndIdAndVersion(newStringType(id), newStringType(owner), newStringType(version), access);
    }

    protected List<Source> getSourceByOwnerAndIdAndVersion(StringType id, StringType owner, StringType version, List<String> access) {
        List<Source> sources = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());

        if (isVersionAll(version)) {
            // get all versions
            if (ORG.equals(ownerType)) {
                sources.addAll(sourceRepository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(),
                        value, access));
            } else {
                sources.addAll(sourceRepository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(),
                        value, access));
            }
        } else {
            addSourceVersion(id, version, access, sources, ownerType, value);
        }
        if (sources.isEmpty())
            throw new ResourceNotFoundException(notFound(CodeSystem.class, owner, id, version));
        return sources;
    }

    protected void addSourceVersion(StringType id, StringType version, List<String> access, List<Source> sources, String ownerType, String ownerId) {
        final Source source = oclFhirUtil.getSourceVersion(id, version, access, ownerType, ownerId);
        if (source != null) sources.add(source);
    }

    protected List<Source> filterSourceHead(List<Source> sources) {
        return sources.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }

    protected List<Collection> filterCollectionHead(List<Collection> collections) {
        return collections.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }

    protected String buildAccessionId(String resourceType, IdType idType, StringType owner) {
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        return FS + (ORG.equals(ownerType) ? ORGS : USERS) +
                FS + value +
                FS + resourceType +
                FS + idType.getIdPart() +
                FS + (isValid(idType.getVersionIdPart()) ? idType.getVersionIdPart() + FS : EMPTY);
    }

     class Filter {

        private String status;
        private String contentMode;
        private String publisher;
        private String version;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getContentMode() {
            return contentMode;
        }

        public void setContentMode(String contentMode) {
            this.contentMode = contentMode;
        }

         public String getPublisher() {
             return publisher;
         }

         public void setPublisher(String publisher) {
             this.publisher = publisher;
         }

         public String getVersion() {
             return version;
         }

         public void setVersion(String version) {
             this.version = version;
         }

         @Override
         public String toString() {
             return "Filter{" +
                     "status='" + status + '\'' +
                     ", contentMode='" + contentMode + '\'' +
                     ", publisher='" + publisher + '\'' +
                     ", version='" + version + '\'' +
                     '}';
         }
     }

    protected Filter getFilter(StringType status, StringType contentMode, StringType publisher, StringType version) {
        final Filter filter = new Filter();
        if (isValid(status)) filter.setStatus(status.getValue());
        if (isValid(contentMode)) filter.setContentMode(contentMode.getValue());
        if (isValid(publisher)) filter.setPublisher(publisher.getValue());
        if (isValid(version)) filter.setVersion(version.getValue());
        return filter;
    }

}
