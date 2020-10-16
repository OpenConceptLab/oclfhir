package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The CodeSystemResourceProvider.
 * @author harpatel1
 */
@Component
public class CodeSystemResourceProvider implements IResourceProvider {

    private SourceRepository sourceRepository;
    private CodeSystemConverter codeSystemConverter;
    private OclFhirUtil oclFhirUtil;

    @Autowired
    public CodeSystemResourceProvider(SourceRepository sourceRepository, CodeSystemConverter codeSystemConverter, OclFhirUtil oclFhirUtil) {
        this.sourceRepository = sourceRepository;
        this.codeSystemConverter = codeSystemConverter;
        this.oclFhirUtil = oclFhirUtil;
    }

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
        List<Source> sources = getPublicSourceByMnemonic(new StringType(id.getIdPart()));
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, true, null);
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
    public Bundle searchCodeSystems(RequestDetails details) {
        List<Source> sources = getPublicSources();
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false, null);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link CodeSystem} for a given Url.
     * @param url
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchCodeSystemByUrl(@RequiredParam(name = CodeSystem.SP_URL) StringType url, RequestDetails details) {
        List<Source> sources = getPublicSourceByUrl(url);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, true, null);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    @Search
    @Transactional
    public Bundle searchCodeSystemByOwner(@RequiredParam(name = "owner") StringType owner, @OptionalParam(name = "id") StringType id
            , RequestDetails details) {
        List<Source> sources = getPublicSourceByIdOrOwner(id, owner, sourceRepository);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, isValid(id), null);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Source> getPublicSourceByMnemonic(StringType id) {
        return sourceRepository.findByMnemonicAndPublicAccessIn(id.getValue(), publicAccess)
                .parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    private List<Source> getPublicSources() {
        return sourceRepository.findByPublicAccessIn(publicAccess).parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    private List<Source> getPublicSourceByUrl(StringType url) {
        return sourceRepository.findByExternalIdAndPublicAccessIn(url.getValue(), publicAccess)
                .parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }

    public List<Source> getPublicSourceByIdOrOwner(StringType id, StringType owner, SourceRepository repository) {
        List<Source> sources = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (ORG.equals(ownerType)) {
            if (isValid(id)) {
                sources.addAll(repository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(), value, publicAccess));
            } else {
                sources.addAll(repository.findByOrganizationMnemonicAndPublicAccessIn(value, publicAccess));
            }
        } else {
            if (isValid(id)) {
                sources.addAll(repository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(), value, publicAccess));
            } else {
                sources.addAll(repository.findByUserIdUsernameAndPublicAccessIn(value, publicAccess));
            }
        }
        return sources.parallelStream().filter(s -> "HEAD".equals(s.getVersion()))
                .collect(Collectors.toList());
    }
}
