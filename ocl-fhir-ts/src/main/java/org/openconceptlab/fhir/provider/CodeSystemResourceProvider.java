package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

/**
 * The CodeSystemResourceProvider.
 * @author hp11
 */
@Component
public class CodeSystemResourceProvider implements IResourceProvider {

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CodeSystemConverter codeSystemConverter;

    @Autowired
    private OclFhirUtil oclFhirUtil;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    /**
     * Get a single {@link CodeSystem} for given {@link IdType}.
     * @param id {@link CodeSystem#id}
     * @return {@link CodeSystem}
     */
    @Read()
    @Transactional
    public CodeSystem get(@IdParam IdType id){
        List<CodeSystem> codeSystems = new ArrayList<>();
        List<Source> sources = getPublicSourceByMnemonic(new StringType(id.getIdPart()));
        codeSystemConverter.convertToCodeSystem(codeSystems, sources, true, null);
        if(codeSystems.size() >= 1) {
            return codeSystems.get(0);
        } else {
            throw new ResourceNotFoundException(id, oclFhirUtil.getNotFoundOutcome(id));
        }
    }

    /**
     * Returns all public {@link CodeSystem}.
     *
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle getCodeSystems(RequestDetails details) {
        List<CodeSystem> codeSystems = new ArrayList<CodeSystem>();
        List<Source> sources = getPublicSources();
        codeSystemConverter.convertToCodeSystem(codeSystems, sources, true, null);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns all public {@link CodeSystem} for a given publisher.
     * @param publisher
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle getCodeSystemsByPublisher(@RequiredParam(name = CodeSystem.SP_PUBLISHER) StringType publisher,
    		RequestDetails details) {
        validatePublisher(publisher.getValue());
        List<CodeSystem> codeSystems = new ArrayList<CodeSystem>();
        List<Source> sources = getPublicSourcesByPublisher(publisher.getValue());
        codeSystemConverter.convertToCodeSystem(codeSystems, sources, true, null);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Source> getPublicSourceByMnemonic(StringType id) {
        return sourceRepository.findByMnemonicAndPublicAccessIn(id.getValue(), publicAccess);
    }

    private List<Source> getPublicSources() {
        return sourceRepository.findByPublicAccessIn(publicAccess);
    }
    
    private List<Source> getPublicSourcesByPublisher(String publisher) {
        String owner = getOwner(publisher);
        String value = getPublisher(publisher);
        if(ORG.equals(owner)) {
            return sourceRepository.findByOrganizationMnemonic(value);
        } else {
            return sourceRepository.findByUserIdUsername(value);
        }
    }
}
