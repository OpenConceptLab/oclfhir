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
import static org.openconceptlab.fhir.util.OclFhirConstants.FS;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.badRequest;

@RestController
@RequestMapping({"/users/{user}/CodeSystem"})
public class OclFhirUserCodeSystemController extends BaseOclFhirController {

    public OclFhirUserCodeSystemController(CodeSystemResourceProvider codeSystemResourceProvider,
                                           ValueSetResourceProvider valueSetResourceProvider,
                                           OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link CodeSystem}
     *
     * @param user       - the username
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @PostMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystemForUser(@PathVariable(name = USER) String user,
                                                          @RequestBody String codeSystem,
                                                          @RequestHeader(name = AUTHORIZATION) String auth) {
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(system.getIdElement().getIdPart(), acsnOpt, USERS, user);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(
                system.getIdentifier(), USERS, user, CODESYSTEM, system.getIdElement().getIdPart(), system.getVersion());

        performCreate(system, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link CodeSystem} version.
     *
     * @param id         - the {@link CodeSystem} id
     * @param version    - the {@link CodeSystem} version
     * @param user       - the username
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateCodeSystemForUser(@PathVariable(name = ID) String id,
                                                          @PathVariable(name = VERSION) String version,
                                                          @PathVariable(name = USER) String user,
                                                          @RequestBody String codeSystem,
                                                          @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, USER, user))
            return badRequest("The CodeSystem can not be edited.");
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        IdType idType = new IdType(CODESYSTEM, id, version);
        performUpdate(system, auth, idType, formatUser(user));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link CodeSystem} version.
     *
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param user    - the username
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByUser(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = USER) String user,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, USER, user))
            return badRequest("The CodeSystem can not be deleted.");
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + SOURCES + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Delete concepts from {@link CodeSystem} version.
     *
     * @param id        - the {@link CodeSystem} id
     * @param version   - the {@link CodeSystem} version
     * @param conceptId - the concept code
     * @param user      - the username
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInCodeSystemForUser(@PathVariable(name = ID) String id,
                                                                   @PathVariable(name = VERSION) String version,
                                                                   @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                   @PathVariable(name = USER) String user,
                                                                   @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + SOURCES + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Read {@link CodeSystem} by Id.
     *
     * @param user - the username
     * @param id   - the {@link CodeSystem} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user,
                                                      @PathVariable String id,
                                                      @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                      HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link CodeSystem} version.
     *
     * @param user    - the username
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version,
                                                              @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                              HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link CodeSystem} of user.
     *
     * @param user - the username
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUser(@PathVariable String user,
                                                          @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                          HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param user            - the username
     * @param system          - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user,
                                                          @RequestParam(name = SYSTEM) String system,
                                                          @RequestParam(name = CODE) String code,
                                                          @RequestParam(name = VERSION, required = false) String version,
                                                          @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param user       - the username
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param user            - the username
     * @param url             - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param display         - the concept display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user,
                                                            @RequestParam(name = URL) String url,
                                                            @RequestParam(name = CODE) String code,
                                                            @RequestParam(name = VERSION, required = false) String version,
                                                            @RequestParam(name = DISPLAY, required = false) String display,
                                                            @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param user       - the username
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

}
