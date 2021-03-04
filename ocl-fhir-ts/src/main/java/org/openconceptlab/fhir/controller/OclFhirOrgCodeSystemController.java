package org.openconceptlab.fhir.controller;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.VALIDATE_CODE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@RestController
@RequestMapping({"/orgs/{org}/CodeSystem"})
public class OclFhirOrgCodeSystemController extends BaseOclFhirController {

    public OclFhirOrgCodeSystemController(CodeSystemResourceProvider codeSystemResourceProvider,
                                          ValueSetResourceProvider valueSetResourceProvider,
                                          OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link CodeSystem}
     *
     * @param org        - the organization id
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @PostMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystemForOrg(@PathVariable(name = ORG) String org,
                                                         @RequestBody String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(system.getIdElement().getIdPart(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(
                system.getIdentifier(), ORGS, org, CODESYSTEM, system.getIdElement().getIdPart(), system.getVersion());

        performCreate(system, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link CodeSystem} version.
     *
     * @param id         - the {@link CodeSystem} id
     * @param version    - the {@link CodeSystem} version
     * @param org        - the organization id
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateCodeSystemForOrg(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = ORG) String org,
                                                         @RequestBody String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, ORG, org))
            return badRequest("The CodeSystem can not be edited.");
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        IdType idType = new IdType(CODESYSTEM, id, version);
        performUpdate(system, auth, idType, formatOrg(org));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link CodeSystem} version.
     *
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param org     - the organization id
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByOrg(@PathVariable(name = ID) String id,
                                                        @PathVariable(name = VERSION) String version,
                                                        @PathVariable(name = ORG) String org,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, ORG, org))
            return badRequest("The CodeSystem can not be deleted.");
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Delete concepts from {@link CodeSystem} version.
     *
     * @param id        - the {@link CodeSystem} id
     * @param version   - the {@link CodeSystem} version
     * @param conceptId - the concept code
     * @param org       - the organization id
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInCodeSystemForOrg(@PathVariable(name = ID) String id,
                                                                  @PathVariable(name = VERSION) String version,
                                                                  @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                  @PathVariable(name = ORG) String org,
                                                                  @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Read {@link CodeSystem} by Id.
     *
     * @param org  - the organization id
     * @param id   - the {@link CodeSystem} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable String org,
                                                     @PathVariable String id,
                                                     @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                     HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link CodeSystem} version.
     *
     * @param org     - the organization id
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                             HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link CodeSystem} of org.
     *
     * @param org  - the organization id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByOrg(@PathVariable String org,
                                                         @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                         HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org             - the organization id
     * @param system          - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org,
                                                         @RequestParam(name = SYSTEM) String system,
                                                         @RequestParam(name = CODE) String code,
                                                         @RequestParam(name = VERSION, required = false) String version,
                                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org             - the organization id
     * @param url             - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param display         - the concept display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

}


