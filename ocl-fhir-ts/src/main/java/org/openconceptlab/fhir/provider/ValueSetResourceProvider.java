package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.converter.ConceptMapConverter;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

/**
 * The ValueSetResourceProvider.
 * @author harpatel1
 */
@Component
public class ValueSetResourceProvider extends BaseProvider implements IResourceProvider {

    private static final Log log = LogFactory.getLog(ValueSetResourceProvider.class);

    public ValueSetResourceProvider(SourceRepository sourceRepository, CodeSystemConverter codeSystemConverter,
                                    CollectionRepository collectionRepository, ValueSetConverter valueSetConverter,
                                    ConceptMapConverter conceptMapConverter, OclFhirUtil oclFhirUtil) {
        super(sourceRepository, codeSystemConverter, collectionRepository, valueSetConverter, conceptMapConverter, oclFhirUtil);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
    }

    @Create
    public MethodOutcome createValueSet(@ResourceParam ValueSet valueSet, RequestDetails requestDetails) {
        if (valueSet == null) {
            throw new InvalidRequestException("The ValueSet can not be empty");
        }
        String accessionId = getAccessionIdentifier(valueSet.getIdentifier());
        if (!isValid(accessionId)) {
            throw new InvalidRequestException("The ValueSet.identifier is empty or identifier of type ACSN is empty.");
        }
        if (!isValid(valueSet.getUrl())) {
            throw new InvalidRequestException("The ValueSet.url can not be empty. Please provide canonical url.");
        }
        valueSetConverter.createValueSet(valueSet, accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    @Update
    public MethodOutcome updateValueSet(@IdParam IdType idType,
                                        @ResourceParam ValueSet valueSet,
                                        RequestDetails requestDetails) {
        if (valueSet == null) {
            throw new InvalidRequestException("The ValueSet can not be empty");
        }
        if (idType == null || !isValid(idType.getIdPart()) || !isValid(idType.getVersionIdPart()) ||
                isVersionAll(newStringType(idType.getVersionIdPart()))) {
            throw new InvalidRequestException("Invalid ValueSet.id or ValueSet.version provided. Both parameters are required.");
        }
        StringType owner = newStringType(requestDetails.getHeader(OWNER));
        if (!isValid(owner))
            throw new InvalidRequestException("Owner can not be empty.");
        List<Collection> collections = filterCollectionHead(
                getCollectionByOwnerAndIdAndVersion(idType.getIdPart(), owner, idType.getVersionIdPart(), publicAccess));
        if (collections.isEmpty()) {
            throw new InvalidRequestException("ValueSet is not found.");
        }
        String accessionId = buildAccessionId(VALUESET, idType, owner);
        valueSetConverter.updateValueSet(valueSet, collections.get(0), accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    /**
     * Returns all public {@link ValueSet}.
     *
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSets(@OptionalParam(name = PAGE) StringType page,
                                  @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                  RequestDetails details) {
        List<Collection> collections = filterCollectionHead(getCollections(publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, false, getPage(page), hasNext);
        log.info("Found " + valueSets.size() + " ValueSets.");
        Bundle bundle = OclFhirUtil.getBundle(valueSets, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(collections.size());
        return bundle;
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
                                      @OptionalParam(name = PAGE) StringType page,
                                      @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                      RequestDetails details) {
        List<Collection> collections = filterCollectionHead(getCollectionByUrl(url, version, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, !isVersionAll(version), getPage(page), hasNext);
        log.info("Found " + valueSets.size() + " ValueSets.");
        Bundle bundle = OclFhirUtil.getBundle(valueSets, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(collections.size());
        return bundle;
    }

    /**
     * Returns all public {@link ValueSet} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchValueSetByOwner(@RequiredParam(name = OWNER) StringType owner,
                                        @OptionalParam(name = PAGE) StringType page,
                                        @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                        RequestDetails details) {
        List<Collection> collections = filterCollectionHead(getCollectionByOwner(owner, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, false, getPage(page), hasNext);
        log.info("Found " + valueSets.size() + " ValueSets.");
        Bundle bundle = OclFhirUtil.getBundle(valueSets, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(collections.size());
        return bundle;
    }

    /**
     * Returns public {@link ValueSet} for a given owner and Id. Returns given version if provided, otherwise
     * latest version is returned.
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
                                             @OptionalParam(name = PAGE) StringType page,
                                             @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                             RequestDetails details) {
        List<Collection> collections = filterCollectionHead(getCollectionByOwnerAndId(id, owner, version, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ValueSet> valueSets = valueSetConverter.convertToValueSet(collections, !isVersionAll(version), getPage(page), hasNext);
        log.info("Found " + valueSets.size() + " ValueSets.");
        Bundle bundle = OclFhirUtil.getBundle(valueSets, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(collections.size());
        return bundle;
    }

    @Operation(name = VALIDATE_CODE, idempotent = true)
    @Transactional
    public Parameters valueSetValidateCode(@OperationParam(name = URL, type = UriType.class, min = 1) UriType url,
                                           @OperationParam(name = VALUESET_VERSION, type = StringType.class) StringType valueSetVersion,
                                           @OperationParam(name = CODE, type = CodeType.class, min = 1) CodeType code,
                                           @OperationParam(name = SYSTEM, type = UriType.class, min = 1) UriType system,
                                           @OperationParam(name = SYSTEM_VERSION, type = StringType.class) StringType systemVersion,
                                           @OperationParam(name = DISPLAY, type = StringType.class) StringType display,
                                           @OperationParam(name = DISP_LANG, type = CodeType.class) CodeType displayLanguage,
                                           @OperationParam(name = CODING, type = Coding.class) Coding coding,
                                           @OperationParam(name = OWNER, type = StringType.class) StringType owner,
                                           RequestDetails requestDetails) {

        if (isValid(code) && coding != null)
            throw new InvalidRequestException("Either code or coding should be provided, can not accept both.");
        if (coding != null) {
            system = new UriType(coding.getSystem());
            code = new CodeType(coding.getCode());
            systemVersion = new StringType(coding.getVersion());
            display = new StringType(coding.getDisplay());
        }

        // $validate-code by id
        String id = requestDetails.getHeader(RESOURCE_ID);
        if (isValid(id) && isValid(owner)) {
            validate(system, code, coding, valueSetVersion, systemVersion);
            List<Collection> collections = getCollectionByOwnerAndId(newStringType(id), owner, valueSetVersion, publicAccess);
            if (collections.isEmpty())
                throw new ResourceNotFoundException(notFound(ValueSet.class, owner, newStringType(id), valueSetVersion));
            return valueSetConverter.validateCode(collections.get(0), system, systemVersion, newString(code), display,
                    displayLanguage, owner, publicAccess);
        }

        // $validate-code by url
        validate(url, system, code, coding, valueSetVersion, systemVersion);
        Collection collection = isValid(owner) ? getCollectionByOwnerAndUrl(owner, newStringType(url), valueSetVersion, publicAccess) :
                    getCollectionByUrl(newStringType(url), valueSetVersion, publicAccess).get(0);
        return valueSetConverter.validateCode(collection, system, systemVersion, newString(code), display, displayLanguage,
                owner, publicAccess);
    }

    @Operation(name = EXPAND, idempotent = true)
    @Transactional
    public ValueSet valueSetExpand(@OperationParam(name = URL, type = UriType.class, min = 1) UriType url,
                                   @OperationParam(name = VALUESET_VERSION, type = StringType.class) StringType valueSetVersion,
                                   @OperationParam(name = OFFSET, type = IntegerType.class) IntegerType offset,
                                   @OperationParam(name = COUNT, type = IntegerType.class) IntegerType count,
                                   @OperationParam(name = INCLUDE_DESIGNATIONS, type = BooleanType.class) BooleanType includeDesignations,
                                   @OperationParam(name = INCLUDE_DEFINITION, type = BooleanType.class) BooleanType includeDefinition,
                                   @OperationParam(name = ACTIVE_ONLY, type = BooleanType.class) BooleanType activeOnly,
                                   @OperationParam(name = DISPLAY_LANGUAGE, type = CodeType.class) CodeType displayLanguage,
                                   @OperationParam(name = EXCLUDE_SYSTEM, type = CanonicalType.class, max = OperationParam.MAX_UNLIMITED) Set<CanonicalType> excludeSystems,
                                   @OperationParam(name = SYSTEMVERSION, type = CanonicalType.class, max = OperationParam.MAX_UNLIMITED) Set<CanonicalType> systemVersions,
                                   @OperationParam(name = FILTER, type = StringType.class) StringType filter,
                                   @OperationParam(name = OWNER, type = StringType.class) StringType owner,
                                   RequestDetails requestDetails) {
        validate(url, offset, count, valueSetVersion);
        String id = requestDetails.getHeader(RESOURCE_ID);
        // $expand by id
        Collection collection = null;
        if (isValid(id)) {
            List<Collection> collections = getCollectionByOwnerAndId(newStringType(id), owner, valueSetVersion, publicAccess);
            if (collections.isEmpty())
                throw new ResourceNotFoundException(notFound(ValueSet.class, owner, newStringType(id), valueSetVersion));
            collection = collections.get(0);
        // $expand by url
        } else {
            collection = isValid(owner) ? getCollectionByOwnerAndUrl(owner, newStringType(url), valueSetVersion, publicAccess) :
                    getCollectionByUrl(newStringType(url), valueSetVersion, publicAccess).get(0);
        }

        if (!isValid(offset)) offset = new IntegerType(0);
        if (!isValid(count)) count = new IntegerType(100);
        if (count.getValue() > 100) count.setValue(100);
        // by default include all designations
        if (!isValid(includeDesignations)) includeDesignations = new BooleanType(true);
        // by default exclude ValueSet definition
        if (!isValid(includeDefinition)) includeDefinition = new BooleanType(false);
        // by default only include active concepts
        if (!isValid(activeOnly)) activeOnly = new BooleanType(true);
        List<String> excludeSystemsList = new ArrayList<>();
        List<String> systemVersionsList = new ArrayList<>();
        if (excludeSystems != null)
            excludeSystemsList = excludeSystems.parallelStream()
                    .filter(OclFhirUtil::isValid)
                    .map(PrimitiveType::getValue)
                    .map(String::trim)
                    .collect(Collectors.toList());
        if (systemVersions != null)
            systemVersionsList = systemVersions.parallelStream()
                    .filter(OclFhirUtil::isValid)
                    .map(PrimitiveType::getValue)
                    .map(String::trim)
                    .collect(Collectors.toList());
        validateSystemVersion(systemVersionsList);
        return valueSetConverter.expand(collection, offset, count, includeDesignations, includeDefinition,
                activeOnly, displayLanguage, excludeSystemsList, systemVersionsList, filter);
    }

    private List<Collection> getCollections(List<String> access) {
        return collectionRepository.findAllLatest(access);
    }

    private List<Collection> getCollectionByUrl(StringType url, StringType version, List<String> access) {
        List<Collection> collections = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            collections.addAll(collectionRepository.findByCanonicalUrlAndPublicAccessIn(url.getValue(), access));
        } else {
            final Collection collection;
            if (!isValid(version)) {
                // get latest version
                collection = getLatestCollectionByUrl(url, access);
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
            // get latest version
            collection = getLatestCollectionByOwnerAndUrl(value, ownerType, url, access);
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
            return collectionRepository.findByOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByMnemonic(value, access, true);
        } else {
            return collectionRepository.findByUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByMnemonic(value, access, true);
        }
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
            // get latest version
            collection = getLatestCollectionByOwner(id.getValue(), ownerId, ownerType, access);
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

    private Collection getLatestCollectionByOwner(String id, String owner, String ownerType, List<String> access) {
        if (ORG.equals(ownerType)) {
            return collectionRepository.findFirstByMnemonicAndPublicAccessInAndOrganizationMnemonicAndIsLatestVersionOrderByCreatedAtDesc(
                    id, access, owner, true);
        }
        return collectionRepository.findFirstByMnemonicAndPublicAccessInAndUserIdUsernameAndIsLatestVersionOrderByCreatedAtDesc(
                id, access, owner, true);
    }

    private Collection getLatestCollectionByUrl(StringType url, List<String> access) {
        return collectionRepository.findFirstByCanonicalUrlAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(
                url.getValue(), access, true
        );
    }

    private Collection getLatestCollectionByOwnerAndUrl(String owner, String ownerType, StringType url, List<String> access) {
        if (ORG.equals(ownerType))
            return collectionRepository.findFirstByCanonicalUrlAndOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(
                    url.getValue(), owner, access, true
            );
        return collectionRepository.findFirstByCanonicalUrlAndUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(
                url.getValue(), owner, access, true
        );
    }

    private Collection getCollectionVersionByOwnerAndUrl(String owner, String ownerType, StringType url, StringType version, List<String> access) {
        if (ORG.equals(ownerType))
            return collectionRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
        return collectionRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
    }

    private void validate(UriType url, UriType system, CodeType code, Coding coding, StringType... versions) {
        if (!isValid(url) || !isValid(system))
            throw new InvalidRequestException("Both url and system must be provided.");
        for (StringType version : versions) {
            if (isVersionAll(version)) throw new InvalidRequestException("Invalid version provided.");
        }
        validateCode(code, coding);
    }

    private void validate(UriType system, CodeType code, Coding coding, StringType... versions) {
        if (!isValid(system))
            throw new InvalidRequestException("The system must be provided.");
        for (StringType version : versions) {
            if (isVersionAll(version)) throw new InvalidRequestException("Invalid version provided.");
        }
        validateCode(code, coding);
    }

    private void validateCode(CodeType code, Coding coding) {
        if (!isValid(code) && coding == null)
            throw new InvalidRequestException("Either of code or coding must be provided.");
        if (coding != null && (!isValid(coding.getSystem()) || !isValid(coding.getCode())))
            throw new InvalidRequestException("Both system and code of coding must be provided.");
    }

    private void validate(UriType url, IntegerType offset, IntegerType count, StringType... versions) {
        if (!isValid(url))
            throw new InvalidRequestException("Url parameter of $expand operation must be provided.");
        if (isValid(offset) && offset.getValue() < 0)
            throw new InvalidRequestException("Offset parameter of $expand operation can not be negative.");
        if (isValid(count) && count.getValue() < 0)
            throw new InvalidRequestException("Count parameter of $expand operation can not be negative.");
        for (StringType version : versions) {
            if (isVersionAll(version)) throw new InvalidRequestException("Invalid version provided.");
        }
    }

    private void validateSystemVersion(List<String> systemVersions) {
        systemVersions.stream().map(m -> m.split("\\|")[0]).collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
                .forEach((k,v) -> {
                    if (v > 1) {
                        throw new InvalidRequestException("ValueSet $expand parameter system-version can not contain duplicate system url. Duplicate system url="+k+" found.");
                    } });
    }
}
