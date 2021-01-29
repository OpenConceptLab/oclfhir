package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
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
import java.util.Arrays;
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

    /**
     * Returns all public {@link ConceptMap}.
     *
     * @return {@link Bundle}
     */
    @Search()
    @Transactional
    public Bundle searchConceptMaps(@OptionalParam(name = PAGE) StringType page, RequestDetails details) {
        List<Source> sources = filterSourceHead(getSources(publicAccess));
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false, getPage(page));
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        return OclFhirUtil.getBundle(conceptMaps, details.getCompleteUrl(), details.getRequestPath());
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
                                        RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByUrl(url, version, publicAccess));
        boolean includeMappings = !isValid(version) || !isVersionAll(version);
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, includeMappings,
                getPage(page));
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        return OclFhirUtil.getBundle(conceptMaps, details.getCompleteUrl(), details.getRequestPath());
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
                                          RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwner(owner, publicAccess));
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false, getPage(page));
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        return OclFhirUtil.getBundle(conceptMaps, details.getCompleteUrl(), details.getRequestPath());
    }

    /**
     * Returns public {@link ConceptMap} for a given owner and Id. Returns given version if provided, otherwise
     * most recent released version is returned.
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
                                               RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwnerAndIdAndVersion(id, owner, version, publicAccess));
        boolean includeMappings = !isVersionAll(version);
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, includeMappings, getPage(page));
        log.info("Found " + conceptMaps.size() + " ConceptMaps.");
        return OclFhirUtil.getBundle(conceptMaps, details.getCompleteUrl(), details.getRequestPath());
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
                                          @OperationParam(name = OWNER, type = StringType.class) StringType owner) {

        if (coding != null) {
            sourceSystem = new UriType(coding.getSystem());
            sourceCode = new CodeType(coding.getCode());
            sourceVersion = new StringType(coding.getVersion());
        }
        if (!isValid(conceptMapUrl) || !isValid(sourceCode) || !isValid(sourceSystem)) {
            String msg = "Could not perform ConceptMap $translate operation, the url, code and system parameters are required.";
            throw new InvalidRequestException(msg);
        }
        Source conceptMap = isValid(owner) ? oclFhirUtil.getSourceByOwnerAndUrl(owner, newStringType(conceptMapUrl), conceptMapVersion, publicAccess) :
                getSourceByUrl(newStringType(conceptMapUrl), conceptMapVersion, publicAccess).get(0);
        return conceptMapConverter.translate(conceptMap, sourceSystem, sourceVersion, sourceCode, targetSystem, publicAccess);
    }

}
