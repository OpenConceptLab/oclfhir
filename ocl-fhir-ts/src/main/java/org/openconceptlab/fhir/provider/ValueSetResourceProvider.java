package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openconceptlab.fhir.util.OclFhirUtil.*;

/**
 * The ValueSetResourceProvider.
 * @author hp11
 */
@Component
public class ValueSetResourceProvider implements IResourceProvider {
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private ValueSetConverter valueSetConverter;
	
    @Autowired
    private OclFhirUtil oclFhirUtil;
	
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
        List<ValueSet> valueSets = new ArrayList<>();
        List<Collection> collections = getPublicCollectionByMnemonic(new StringType(id.getIdPart()));
        valueSetConverter.convertToValueSet(valueSets, collections, null);
        if (valueSets.size() >= 1) {
            return valueSets.get(0);
        } else {
            throw new ResourceNotFoundException(id, oclFhirUtil.getNotFoundOutcome(id));
        }
    }

    /**
     * Returns all public {@link ValueSet}.
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle getValueSets(RequestDetails details) {
        List<ValueSet> valueSets = new ArrayList<>();
        List<Collection> collections = getPublicCollections();
        valueSetConverter.convertToValueSet(valueSets, collections, null);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns all public {@link ValueSet} for a given publisher.
     * @param publisher
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle getValueSetsByPublisher(@RequiredParam(name = ValueSet.SP_PUBLISHER) StringType publisher, RequestDetails details) {
        validatePublisher(publisher.getValue());
        List<ValueSet> valueSets = new ArrayList<ValueSet>();
        List<Collection> collections = getPublicCollectionsByPublisher(publisher.getValue());
        valueSetConverter.convertToValueSet(valueSets, collections, null);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Collection> getPublicCollectionsByPublisher(String publisher) {
        String owner = getOwner(publisher);
        String value = getPublisher(publisher);
        if(ORG.equals(owner)) {
            return collectionRepository.findByOrganizationMnemonic(value);
        } else {
            return collectionRepository.findByUserIdUsername(value);
        }
    }

    private List<Collection> getPublicCollections() {
        return collectionRepository.findByPublicAccessIn(publicAccess);
    }

    private List<Collection> getPublicCollectionByMnemonic(StringType id) {
        return collectionRepository.findByMnemonicAndPublicAccessIn(id.getValue(), publicAccess);
    }

}
