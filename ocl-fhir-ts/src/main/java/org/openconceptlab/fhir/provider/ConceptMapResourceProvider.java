package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.converter.ConceptMapConverter;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

/**
 * The ConceptMapResourceProvider.
 * @author harpatel1
 */
@Component
public class ConceptMapResourceProvider extends BaseProvider implements IResourceProvider {

    private static final Log log = LogFactory.getLog(ConceptMapResourceProvider.class);

    public ConceptMapResourceProvider(SourceRepository sourceRepository, CodeSystemConverter codeSystemConverter,
                                      CollectionRepository collectionRepository, ValueSetConverter valueSetConverter,
                                      ConceptMapConverter conceptMapConverter, OclFhirUtil oclFhirUtil) {
        super(sourceRepository, codeSystemConverter, collectionRepository, valueSetConverter, conceptMapConverter, oclFhirUtil);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ConceptMap.class;
    }

    @Create
    public MethodOutcome createConceptMap(@ResourceParam ConceptMap conceptMap, RequestDetails requestDetails) {
        if (conceptMap == null) {
            throw new InvalidRequestException("The ConceptMap can not be empty");
        }
        String accessionId = getAccessionIdentifier(Collections.singletonList(conceptMap.getIdentifier()));
        if (!isValid(accessionId)) {
            throw new InvalidRequestException("The ConceptMap.identifier is empty or identifier of type ACSN is empty.");
        }
        if (!isValid(conceptMap.getUrl())) {
            throw new InvalidRequestException("The ConceptMap.url can not be empty. Please provide canonical url.");
        }
        conceptMapConverter.createConceptMap(conceptMap, accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    @Update
    public MethodOutcome updateConceptMap(@IdParam IdType idType,
                                          @ResourceParam ConceptMap conceptMap,
                                          RequestDetails requestDetails) {
        if (conceptMap == null) {
            throw new InvalidRequestException("The ConceptMap can not be empty");
        }
        if (idType == null || !isValid(idType.getIdPart()) || !isValid(idType.getVersionIdPart()) ||
                isVersionAll(newStringType(idType.getVersionIdPart()))) {
            throw new InvalidRequestException("Invalid ConceptMap.id or ConceptMap.version provided. Both parameters are required.");
        }
        StringType owner = newStringType(requestDetails.getHeader(OWNER));
        if (!isValid(owner))
            throw new InvalidRequestException("Owner can not be empty.");
        List<Source> sources = filterSourceHead(
                getSourceByOwnerAndIdAndVersion(idType.getIdPart(), owner.getValue(), idType.getVersionIdPart(), publicAccess));
        if (sources.isEmpty()) {
            throw new InvalidRequestException("ConceptMap is not found.");
        }
        String accessionId = buildAccessionId(CONCEPTMAP, idType, owner);
        conceptMapConverter.updateConceptMap(conceptMap, sources.get(0), accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    /**
     * Returns all public {@link ConceptMap}.
     *
     * @return {@link Bundle}
     */
    @Search()
    @Transactional
    public Bundle searchConceptMaps(@OptionalParam(name = PAGE) StringType page,
                                    @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                    RequestDetails details) {
        List<Source> sources = filterSourceHead(getSources(publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false,
                getPage(page), hasNext);
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        Bundle bundle = OclFhirUtil.getBundle(conceptMaps, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(sources.size());
        return bundle;
    }

    /**
     * Returns public {@link ConceptMap} for a given Url.
     * @param url
     * @param version
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchConceptMapByUrl(@RequiredParam(name = ConceptMap.SP_URL) StringType url,
                                        @OptionalParam(name = VERSION) StringType version,
                                        @OptionalParam(name = PAGE) StringType page,
                                        @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                        RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByUrl(url, version, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        boolean includeMappings = !isValid(version) || !isVersionAll(version);
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, includeMappings,
                getPage(page), hasNext);
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        Bundle bundle = OclFhirUtil.getBundle(conceptMaps, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(sources.size());
        return bundle;
    }

    /**
     * Returns all public {@link ConceptMap} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchConceptMapByOwner(@RequiredParam(name = OWNER) StringType owner,
                                          @OptionalParam(name = PAGE) StringType page,
                                          @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                          RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwner(owner, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false,
                getPage(page), hasNext);
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        Bundle bundle = OclFhirUtil.getBundle(conceptMaps, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(sources.size());
        return bundle;
    }

    /**
     * Returns public {@link ConceptMap} for a given owner and Id. Returns given version if provided, otherwise
     * latest version is returned.
     * @param owner
     * @param id
     * @param version
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchConceptMapByOwnerAndId(@RequiredParam(name = OWNER) StringType owner,
                                               @RequiredParam(name = ID) StringType id,
                                               @OptionalParam(name = VERSION) StringType version,
                                               @OptionalParam(name = PAGE) StringType page,
                                               @OptionalParam(name = OWNER_URL) StringType ownerUrl,
                                               RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwnerAndIdAndVersion(id, owner, version, publicAccess));
        StringBuilder hasNext = new StringBuilder();
        boolean includeMappings = !isVersionAll(version);
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, includeMappings,
                getPage(page), hasNext);
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        Bundle bundle = OclFhirUtil.getBundle(conceptMaps, isValid(ownerUrl) ? ownerUrl.getValue() : details.getCompleteUrl(),
                getPrevPage(page), getNextPage(page, hasNext));
        bundle.setTotal(sources.size());
        return bundle;
    }

    @Operation(name = TRANSLATE, idempotent = true)
    @Transactional
    public Parameters conceptMapTranslate(@OperationParam(name = URL, min = 1, type = UriType.class) UriType conceptMapUrl,
                                          @OperationParam(name = CONCEPT_MAP_VERSION, type = StringType.class) StringType conceptMapVersion,
                                          @OperationParam(name = SYSTEM, min = 1, type = UriType.class) UriType sourceSystem,
                                          @OperationParam(name = VERSION, type = StringType.class) StringType sourceVersion,
                                          @OperationParam(name = CODE, min = 1, type = CodeType.class) CodeType sourceCode,
                                          @OperationParam(name = CODING, type = Coding.class) Coding coding,
                                          @OperationParam(name = TARGET_SYSTEM, type = UriType.class) UriType targetSystem,
                                          @OperationParam(name = OWNER, type = StringType.class) StringType owner,
                                          RequestDetails requestDetails) {

        // $translate by id
        String id = requestDetails.getHeader(RESOURCE_ID);
        if (isValid(id) && isValid(owner)) {
            if (coding != null) {
                sourceCode = new CodeType(coding.getCode());
                if (!isValid(conceptMapVersion))
                    sourceVersion = new StringType(coding.getVersion());
            }
            validate(sourceCode, sourceSystem);
            List<Source> conceptMaps = getSourceByOwnerAndIdAndVersion(newStringType(id), owner, conceptMapVersion, publicAccess);
            if (conceptMaps.isEmpty()) throw new ResourceNotFoundException(notFound(CodeSystem.class, owner, newStringType(id), conceptMapVersion));
            return conceptMapConverter.translate(conceptMaps.get(0), sourceSystem, sourceVersion, sourceCode, targetSystem, publicAccess);
        }
        // $translate by url
        if (coding != null) {
            sourceSystem = new UriType(coding.getSystem());
            sourceCode = new CodeType(coding.getCode());
            sourceVersion = new StringType(coding.getVersion());
        }
        validate(conceptMapUrl, sourceCode, sourceSystem);
        Source conceptMap = isValid(owner) ? oclFhirUtil.getSourceByOwnerAndUrl(owner, newStringType(conceptMapUrl), conceptMapVersion, publicAccess) :
                getSourceByUrl(newStringType(conceptMapUrl), conceptMapVersion, publicAccess).get(0);
        return conceptMapConverter.translate(conceptMap, sourceSystem, sourceVersion, sourceCode, targetSystem, publicAccess);
    }

    private void validate(UriType conceptMapUrl, CodeType sourceCode, UriType sourceSystem) {
        if (!isValid(conceptMapUrl))
            throw new InvalidRequestException("Could not perform ConceptMap $translate operation, the url parameter is required.");
        validate(sourceCode, sourceSystem);
    }

    private void validate(CodeType sourceCode, UriType sourceSystem) {
        if (!isValid(sourceCode) || !isValid(sourceSystem)) {
            String msg = "Could not perform ConceptMap $translate operation, the code and system parameters are required.";
            throw new InvalidRequestException(msg);
        }
    }

}
