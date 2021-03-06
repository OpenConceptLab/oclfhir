package org.openconceptlab.fhir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@Tag(name = VALUE_SET_USER_NAMESPACE, description = "user namespace")
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
    @Operation(summary = CREATE_VALUESET)
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                        @RequestBody @Parameter(description = THE_VALUESET_JSON_RESOURCE) String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(summary = UPDATE_VALUESET_VERSION)
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateValueSetForUser(@PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                        @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String version,
                                                        @PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                        @RequestBody @Parameter(description = THE_VALUESET_JSON_RESOURCE) String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(summary = DELETE_VALUESET_VERSION)
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteValueSetByUser(@PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                       @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String version,
                                                       @PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                       @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth,
                                                       @RequestParam(name = "force", required = false, defaultValue = "false") @Parameter(hidden = true) boolean force) {
        Collection collection = oclFhirUtil.getCollectionVersion(id, version, publicAccess, USER, user);
        if (collection == null) return ResponseEntity.notFound().build();
        if (oclFhirUtil.isOclAdmin(auth) && force) {
            collectionRepository.delete(collection);
            return ResponseEntity.noContent().build();
        } else {
            String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + COLLECTIONS + FS + id + FS + version + FS;
            return performDeleteOclApi(url, auth);
        }
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
    @Operation(hidden = true)
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInValueSetForUser(@PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                                 @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String version,
                                                                 @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                 @PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                                 @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(summary = GET_VALUESET_BY_USER_AND_ID)
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                    @PathVariable @Parameter(description = THE_VALUESET_ID) String id,
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
    @Operation(summary = GET_SEARCH_VALUESET_VERSIONS)
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                            @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                            @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION, allowEmptyValue = true) Optional<String> version,
                                                            @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                            @RequestParam(name = ValueSet.SP_STATUS) Optional<String> status,
                                                            @RequestParam(name = ValueSet.SP_PUBLISHER) Optional<String> publisher,
                                                            HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request),
                ValueSet.SP_STATUS, status.orElse(EMPTY),
                ValueSet.SP_PUBLISHER, URLDecoder.decode(publisher.orElse(EMPTY), StandardCharsets.UTF_8));
    }

    /**
     * Read all {@link ValueSet} of user.
     *
     * @param user - the username
     * @param page - the page number
     * @return ResponseEntity
     */
    @Operation(summary = SEARCH_VALUESETS_FOR_USER)
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable @Parameter(description = THE_USERNAME) String user,
                                                        @RequestParam(name = ValueSet.SP_STATUS) Optional<String> status,
                                                        @RequestParam(name = ValueSet.SP_PUBLISHER) Optional<String> publisher,
                                                        @RequestParam(name = ValueSet.SP_VERSION) Optional<String> version,
                                                        @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                        HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request),
                ValueSet.SP_STATUS, status.orElse(EMPTY),
                ValueSet.SP_PUBLISHER, URLDecoder.decode(publisher.orElse(EMPTY), StandardCharsets.UTF_8),
                ValueSet.SP_VERSION, URLDecoder.decode(version.orElse(EMPTY), StandardCharsets.UTF_8));
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
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                         @RequestParam(name = URL) @Parameter(description = THE_VALUESET_URL) String url,
                                                         @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
                                                         @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                         @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
                                                         @RequestParam(name = SYSTEM_VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String systemVersion,
                                                         @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                         @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example2", value = VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                         @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
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
    @Operation(summary = PERFORM_EXPAND_BY_URL)
    @GetMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                       @RequestParam(name = URL) @Parameter(description = THE_VALUESET_URL) String url,
                                                       @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
                                                       @RequestParam(name = OFFSET, required = false, defaultValue = "0") @Parameter(description = STARTING_INDEX_IF_SUBSET_IS_DESIRED) Integer offset,
                                                       @RequestParam(name = COUNT, required = false, defaultValue = "100") @Parameter(description = NUMBER_OF_CODES_TO_BE_RETURNED) Integer count,
                                                       @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") @Parameter(description = INCLUDE_CONCEPT_DESIGNATIONS) Boolean includeDesignations,
                                                       @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") @Parameter(description = INCLUDE_VALUESET_DEFINITION) Boolean includeDefinition,
                                                       @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") @Parameter(description = ONLY_INCLUDE_ACTIVE_CONCEPTS) Boolean activeOnly,
                                                       @RequestParam(name = DISPLAY_LANGUAGE, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_VALUE_SET_EXPANSION_CONTAINS_DISPLAY) String displayLanguage,
                                                       @RequestParam(name = FILTER, required = false) @Parameter(description = VALUESET_EXPAND_FILTER_TEXT) String filter) {
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = VALUESET_EXPAND_REQ_BODY_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_EXPAND_BY_URL)
    @PostMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                       @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user            - the username
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
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @GetMapping(path = {"/{id}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndId(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                              @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                              @RequestParam(name = URL, required = false) @Parameter(description = THE_VALUESET_URL) String url,
                                                              @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
                                                              @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                              @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
                                                              @RequestParam(name = SYSTEM_VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String systemVersion,
                                                              @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                              @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user            - the username
     * @param id              - the {@link ValueSet} id
     * @param url             - the {@link ValueSet} url
     * @param pathVersion     - the {@link ValueSet} version
     * @param code            - the concept code
     * @param system          - the {@link CodeSystem} url
     * @param systemVersion   - the {@link CodeSystem} version
     * @param display         - the display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @GetMapping(path = {"/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndIdAndVersion(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                                        @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                                        @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String pathVersion,
                                                                        @RequestParam(name = URL, required = false) @Parameter(description = THE_VALUESET_URL) String url,
                                                                        @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                                        @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
                                                                        @RequestParam(name = SYSTEM_VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String systemVersion,
                                                                        @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                                        @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, pathVersion, code, system, systemVersion, display,
                displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user       - the username
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = VALUESET_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE1),
                    @ExampleObject(name = "example2", value = VALUESET_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @PostMapping(path = {"/{id}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndId(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                              @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                              @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param user       - the username
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = VALUESET_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE1),
                    @ExampleObject(name = "example2", value = VALUESET_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @PostMapping(path = {"/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUserAndIdAndVersion(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                                        @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                                        @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String pathVersion,
                                                                        @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user                - the username
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
    @Operation(summary = PERFORM_EXPAND_BY_ID)
    @GetMapping(path = {"/{id}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndId(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                            @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                            @RequestParam(name = URL, required = false) @Parameter(description = THE_VALUESET_URL) String url,
                                                            @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
                                                            @RequestParam(name = OFFSET, required = false, defaultValue = "0") @Parameter(description = STARTING_INDEX_IF_SUBSET_IS_DESIRED) Integer offset,
                                                            @RequestParam(name = COUNT, required = false, defaultValue = "100") @Parameter(description = NUMBER_OF_CODES_TO_BE_RETURNED) Integer count,
                                                            @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") @Parameter(description = INCLUDE_CONCEPT_DESIGNATIONS) Boolean includeDesignations,
                                                            @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") @Parameter(description = INCLUDE_VALUESET_DEFINITION) Boolean includeDefinition,
                                                            @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") @Parameter(description = ONLY_INCLUDE_ACTIVE_CONCEPTS) Boolean activeOnly,
                                                            @RequestParam(name = DISPLAY_LANGUAGE, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_VALUE_SET_EXPANSION_CONTAINS_DISPLAY) String displayLanguage,
                                                            @RequestParam(name = FILTER, required = false) @Parameter(description = VALUESET_EXPAND_FILTER_TEXT) String filter) {
        Parameters parameters = valueSetExpandParameters(url, valueSetVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user                - the username
     * @param id                  - the {@link ValueSet} id
     * @param url                 - the {@link ValueSet} url
     * @param pathVersion         - the {@link ValueSet} version
     * @param offset              - the offset (for partial output)
     * @param count               - the count (for partial output)
     * @param includeDesignations - flag to include/exclude designations
     * @param includeDefinition   - flag to include/exclude definition
     * @param activeOnly          - flag to include/exclude active concepts
     * @param displayLanguage     - the display language
     * @param filter              - the concept code filter
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_EXPAND_BY_ID)
    @GetMapping(path = {"/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndIdAndVersion(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                                      @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                                      @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String pathVersion,
                                                                      @RequestParam(name = URL, required = false) @Parameter(description = THE_VALUESET_URL) String url,
                                                                      @RequestParam(name = OFFSET, required = false, defaultValue = "0") @Parameter(description = STARTING_INDEX_IF_SUBSET_IS_DESIRED) Integer offset,
                                                                      @RequestParam(name = COUNT, required = false, defaultValue = "100") @Parameter(description = NUMBER_OF_CODES_TO_BE_RETURNED) Integer count,
                                                                      @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") @Parameter(description = INCLUDE_CONCEPT_DESIGNATIONS) Boolean includeDesignations,
                                                                      @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") @Parameter(description = INCLUDE_VALUESET_DEFINITION) Boolean includeDefinition,
                                                                      @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") @Parameter(description = ONLY_INCLUDE_ACTIVE_CONCEPTS) Boolean activeOnly,
                                                                      @RequestParam(name = DISPLAY_LANGUAGE, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_VALUE_SET_EXPANSION_CONTAINS_DISPLAY) String displayLanguage,
                                                                      @RequestParam(name = FILTER, required = false) @Parameter(description = VALUESET_EXPAND_FILTER_TEXT) String filter) {
        Parameters parameters = valueSetExpandParameters(url, pathVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatUser(user));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user       - the username
     * @param id         - the {@link ValueSet} id
     * @param parameters - the input paramters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = VALUESET_EXPAND_REQ_BODY_ID_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_EXPAND_BY_ID)
    @PostMapping(path = {"/{id}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndId(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                            @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                            @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param user       - the username
     * @param id         - the {@link ValueSet} id
     * @param parameters - the input paramters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = VALUESET_EXPAND_REQ_BODY_ID_VERSION_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_EXPAND_BY_ID)
    @PostMapping(path = {"/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUserAndIdAndVersion(@PathVariable(name = USER) @Parameter(description = THE_USERNAME) String user,
                                                                      @PathVariable(name = ID) @Parameter(description = THE_VALUESET_ID) String id,
                                                                      @PathVariable(name = VERSION) @Parameter(description = THE_VALUESET_VERSION) String pathVersion,
                                                                      @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, EXPAND, id);
    }

}


