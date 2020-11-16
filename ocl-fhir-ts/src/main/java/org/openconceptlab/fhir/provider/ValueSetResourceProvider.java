package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
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

    public ValueSetResourceProvider(){
        super();
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
     * @param version
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByUrl(@RequiredParam(name = ValueSet.SP_URL) StringType url,
                                      @OptionalParam(name = VERSION) StringType version,
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
     * @param version
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByOwnerAndId(@RequiredParam(name = OWNER) StringType owner,
                                               @RequiredParam(name = ID) StringType id,
                                               @OptionalParam(name = VERSION) StringType version,
                                               RequestDetails details) {
        List<Collection> collections = filterHead(getCollectionByOwnerAndId(id, owner, version, publicAccess));
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections);
        return OclFhirUtil.getBundle(valueSets, details.getFhirServerBase(), details.getRequestPath());
    }

    @Operation(name = VALIDATE_CODE, idempotent = true)
    @Transactional
    public Parameters valueSetValidateCode(@OperationParam(name = URL, type = UriType.class) UriType url,
                                           @OperationParam(name = VALUESET_VERSION, type = StringType.class) StringType valueSetVersion,
                                           @OperationParam(name = CODE, type = CodeType.class) CodeType code,
                                           @OperationParam(name = SYSTEM, type = UriType.class) UriType system,
                                           @OperationParam(name = SYSTEM_VERSION, type = StringType.class) StringType systemVersion,
                                           @OperationParam(name = DISPLAY, type = StringType.class) StringType display,
                                           @OperationParam(name = DISP_LANG, type = CodeType.class) CodeType displayLanguage,
                                           @OperationParam(name = CODING, type = Coding.class) Coding coding,
                                           @OperationParam(name = OWNER, type = StringType.class) StringType owner) {



        if (isValid(code) && coding != null)
            throw new UnprocessableEntityException("Either code or coding should be provided, can not accept both.");
        if (coding != null) {
            system = new UriType(coding.getSystem());
            code = new CodeType(coding.getCode());
            systemVersion = new StringType(coding.getVersion());
            display = new StringType(coding.getDisplay());
        }
        // validate input values
        validate(url, system, code, coding);
        Collection collection = isValid(owner) ? getCollectionByOwnerAndUrl(owner, newStringType(url), valueSetVersion, publicAccess) :
                    getCollectionByUrl(newStringType(url), valueSetVersion, publicAccess).get(0);
        return valueSetConverter.validateCode(collection, system, systemVersion
                , newString(code), display, displayLanguage, owner, publicAccess);
    }

    private List<Collection> getCollections(List<String> access) {
        return collectionRepository.findByPublicAccessIn(access).parallelStream().filter(Collection::getIsLatestVersion)
                .collect(Collectors.toList());
    }

    private List<Collection> getCollectionByUrl(StringType url, StringType version, List<String> access) {
        List<Collection> collections = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            collections.addAll(collectionRepository.findByCanonicalUrlAndPublicAccessIn(url.getValue(), access));
        } else {
            final Collection collection;
            if (!isValid(version)) {
                // get most recent released version
                collection = getMostRecentReleasedCollectionByUrl(url, access);
            } else {
                // get a given version
                collection = collectionRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(url.getValue(), version.getValue(), access);
            }
            if (collection != null) collections.add(collection);
        }
        if (collections.isEmpty())
            throw new ResourceNotFoundException(notFound(ValueSet.class, url, version));
        return collections;
    }

    private Collection getCollectionByOwnerAndUrl(StringType owner, StringType url, StringType version, List<String> access) {
        Collection collection = null;
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (!isValid(version)) {
            // get most recent released version
            collection = getMostRecentReleasedCollectionByOwnerAndUrl(value, ownerType, url, access);
        } else {
            // get a given version
            collection = getCollectionVersionByOwnerAndUrl(value, ownerType, url, version, access);
        }
        if (collection == null)
            throw new ResourceNotFoundException(notFound(ValueSet.class, url, version));
        return collection;
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
        String ownerId = getOwner(owner.getValue());

        if (isVersionAll(version)) {
            // get all versions
            if (ORG.equals(ownerType)) {
                collections.addAll(collectionRepository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(),
                        ownerId, access));
            } else {
                collections.addAll(collectionRepository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(),
                        ownerId, access));
            }
        } else {
            Collection collection = getCollectionVersion(id, version, access, ownerType, ownerId);
            if (collection != null) collections.add(collection);
        }
        if (collections.isEmpty())
            throw new ResourceNotFoundException(notFound(ValueSet.class, owner, id, version));
        return collections;
    }

    public Collection getCollectionVersion(StringType id, StringType version, List<String> access, String ownerType, String ownerId) {
        final Collection collection;
        if (!isValid(version)) {
            // get most recent released version
            collection = getMostRecentReleasedCollectionByOwner(id.getValue(), ownerId, ownerType, access);
        } else {
            // get a given version
            if (ORG.equals(ownerType)) {
                collection = collectionRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(
                        id.getValue(), version.getValue(), ownerId, access);
            } else {
                collection = collectionRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(
                        id.getValue(), version.getValue(), ownerId, access);
            }
        }
        return collection;
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
        return collectionRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                url.getValue(), true, access
        );
    }

    private Collection getMostRecentReleasedCollectionByOwnerAndUrl(String owner, String ownerType, StringType url, List<String> access) {
        if (ORG.equals(ownerType))
            return collectionRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    url.getValue(), true, owner, access
            );
        return collectionRepository.findFirstByCanonicalUrlAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(
                url.getValue(), true, owner, access
        );
    }

    private Collection getCollectionVersionByOwnerAndUrl(String owner, String ownerType, StringType url, StringType version, List<String> access) {
        if (ORG.equals(ownerType))
            return collectionRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
        return collectionRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
    }

    private List<Collection> filterHead(List<Collection> collections) {
        return collections.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }

    private void validate(UriType url, UriType system, CodeType code, Coding coding) {
        if (!isValid(url) || !isValid(system))
            throw new UnprocessableEntityException("Both url and system must be provided.");
        if (!isValid(code) && coding == null)
            throw new UnprocessableEntityException("Either of code or coding must be provided.");
        if (coding != null && (!isValid(coding.getSystem()) || !isValid(coding.getCode())))
            throw new UnprocessableEntityException("Both system and code of coding must be provided.");
    }
}
