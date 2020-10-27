package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
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

    SourceRepository sourceRepository;
    CodeSystemConverter codeSystemConverter;
    OclFhirUtil oclFhirUtil;

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
     * Returns all public {@link CodeSystem}.
     *
     * @return {@link Bundle}
     */
    @Search()
    @Transactional
    public Bundle searchCodeSystems(RequestDetails details) {
        List<Source> sources = filterHead(getSources(publicAccess));
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link CodeSystem} for a given Url.
     * @param url
     * @param version
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchCodeSystemByUrl(@RequiredParam(name = CodeSystem.SP_URL) StringType url,
                                        @OptionalParam(name = VERSION) StringType version,
                                        RequestDetails details) {
        List<Source> sources = filterHead(getSourceByUrl(url, version, publicAccess));
        boolean includeConcepts = !isValid(version) || !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns all public {@link CodeSystem} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchCodeSystemByOwner(@RequiredParam(name = OWNER) StringType owner,
                                          RequestDetails details) {
        List<Source> sources = filterHead(getSourceByOwner(owner, publicAccess));
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * Returns public {@link CodeSystem} for a given owner and Id. Returns given version if provided, otherwise
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
                                               RequestDetails details) {
        List<Source> sources = filterHead(getSourceByOwnerAndId(id, owner, version, publicAccess));
        boolean includeConcepts = !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts);
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    private List<Source> getSources(List<String> access) {
        return sourceRepository.findByPublicAccessIn(access).parallelStream().filter(Source::getIsLatestVersion)
                .collect(Collectors.toList());
    }

    private List<Source> getSourceByUrl(StringType url, StringType version, List<String> access) {
        List<Source> sources = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            sources.addAll(sourceRepository.findByExternalIdAndPublicAccessIn(url.getValue(), access));
        } else {
            final Source source;
            if (!isValid(version)) {
                // get most recent released version
                source = getMostRecentReleasedSourceByUrl(url, access);
            } else {
                // get a given version
                source = sourceRepository.findFirstByExternalIdAndVersionAndPublicAccessIn(url.getValue(), version.getValue(), access);
            }
            if (source != null) sources.add(source);
        }
        if (sources.isEmpty())
            throw new ResourceNotFoundException(notFound(CodeSystem.class, url, version));
        return sources;
    }

    private List<Source> getSourceByOwner(StringType owner, List<String> access) {
        List<Source> sources = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (ORG.equals(ownerType)) {
            sources.addAll(sourceRepository.findByOrganizationMnemonicAndPublicAccessIn(value, access));
        } else {
            sources.addAll(sourceRepository.findByUserIdUsernameAndPublicAccessIn(value, access));
        }
        return sources.parallelStream().filter(Source::getIsLatestVersion).collect(Collectors.toList());
    }

    private List<Source> getSourceByOwnerAndId(StringType id, StringType owner, StringType version, List<String> access) {
        List<Source> sources = new ArrayList<>();
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());

        if (isVersionAll(version)) {
            // get all versions
            if (ORG.equals(ownerType)) {
                sources.addAll(sourceRepository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(id.getValue(),
                        value, access));
            } else {
                sources.addAll(sourceRepository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(id.getValue(),
                        value, access));
            }
        } else {
            addSourceVersion(id, version, access, sources, ownerType, value);
        }
        if (sources.isEmpty())
            throw new ResourceNotFoundException(notFound(CodeSystem.class, owner, id, version));
        return sources;
    }

    private void addSourceVersion(StringType id, StringType version, List<String> access, List<Source> sources, String ownerType, String value) {
        final Source source = oclFhirUtil.getSourceVersion(id, version, access, ownerType, value);
        if (source != null) sources.add(source);
    }

    private Source getMostRecentReleasedSourceByUrl(StringType url, List<String> access) {
        return sourceRepository.findFirstByExternalIdAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
          url.getValue(), true, access
        );
    }

    private List<Source> filterHead(List<Source> sources) {
        return sources.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }
}
