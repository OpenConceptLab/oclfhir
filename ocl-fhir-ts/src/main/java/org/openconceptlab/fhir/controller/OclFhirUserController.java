package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.CODESYSTEM;

@RestController
@RequestMapping({"/users"})
public class OclFhirUserController extends BaseOclFhirController{

    public OclFhirUserController(CodeSystemResourceProvider codeSystemResourceProvider,
                                ValueSetResourceProvider valueSetResourceProvider,
                                OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    @PostMapping(path = {"/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystemForUser(@PathVariable(name = USER) String user,
                                                          @RequestBody String codeSystem,
                                                          @RequestHeader(name = AUTHORIZATION) String auth) {
        try {
            CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
            Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
            ResponseEntity<String> response = validate(user, system.getId(), acsnOpt, USERS, user);
            if (response != null) return response;
            if (acsnOpt.isEmpty()) addIdentifier(system.getIdentifier(), USERS, user, CODESYSTEM, system.getId());

            performCreate(system, auth);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user,
                                                      @PathVariable String id,
                                                      @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/{user}/CodeSystem/{id}/version",
            "/{user}/CodeSystem/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version,
                                                              @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUser(@PathVariable String user) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user));
    }

    @DeleteMapping(path = {"/{user}/CodeSystem/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByUser(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = USER) String user,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        return handleDeleteResource(CodeSystem.class, id, version, formatUser(user), auth);
    }

    @GetMapping(path = {"/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user,
                                                          @RequestParam(name = SYSTEM) String system,
                                                          @RequestParam(name = CODE) String code,
                                                          @RequestParam(name = VERSION, required = false) String version,
                                                          @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user,
                                                            @RequestParam(name = URL) String url,
                                                            @RequestParam(name = CODE) String code,
                                                            @RequestParam(name = VERSION, required = false) String version,
                                                            @RequestParam(name = DISPLAY, required = false) String display,
                                                            @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForUser(@PathVariable(name = USER) String user,
                                                        @RequestBody String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        try {
            ValueSet set = (ValueSet) parser.parseResource(valueSet);
            Optional<Identifier> acsnOpt = hasAccessionIdentifier(set.getIdentifier());
            ResponseEntity<String> response = validate(user, set.getId(), acsnOpt, USERS, user);
            if (response != null) return response;
            if (acsnOpt.isEmpty()) addIdentifier(set.getIdentifier(), USERS, user, VALUESET, set.getId());

            performCreate(set, auth);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping(path = {"/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user,
                                                    @PathVariable String id,
                                                    @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/{user}/ValueSet/{id}/version",
            "/{user}/ValueSet/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version,
                                                            @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user));
    }

    @GetMapping(path = {"/{user}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/{user}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/{user}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/{user}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

}
