package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.repository.CollectionRepository;
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
	
	CollectionRepository collectionRepository;
	ValueSetConverter valueSetConverter;
    OclFhirUtil oclFhirUtil;

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
     * Returns all public {@link ValueSet}.
     *
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSets(RequestDetails details) {
        List<Collection> collections = filterHead(getCollections(publicAccess));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link ValueSet} for a given Url.
     * @param url
     * @param _history
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByUrl(@RequiredParam(name = ValueSet.SP_URL) StringType url,
                                      @OptionalParam(name = _HISTORY) StringType version,
                                      RequestDetails details) {
        List<Collection> collections = filterHead(getCollectionByUrl(url, version, publicAccess));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns all public {@link ValueSet} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByOwner(@RequiredParam(name = OWNER) StringType owner,
                                        RequestDetails details) {
        List<Collection> collections = filterHead(getCollectionByOwner(owner, publicAccess));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link ValueSet} for a given owner and Id. Returns given version if provided, otherwise
     * most recent released version is returned.
     * @param owner
     * @param id
     * @param _history
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByOwnerAndId(@RequiredParam(name = OWNER) StringType owner,
                                               @RequiredParam(name = ID) StringType id,
                                               @OptionalParam(name = _HISTORY) StringType version,
                                               RequestDetails details) {
        List<Collection> collections = filterHead(getCollectionByOwnerAndId(id, owner, version, publicAccess));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Collection> getCollections(List<String> access) {
        return collectionRepository.findByPublicAccessIn(access).parallelStream().filter(Collection::getIsLatestVersion)
                .collect(Collectors.toList());
    }

    private List<Collection> getCollectionByUrl(StringType url, StringType version, List<String> access) {
        List<Collection> collections = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            collections.addAll(collectionRepository.findByExternalIdAndPublicAccessIn(url.getValue(), access));
        } else {
            final Collection collection;
            if (!isValid(version)) {
                // get most recent released version
                collection = getMostRecentReleasedCollectionByUrl(url, access);
            } else {
                // get a given version
                collection = collectionRepository.findFirstByExternalIdAndVersionAndPublicAccessIn(url.getValue(), version.getValue(), access);
            }
            if (collection != null) collections.add(collection);
        }
        if (collections.isEmpty())
            throw new ResourceNotFoundException(notFound(ValueSet.class, url, version));
        return collections;
    }

    private List<Collection> getCollectionByOwner(StringType owner, List<String> access) {
        List<Collection> collections = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (ORG.equals(ownerType)) {
            collections.addAll(collectionRepository.findByOrganizationMnemonicAndPublicAccessIn(value, access));
        } else {
            collections.addAll(collectionRepository.findByUserIdUsernameAndPublicAccessIn(value, access));
        }
        return collections.parallelStream().filter(Collection::getIsLatestVersion).collect(Collectors.toList());
    }

    private List<Collection> getCollectionByOwnerAndId(StringType id, StringType owner, StringType version, List<String> access) {
        List<Collection> collections = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());

        if (isVersionAll(version)) {
            // get all versions
            if (ORG.equals(ownerType)) {
                collections.addAll(collectionRepository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(),
                        value, access));
            } else {
                collections.addAll(collectionRepository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(),
                        value, access));
            }
        } else {
            final Collection collection;
            if (!isValid(version)) {
                // get most recent released version
                collection = getMostRecentReleasedCollectionByOwner(id.getValue(), value, ownerType, access);
            } else {
                // get a given version
                if (ORG.equals(ownerType)) {
                    collection = collectionRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(
                            id.getValue(), version.getValue(), value, access);
                } else {
                    collection = collectionRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(
                            id.getValue(), version.getValue(), value, access);
                }
            }
            if (collection != null) collections.add(collection);
        }
        if (collections.isEmpty())
            throw new ResourceNotFoundException(notFound(ValueSet.class, owner, id, version));
        return collections;
    }

    private Collection getMostRecentReleasedCollectionByOwner(String id, String owner, String ownerType, List<String> access) {
        if (ORG.equals(ownerType)) {
            return collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(
                    id, true, access, owner);
        }
        return collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(
                id, true, access, owner);
    }

    private Collection getMostRecentReleasedCollectionByUrl(StringType url, List<String> access) {
        return collectionRepository.findFirstByExternalIdAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                url.getValue(), true, access
        );
    }

    private List<Collection> filterHead(List<Collection> collections) {
        return collections.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }
}
