package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.StringType;
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
    public Bundle searchConceptMaps(RequestDetails details) {
        List<Source> sources = filterSourceHead(getSources(publicAccess));
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false, 0);
        return OclFhirUtil.getBundle(conceptMaps, details.getFhirServerBase(), details.getRequestPath());
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
        return OclFhirUtil.getBundle(conceptMaps, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns all public {@link ConceptMap} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchConceptMapByOwner(@RequiredParam(name = OWNER) StringType owner,
                                          RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwner(owner, publicAccess));
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, false, 0);
        return OclFhirUtil.getBundle(conceptMaps, details.getFhirServerBase(), details.getRequestPath());
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
    public Bundle searchCodeSystemByOwnerAndId(@RequiredParam(name = OWNER) StringType owner,
                                               @RequiredParam(name = ID) StringType id,
                                               @OptionalParam(name = VERSION) StringType version,
                                               @OptionalParam(name = PAGE) StringType page,
                                               RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwnerAndIdAndVersion(id, owner, version, publicAccess));
        boolean includeMappings = !isVersionAll(version);
        List<ConceptMap> conceptMaps = conceptMapConverter.convertToConceptMap(sources, includeMappings, getPage(page));
        return OclFhirUtil.getBundle(conceptMaps, details.getFhirServerBase(), details.getRequestPath());
    }

}
