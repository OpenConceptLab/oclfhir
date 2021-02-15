package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.CODESYSTEM;

@RestController
@RequestMapping({"/orgs"})
public class OclFhirOrgController extends BaseOclFhirController {

    public OclFhirOrgController(CodeSystemResourceProvider codeSystemResourceProvider,
                                ValueSetResourceProvider valueSetResourceProvider,
                                OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    @PostMapping(path = {"/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystemForOrg(@PathVariable(name = ORG) String org,
                                                         @RequestBody String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(org, system.getId(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(system.getIdentifier(), ORGS, org, CODESYSTEM, system.getId());

        performCreate(system, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = {"/{org}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable(name = ORG) String org,
                                                     @PathVariable(name = ID) String id,
                                                     @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/{org}/CodeSystem/{id}/version",
            "/{org}/CodeSystem/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByOrg(@PathVariable String org,
                                                         @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), PAGE, page.orElse("1"));
    }

    @DeleteMapping(path = {"/{org}/CodeSystem/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByOrg(@PathVariable(name = ID) String id,
                                                        @PathVariable(name = VERSION) String version,
                                                        @PathVariable(name = ORG) String org,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        return handleDeleteResource(CodeSystem.class, id, version, formatOrg(org), auth);
    }

    @PutMapping(path = {"/{org}/CodeSystem/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateCodeSystemForOrg(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = ORG) String org,
                                                         @RequestBody String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(org, id, acsnOpt, USERS, org);
        if (response != null) return response;
        IdType idType = new IdType(CODESYSTEM, id, version);

        performUpdate(system, auth, idType, formatOrg(org));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping(path = {"/{org}/CodeSystem/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInCodeSystemForOrg(@PathVariable(name = ID) String id,
                                                                  @PathVariable(name = VERSION) String version,
                                                                  @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                  @PathVariable(name = ORG) String org,
                                                                  @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    @GetMapping(path = {"/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org,
                                                         @RequestParam(name = SYSTEM) String system,
                                                         @RequestParam(name = CODE) String code,
                                                         @RequestParam(name = VERSION, required = false) String version,
                                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForOrg(@PathVariable(name = ORG) String org,
                                                       @RequestBody String valueSet,
                                                       @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(set.getIdentifier());
        ResponseEntity<String> response = validate(org, set.getId(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(set.getIdentifier(), ORGS, org, VALUESET, set.getId());

        performCreate(set, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = {"/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org,
                                                   @PathVariable String id,
                                                   @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/{org}/ValueSet/{id}/version",
            "/{org}/ValueSet/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version,
                                                           @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByOrg(@PathVariable String org,
                                                       @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), PAGE, page.orElse("1"));
    }

    @GetMapping(path = {"/{org}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrg(@PathVariable String org,
                                                        @RequestParam(name = URL) String url,
                                                        @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                        @RequestParam(name = CODE) String code,
                                                        @RequestParam(name = SYSTEM) String system,
                                                        @RequestParam(name = SYSTEM_VERSION, required = false) String systemVersion,
                                                        @RequestParam(name = DISPLAY, required = false) String display,
                                                        @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {

        Parameters parameters = valueSetVCParameters(url, EMPTY, valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/{org}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/{org}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrg(@PathVariable String org,
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
                includeDefinition, activeOnly, displayLanguage, filter, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND);
    }

    @PostMapping(path = {"/{org}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    @GetMapping(path = {"/{org}/ConceptMap/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapByOrg(@PathVariable(name = ORG) String org,
                                                     @PathVariable(name = ID) String id,
                                                     @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id, PAGE, page);
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/{org}/ConceptMap/{id}/version",
            "/{org}/ConceptMap/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/{org}/ConceptMap"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchConceptMapsByOrg(@PathVariable String org,
                                                         @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), PAGE, page.orElse("1"));
    }

    @GetMapping(path = {"/{org}/ConceptMap/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrg(@PathVariable(name = ORG) String org,
                                                           @RequestParam(name = URL) String conceptMapUrl,
                                                           @RequestParam(name = CONCEPT_MAP_VERSION, required = false) String conceptMapVersion,
                                                           @RequestParam(name = SYSTEM) String system,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = TARGET_SYSTEM, required = false) String targetSystem) {

        Parameters parameters = conceptMapTranslateParameters(conceptMapUrl, conceptMapVersion, system, version, code,
                targetSystem, formatOrg(org));
        return handleFhirOperation(parameters, ConceptMap.class, TRANSLATE);
    }

    @PostMapping(path = {"/{org}/ConceptMap/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrg(@PathVariable(name = ORG) String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE);
    }

}

