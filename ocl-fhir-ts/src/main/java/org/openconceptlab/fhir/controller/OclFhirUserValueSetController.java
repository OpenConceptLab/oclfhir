package org.openconceptlab.fhir.controller;

import org.hl7.fhir.r4.model.*;
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
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@RestController
@RequestMapping({"/users/{user}/ValueSet"})
public class OclFhirUserValueSetController extends BaseOclFhirController {

    public OclFhirUserValueSetController(CodeSystemResourceProvider codeSystemResourceProvider,
                                         ValueSetResourceProvider valueSetResourceProvider,
                                         OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link ValueSet}
     *
     * @param user     - the username
     * @param valueSet - the {@link ValueSet} resource
     * @return ResponseEntity
     */
    @PostMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForUser(@PathVariable(name = USER) String user,
                                                        @RequestBody String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(set.getIdentifier());
        ResponseEntity<String> response = validate(set.getIdElement().getIdPart(), acsnOpt, USERS, user);
        if (response != null) return response;
        if (acsnOpt.isEmpty())
            addIdentifier(set.getIdentifier(), USERS, user, VALUESET, set.getIdElement().getIdPart(), set.getVersion());

        performCreate(set, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link ValueSet} version.
     *
     * @param id       - the {@link ValueSet} id
     * @param version  - the {@link ValueSet} version
     * @param user     - the username
     * @param valueSet - the {@link ValueSet} resource
     * @return ResponseEntity
     */
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateValueSetForUser(@PathVariable(name = ID) String id,
                                                        @PathVariable(name = VERSION) String version,
                                                        @PathVariable(name = USER) String user,
                                                        @RequestBody String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        IdType idType = new IdType(VALUESET, id, version);
        performUpdate(set, auth, idType, formatUser(user));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link ValueSet} version.
     *
     * @param id      - the {@link ValueSet} id
     * @param version - the {@link ValueSet} version
     * @param user    - the username
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteValueSetByUser(@PathVariable(name = ID) String id,
                                                       @PathVariable(name = VERSION) String version,
                                                       @PathVariable(name = USER) String user,
                                                       @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + COLLECTIONS + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Delete concepts from {@link ValueSet} version.
     *
     * @param id        - the {@link ValueSet} id
     * @param version   - the {@link ValueSet} version
     * @param conceptId - the concept code
     * @param user      - the username
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInValueSetForUser(@PathVariable(name = ID) String id,
                                                                 @PathVariable(name = VERSION) String version,
                                                                 @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                 @PathVariable(name = USER) String user,
                                                                 @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + COLLECTIONS + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Read {@link ValueSet} by Id.
     *
     * @param user - the username
     * @param id   - the {@link ValueSet} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user,
                                                    @PathVariable String id,
                                                    @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                    HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link ValueSet} version.
     *
     * @param user    - the username
     * @param id      - the {@link ValueSet} id
     * @param version - the {@link ValueSet} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version,
                                                            @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                            HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link ValueSet} of user.
     *
     * @param user - the username
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user,
                                                        @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                        HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user            - the username
     * @param url             - the {@link ValueSet} url
     * @param valueSetVersion - the {@link ValueSet} version
     * @param code            - the concept code
     * @param system          - the {@link CodeSystem} url
     * @param systemVersion   - the {@link CodeSystem} version
     * @param display         - the display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable String user,
                                                         @RequestParam(name = URL) String url,
                                                         @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                         @RequestParam(name = CODE) String code,
                                                         @RequestParam(name = SYSTEM) String system,
                                                         @RequestParam(name = SYSTEM_VERSION, required = false) String systemVersion,
                                                         @RequestParam(name = DISPLAY, required = false) String display,
                                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user       - the username
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user                - the username
     * @param url                 - the {@link ValueSet} url
     * @param valueSetVersion     - the {@link ValueSet} version
     * @param offset              - the offset (for partial output)
     * @param count               - the count (for partial output)
     * @param includeDesignations - flag to include/exclude designations
     * @param includeDefinition   - flag to include/exclude definition
     * @param activeOnly          - flag to include/exclude active concepts
     * @param displayLanguage     - the display language
     * @param filter              - the concept code filter
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable String user,
                                                       @RequestParam(name = URL) String url,
                                                       @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                       @RequestParam(name = OFFSET, required = false, defaultValue = "0") Integer offset,
                                                       @RequestParam(name = COUNT, required = false, defaultValue = "100") Integer count,
                                                       @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") Boolean includeDesignations,
                                                       @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") Boolean includeDefinition,
                                                       @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") Boolean activeOnly,
                                                       @RequestParam(name = DISPLAY_LANGUAGE, required = false) String displayLanguage,
                                                       @RequestParam(name = FILTER, required = false) String filter) {
        Parameters parameters = valueSetExpandParameters(url, valueSetVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user       - the username
     * @param parameters - the input paramters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user             - the username
     * @param id              - the {@link ValueSet} id
     * @param url             - the {@link ValueSet} url
     * @param valueSetVersion - the {@link ValueSet} version
     * @param code            - the concept code
     * @param system          - the {@link CodeSystem} url
     * @param systemVersion   - the {@link CodeSystem} version
     * @param display         - the display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/$validate-code", "/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndId(@PathVariable String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION, required = false) String pathVersion,
                                                              @RequestParam(name = URL, required = false) String url,
                                                              @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                              @RequestParam(name = CODE) String code,
                                                              @RequestParam(name = SYSTEM) String system,
                                                              @RequestParam(name = SYSTEM_VERSION, required = false) String systemVersion,
                                                              @RequestParam(name = DISPLAY, required = false) String display,
                                                              @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, isValid(pathVersion) ? pathVersion : valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user        - the username
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/{id}/$validate-code", "/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndId(@PathVariable String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION, required = false) String pathVersion,
                                                              @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user                 - the username
     * @param id                  - the {@link ValueSet} id
     * @param url                 - the {@link ValueSet} url
     * @param valueSetVersion     - the {@link ValueSet} version
     * @param offset              - the offset (for partial output)
     * @param count               - the count (for partial output)
     * @param includeDesignations - flag to include/exclude designations
     * @param includeDefinition   - flag to include/exclude definition
     * @param activeOnly          - flag to include/exclude active concepts
     * @param displayLanguage     - the display language
     * @param filter              - the concept code filter
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/$expand", "/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndId(@PathVariable String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION, required = false) String pathVersion,
                                                            @RequestParam(name = URL, required = false) String url,
                                                            @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                            @RequestParam(name = OFFSET, required = false, defaultValue = "0") Integer offset,
                                                            @RequestParam(name = COUNT, required = false, defaultValue = "100") Integer count,
                                                            @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") Boolean includeDesignations,
                                                            @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") Boolean includeDefinition,
                                                            @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") Boolean activeOnly,
                                                            @RequestParam(name = DISPLAY_LANGUAGE, required = false) String displayLanguage,
                                                            @RequestParam(name = FILTER, required = false) String filter) {
        Parameters parameters = valueSetExpandParameters(url, isValid(pathVersion) ? pathVersion : valueSetVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user        - the username
     * @param id         - the {@link ValueSet} id
     * @param parameters - the input paramters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/{id}/$expand", "/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndId(@PathVariable String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION, required = false) String pathVersion,
                                                            @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, EXPAND, id);
    }

}


