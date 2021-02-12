package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
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
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;

import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

/**
 * The CodeSystemResourceProvider.
 * @author harpatel1
 */
@Component
public class CodeSystemResourceProvider extends BaseProvider implements IResourceProvider {

    private static final Log log = LogFactory.getLog(CodeSystemResourceProvider.class);

    public CodeSystemResourceProvider(SourceRepository sourceRepository, CodeSystemConverter codeSystemConverter,
                                      CollectionRepository collectionRepository, ValueSetConverter valueSetConverter,
                                      ConceptMapConverter conceptMapConverter, OclFhirUtil oclFhirUtil) {
        super(sourceRepository, codeSystemConverter, collectionRepository, valueSetConverter, conceptMapConverter, oclFhirUtil);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    @Create
    @Transactional
    public MethodOutcome createCodeSystem(@ResourceParam CodeSystem codeSystem, RequestDetails requestDetails) {
        if (codeSystem == null) {
            throw new InvalidRequestException("The CodeSystem can not be empty");
        }
        String accessionId = getAccessionIdentifier(codeSystem.getIdentifier());
        if (!isValid(accessionId)) {
            throw new InvalidRequestException("The CodeSystem.identifier is empty or identifier of type ACSN is empty.");
        }
        if (!isValid(codeSystem.getUrl())) {
            throw new InvalidRequestException("The CodeSystem.url can not be empty. Please provide canonical url.");
        }
        codeSystemConverter.createCodeSystem(codeSystem, accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    @Update
    @Transactional
    public MethodOutcome updateCodeSystem(@IdParam IdType idType,
                                          @ResourceParam CodeSystem codeSystem,
                                          RequestDetails requestDetails) {
        if (codeSystem == null) {
            throw new InvalidRequestException("The CodeSystem can not be empty");
        }
        if (idType == null || !isValid(idType.getIdPart()) || !isValid(idType.getVersionIdPart()) ||
                isVersionAll(newStringType(idType.getVersionIdPart()))) {
            throw new InvalidRequestException("Invalid CodeSystem.id or CodeSystem.version provided. Both parameters are required.");
        }
        StringType owner = newStringType(requestDetails.getHeader(OWNER));
        if (!isValid(owner))
            throw new InvalidRequestException("Owner can not be empty.");
        List<Source> sources = filterSourceHead(
                getSourceByOwnerAndIdAndVersion(idType.getIdPart(), owner.getValue(), idType.getVersionIdPart(), publicAccess));
        if (sources.isEmpty()) {
            throw new InvalidRequestException("CodeSystem is not found.");
        }
        String accessionId = buildAccessionId(idType, owner);
        codeSystemConverter.updateCodeSystem(codeSystem, sources.get(0), accessionId, requestDetails.getHeader(AUTHORIZATION));
        return new MethodOutcome();
    }

    private String buildAccessionId(IdType idType, StringType owner) {
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        return FS + (ORG.equals(ownerType) ? ORGS : USERS) +
                FS + value +
                FS + CODESYSTEM +
                FS + idType.getIdPart() +
                FS + (isValid(idType.getVersionIdPart()) ? idType.getVersionIdPart() + FS : EMPTY);
    }

    @Delete
    @Transactional
    public void deleteCodeSystem(@IdParam(optional = true) IdType idType,
                                 @RequiredParam(name = ID) StringType id,
                                 @RequiredParam(name = VERSION) StringType version,
                                 @RequiredParam(name = OWNER) StringType owner,
                                 RequestDetails requestDetails) {
        codeSystemConverter.retireCodeSystem(
                requestDetails.getHeader(ID),
                requestDetails.getHeader(VERSION),
                newStringType(requestDetails.getHeader(OWNER)),
                requestDetails.getHeader(AUTHORIZATION)
        );
    }

    /**
     * Returns all public {@link CodeSystem}.
     *
     * @return {@link Bundle}
     */
    @Search()
    @Transactional
    public Bundle searchCodeSystems(@OptionalParam(name = PAGE) StringType page, RequestDetails details) {
        List<Source> sources = filterSourceHead(getSources(publicAccess));
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false, getPage(page));
        log.info("Found " + codeSystems.size() + " CodeSystems.");
        return OclFhirUtil.getBundle(codeSystems, details.getCompleteUrl(), details.getRequestPath());
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
        List<Source> sources = filterSourceHead(getSourceByUrl(url, version, publicAccess));
        boolean includeConcepts = !isValid(version) || !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts,
                getPage(page));
        log.info("Found " + codeSystems.size() + " CodeSystems.");
        return OclFhirUtil.getBundle(codeSystems, details.getCompleteUrl(), details.getRequestPath());
    }

    /**
     * Returns all public {@link CodeSystem} for a given owner.
     * @param owner
     * @return {@link Bundle}
     */
    @Search
    @Transactional
    public Bundle searchCodeSystemByOwner(@RequiredParam(name = OWNER) StringType owner,
                                          @OptionalParam(name = PAGE) StringType page,
                                          RequestDetails details) {
        List<Source> sources = filterSourceHead(getSourceByOwner(owner, publicAccess));
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, false, getPage(page));
        log.info("Found " + codeSystems.size() + " CodeSystems.");
        return OclFhirUtil.getBundle(codeSystems, details.getCompleteUrl(), details.getRequestPath());
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
        List<Source> sources = filterSourceHead(getSourceByOwnerAndIdAndVersion(id, owner, version, publicAccess));
        boolean includeConcepts = !isVersionAll(version);
        List<CodeSystem> codeSystems = codeSystemConverter.convertToCodeSystem(sources, includeConcepts, getPage(page));
        log.info("Found " + codeSystems.size() + " CodeSystems.");
        return OclFhirUtil.getBundle(codeSystems, details.getCompleteUrl(), details.getRequestPath());
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

    private void validateOperation(CodeType code, UriType system, String operation) {
        if (!isValid(code) || !isValid(system)) {
            String msg = "Could not perform CodeSystem %s operation, both code and %s parameters are required.";
                throw new InvalidRequestException(String.format(msg, operation, LOOKUP.equals(operation) ? SYSTEM : URL));
        }
    }
}
