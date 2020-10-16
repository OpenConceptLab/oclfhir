package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirUtil.*;

/**
 * The ValueSetResourceProvider.
 * @author harpatel1
 */
@Component
public class ValueSetResourceProvider implements IResourceProvider {
	
	private CollectionRepository collectionRepository;
	private ValueSetConverter valueSetConverter;
    private OclFhirUtil oclFhirUtil;

    @Autowired
    public ValueSetResourceProvider(CollectionRepository collectionRepository, ValueSetConverter valueSetConverter, OclFhirUtil oclFhirUtil) {
        this.collectionRepository = collectionRepository;
        this.valueSetConverter = valueSetConverter;
        this.oclFhirUtil = oclFhirUtil;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
    }

    /**
     * Get a single {@link ValueSet} for given {@link IdType}.
     * @param id {@link ValueSet#id}
     * @return {@link ValueSet}
     */
    @Read
    @Transactional
    public ValueSet get(@IdParam IdType id) {
        List<Collection> collections = getPublicCollectionByMnemonic(new StringType(id.getIdPart()));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, null);
        if (valueSets.size() >= 1) {
            return valueSets.get(0);
        } else {
            throw new ResourceNotFoundException(id, oclFhirUtil.getNotFoundOutcome(id));
        }
    }

    /**
     * Returns all public {@link ValueSet}.
     *
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle getValueSets(RequestDetails details) {
        List<Collection> collections = getPublicCollections();
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, null);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link ValueSet} for a given Url.
     * @param url
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByUrl(@RequiredParam(name = ValueSet.SP_URL) StringType url, RequestDetails details) {
        List<Collection> collections = getPublicCollectionByUrl(url);
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, null);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    @Search
    @Transactional
    public Bundle searchValueSetByOwner(@RequiredParam(name = "owner") StringType owner, @OptionalParam(name = "id") StringType id
            , RequestDetails details) {
        List<Collection> collections = getPublicCollectionByIdOrOwner(id, owner, collectionRepository);
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, null);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Collection> getPublicCollections() {
        return collectionRepository.findByPublicAccessIn(publicAccess).parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    private List<Collection> getPublicCollectionByMnemonic(StringType id) {
        return collectionRepository.findByMnemonicAndPublicAccessIn(id.getValue(), publicAccess)
                .parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    private List<Collection> getPublicCollectionByUrl(StringType url) {
        return collectionRepository.findByExternalIdAndPublicAccessIn(url.getValue(), publicAccess)
                .parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    public List<Collection> getPublicCollectionByIdOrOwner(StringType id, StringType owner, CollectionRepository repository) {
        List<Collection> collections = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (ORG.equals(ownerType)) {
            if (isValid(id)) {
                collections.addAll(repository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(), value, publicAccess));
            } else {
                collections.addAll(repository.findByOrganizationMnemonicAndPublicAccessIn(value, publicAccess));
            }
        } else {
            if (isValid(id)) {
                collections.addAll(repository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(), value, publicAccess));
            } else {
                collections.addAll(repository.findByUserIdUsernameAndPublicAccessIn(value, publicAccess));
            }
        }
        return collections.parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

}
