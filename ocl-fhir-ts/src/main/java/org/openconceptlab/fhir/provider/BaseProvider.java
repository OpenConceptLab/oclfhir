package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IntegerType;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirConstants.HEAD;
import static org.openconceptlab.fhir.util.OclFhirConstants.ORG;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.notFound;

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
        List<Source> sources = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            sources.addAll(sourceRepository.findByCanonicalUrlAndPublicAccessIn(url.getValue(), access).stream()
                    .sorted(Comparator.comparing(Source::getVersion).reversed()).collect(Collectors.toList()));
        } else {
            final Source source;
            if (!isValid(version)) {
                // get most recent released version
                source = oclFhirUtil.getMostRecentReleasedSourceByUrl(url, access);
            } else {
                // get a given version
                source = sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(url.getValue(), version.getValue(), access);
            }
            if (source != null) sources.add(source);
        }
        if (sources.isEmpty())
            throw new ResourceNotFoundException(notFound(CodeSystem.class, url, version));
        return sources;
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
}
