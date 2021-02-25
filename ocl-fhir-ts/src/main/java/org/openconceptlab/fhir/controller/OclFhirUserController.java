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

@RestController
@RequestMapping({"/users"})
public class OclFhirUserController extends BaseOclFhirController {

    public OclFhirUserController(CodeSystemResourceProvider codeSystemResourceProvider,
                                 ValueSetResourceProvider valueSetResourceProvider,
                                 OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    @PostMapping(path = {"/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PutMapping(path = {"/{user}/CodeSystem/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateCodeSystemForUser(@PathVariable(name = ID) String id,
                                                          @PathVariable(name = VERSION) String version,
                                                          @PathVariable(name = USER) String user,
                                                          @RequestBody String codeSystem,
                                                          @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, USER, user)) return badRequest("The CodeSystem can not be edited.");
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(id, acsnOpt, USERS, user);
        if (response != null) return response;
        IdType idType = new IdType(CODESYSTEM, id, version);

        performUpdate(system, auth, idType, formatUser(user));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping(path = {"/{user}/CodeSystem/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInCodeSystemForUser(@PathVariable(name = ID) String id,
                                                                   @PathVariable(name = VERSION) String version,
                                                                   @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                   @PathVariable(name = USER) String user,
                                                                   @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + SOURCES + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    @GetMapping(path = {"/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user,
                                                      @PathVariable String id,
                                                      @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                      HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/CodeSystem/{id}/version", "/{user}/CodeSystem/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version,
                                                              @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                              HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUser(@PathVariable String user,
                                                          @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                          HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    @DeleteMapping(path = {"/{user}/CodeSystem/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByUser(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = USER) String user,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, USER, user)) return badRequest("The CodeSystem can not be deleted.");
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + SOURCES + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    @DeleteMapping(path = {"/{user}/ValueSet/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteValueSetByUser(@PathVariable(name = ID) String id,
                                                       @PathVariable(name = VERSION) String version,
                                                       @PathVariable(name = USER) String user,
                                                       @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + COLLECTIONS + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    @DeleteMapping(path = {"/{user}/ValueSet/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInValueSetForUser(@PathVariable(name = ID) String id,
                                                                 @PathVariable(name = VERSION) String version,
                                                                 @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                 @PathVariable(name = USER) String user,
                                                                 @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + USERS + FS + user + FS + COLLECTIONS + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
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
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters) {
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
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForUser(@PathVariable(name = USER) String user,
                                                        @RequestBody String valueSet,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(set.getIdentifier());
        ResponseEntity<String> response = validate(set.getIdElement().getIdPart(), acsnOpt, USERS, user);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(set.getIdentifier(), USERS, user, VALUESET, set.getIdElement().getIdPart(), set.getVersion());

        performCreate(set, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = {"/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user,
                                                    @PathVariable String id,
                                                    @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                    HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/ValueSet/{id}/version",
            "/{user}/ValueSet/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version,
                                                            @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                            HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user,
                                                        @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                        HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
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
    public ResponseEntity<String> validateValueSetByUser(@PathVariable String user, @RequestBody String parameters) {
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
    public ResponseEntity<String> expandValueSetByUser(@PathVariable String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    @GetMapping(path = {"/{user}/ConceptMap/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapByUser(@PathVariable(name = USER) String user,
                                                      @PathVariable(name = ID) String id,
                                                      @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                      HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatUser(user), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/ConceptMap/{id}/version",
            "/{user}/ConceptMap/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version,
                                                              @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                              HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/ConceptMap"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchConceptMapsByUser(@PathVariable String user,
                                                          @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                          HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatUser(user), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    @GetMapping(path = {"/{user}/ConceptMap/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByUser(@PathVariable(name = USER) String user,
                                                            @RequestParam(name = URL) String conceptMapUrl,
                                                            @RequestParam(name = CONCEPT_MAP_VERSION, required = false) String conceptMapVersion,
                                                            @RequestParam(name = SYSTEM) String system,
                                                            @RequestParam(name = VERSION, required = false) String version,
                                                            @RequestParam(name = CODE) String code,
                                                            @RequestParam(name = TARGET_SYSTEM, required = false) String targetSystem) {
        Parameters parameters = conceptMapTranslateParameters(conceptMapUrl, conceptMapVersion, system, version, code,
                targetSystem, formatUser(user));
        return handleFhirOperation(parameters, ConceptMap.class, TRANSLATE);
    }

    @PostMapping(path = {"/{user}/ConceptMap/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByUser(@PathVariable(name = USER) String user, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE);
    }


}

