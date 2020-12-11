package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
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
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false, 0);
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
                                        @OptionalParam(name = PAGE) StringType page,
                                        RequestDetails details) {
        List<Source> sources = filterHead(getSourceByUrl(url, version, publicAccess));
        boolean includeConcepts = !isValid(version) || !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts,
                getPage(page));
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
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false, 0);
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
                                               @OptionalParam(name = PAGE) StringType page,
                                               RequestDetails details) {
        List<Source> sources = filterHead(getSourceByOwnerAndIdAndVersion(id, owner, version, publicAccess));
        boolean includeConcepts = !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts, getPage(page));
        return OclFhirUtil.getBundle(codeSystems, details.getFhirServerBase(), details.getRequestPath());
    }

    /**
     * CodeSystem $lookup operation.
     * GET request example:
     * <HOST>/fhir/CodeSystem/$lookup?code=#&system=#&version=#&displayLanguage=#
     *
     * POST request example:
     * {
     *     "resourceType":"Parameters",
     *     "parameter": [
     *          {
     *             "name":"system",
     *             "valueUri":""
     *         },
     *         {
     *             "name":"code",
     *             "valueCode":""
     *         },
     *         {
     *             "name":"version",
     *             "valueString":""
     *         },
     *         {
     *             "name":"displayLanguage",
     *             "valueCode":""
     *         }
     *     ]
     * }
     *
     * @param code - (Mandatory) Code that is to be located
     * @param system - (Mandatory) System for the code that is to be located
     * @param version - (Optional) The version of system
     * @param displayLanguage - (Optional) The display language
     * @return Parameters
     */
    @Operation(name = LOOKUP, idempotent = true)
    @Transactional
    public Parameters codeSystemLookUp(@OperationParam(name = CODE, type = CodeType.class, min = 1) CodeType code,
                                       @OperationParam(name = SYSTEM, type = UriType.class, min = 1) UriType system,
                                       @OperationParam(name = VERSION, type = StringType.class) StringType version,
                                       @OperationParam(name = DISP_LANG, type = CodeType.class) CodeType displayLanguage,
                                       @OperationParam(name = OWNER, type = StringType.class) StringType owner) {

        validateOperation(code, system, LOOKUP);
        Source source = isValid(owner) ? oclFhirUtil.getSourceByOwnerAndUrl(owner, newStringType(system), version, publicAccess) :
                getSourceByUrl(newStringType(system), version, publicAccess).get(0);
        return codeSystemConverter.getLookupParameters(source, code, displayLanguage);
    }

    @Operation(name = VALIDATE_CODE, idempotent = true)
    @Transactional
    public Parameters codeSystemValidateCode(@OperationParam(name = URL, type = UriType.class, min = 1) UriType url,
                                             @OperationParam(name = CODE, type = CodeType.class, min = 1) CodeType code,
                                             @OperationParam(name = VERSION, type = StringType.class) StringType version,
                                             @OperationParam(name = DISPLAY, type = StringType.class) StringType display,
                                             @OperationParam(name = DISP_LANG, type = CodeType.class) CodeType displayLanguage,
                                             @OperationParam(name = CODING, type = Coding.class) Coding coding,
                                             @OperationParam(name = OWNER, type = StringType.class) StringType owner) {

        if (coding != null) {
            url = new UriType(coding.getSystem());
            code = new CodeType(coding.getCode());
            version = new StringType(coding.getVersion());
            display = new StringType(coding.getDisplay());
        }
        validateOperation(code, url, VALIDATE_CODE);
        Source source = isValid(owner) ? oclFhirUtil.getSourceByOwnerAndUrl(owner, newStringType(url), version, publicAccess) :
                getSourceByUrl(newStringType(url), version, publicAccess).get(0);
        return codeSystemConverter.validateCode(source, getCode(code), display, displayLanguage);
    }

    private List<Source> getSources(List<String> access) {
        return sourceRepository.findByPublicAccessIn(access).parallelStream().filter(Source::getIsLatestVersion)
                .collect(Collectors.toList());
    }

    private List<Source> getSourceByUrl(StringType url, StringType version, List<String> access) {
        List<Source> sources = new ArrayList<>();
        if (isVersionAll(version)) {
            // get all versions
            sources.addAll(sourceRepository.findByCanonicalUrlAndPublicAccessIn(url.getValue(), access).stream()
                    .sorted(Comparator.comparing(Source::getVersion).reversed()).collect(Collectors.toList()));
        } else {
            final Source source;
            if (!isValid(version)) {
                // get most recent released version
                source = oclFhirUtil.getMostRecentReleasedSourceByUrl(url, access);
            } else {
                // get a given version
                source = sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(url.getValue(), version.getValue(), access);
            }
            if (source != null) sources.add(source);
        }
        if (sources.isEmpty())
            throw new ResourceNotFoundException(notFound(CodeSystem.class, url, version));
        return sources;
    }

    private List<Source> getSourceByOwner(StringType owner, List<String> access) {
        if (!isValid(owner))
            return new ArrayList<>();
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

    private List<Source> getSourceByOwnerAndIdAndVersion(StringType id, StringType owner, StringType version, List<String> access) {
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

    private void addSourceVersion(StringType id, StringType version, List<String> access, List<Source> sources, String ownerType, String ownerId) {
        final Source source = oclFhirUtil.getSourceVersion(id, version, access, ownerType, ownerId);
        if (source != null) sources.add(source);
    }

    private List<Source> filterHead(List<Source> sources) {
        return sources.stream().filter(s -> !HEAD.equals(s.getVersion())).collect(Collectors.toList());
    }

    private void validateOperation(CodeType code, UriType system, String operation) {
        if (!isValid(code) || !isValid(system)) {
            String msg = "Could not perform CodeSystem %s operation, both code and %s parameters are required.";
                throw new InvalidRequestException(String.format(msg, operation, LOOKUP.equals(operation) ? SYSTEM : URL));
        }
    }
}
