package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * The OclFhirController class. This is used to support OCL compatible end points.
 *
 * @author harpatel1
 */
@RestController
@RequestMapping({"/"})
public class OclFhirController {

    CodeSystemResourceProvider codeSystemResourceProvider;
    ValueSetResourceProvider valueSetResourceProvider;
    OclFhirUtil oclFhirUtil;

    @Autowired
    public OclFhirController(CodeSystemResourceProvider codeSystemResourceProvider,
                             ValueSetResourceProvider valueSetResourceProvider,
                             OclFhirUtil oclFhirUtil) {
        this.codeSystemResourceProvider = codeSystemResourceProvider;
        this.valueSetResourceProvider = valueSetResourceProvider;
        this.oclFhirUtil = oclFhirUtil;
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable(name = ORG) String org,
                                                     @PathVariable(name = ID) String id,
                                                     @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}/version",
                        "/orgs/{org}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByOrg(@PathVariable String org) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org,
                                         @RequestParam(name = SYSTEM) String system,
                                         @RequestParam(name = CODE) String code,
                                         @RequestParam(name = VERSION, required = false) String version,
                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/orgs/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/orgs/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org,
                                                   @PathVariable String id,
                                                   @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}/version",
                        "/orgs/{org}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version,
                                                           @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByOrg(@PathVariable String org) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/orgs/{org}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/orgs/{org}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user,
                                                      @PathVariable String id,
                                                      @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}/version",
                        "/users/{user}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version,
                                                              @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUser(@PathVariable String user) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user,
                                                         @RequestParam(name = SYSTEM) String system,
                                                         @RequestParam(name = CODE) String code,
                                                         @RequestParam(name = VERSION, required = false) String version,
                                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/users/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/users/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user,
                                                    @PathVariable String id,
                                                    @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}/version",
                        "/users/{user}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version,
                                                            @RequestParam(name = PAGE, required = false) String page) {
        if (isValid(page))
            return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL), PAGE, page);
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/users/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user));
    }

    @GetMapping(path = {"/users/{user}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/users/{user}/ValueSet/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @PostMapping(path = {"/users/{user}/ValueSet/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    private ResponseEntity<String> handleSearchResource(final Class<? extends MetadataResource> resourceClass, final String... args) {
        try {
            String resource = searchResource(resourceClass, args);
            return ResponseEntity.ok(resource);
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    private ResponseEntity<String> handleFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        try {
            return ResponseEntity.ok(oclFhirUtil.getResourceAsString(performFhirOperation(parameters, type, operation)));
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    private String searchResource(final Class<? extends MetadataResource> resourceClass, final String... filters) {
        IQuery q = oclFhirUtil.getClient().search().forResource(resourceClass);
        if (filters.length % 2 == 0) {
            for (int i = 0; i < filters.length; i += 2) {
                if (i == 0) {
                    q = q.where(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                } else {
                    q = q.and(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                }
            }
        }
        Bundle bundle = (Bundle) q.execute();
        return oclFhirUtil.getResourceAsString(bundle);
    }

    private Parameters performFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        return oclFhirUtil.getClient()
                .operation()
                .onType(type)
                .named(operation)
                .withParameters(parameters)
                .execute();
    }

    private Parameters generateParameters(String code, String displayLanguage, String owner) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISP_LANG).setValue(new CodeType(displayLanguage));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    private Parameters lookupParameters(String system, String code, String version, String displayLanguage, String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        return parameters;
    }

    private Parameters codeSystemVCParameters(String url, String code, String version, String display, String displayLanguage,
                                              String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(newStringType(display));
        return parameters;
    }

    private Parameters valueSetVCParameters(String url, String valueSetId, String valueSetVersion, String code, String system, String systemVersion,
                                            String display, String displayLanguage, String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        if (isValid(url))
            parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(valueSetId))
            parameters.addParameter().setName("valueSetId").setValue(newStringType(valueSetId));
        if (isValid(systemVersion))
            parameters.addParameter().setName(SYSTEM_VERSION).setValue(newStringType(systemVersion));
        if (isValid(valueSetVersion))
            parameters.addParameter().setName(VALUESET_VERSION).setValue(newStringType(valueSetVersion));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(newStringType(display));
        return parameters;
    }

    private Parameters valueSetExpandParameters(String url, String valueSetVersion, Integer offset, Integer count, Boolean includeDesignations,
                                                Boolean includeDefinition, Boolean activeOnly, String displayLanguage, String filter, String owner) {
        Parameters parameters = new Parameters();
        if (isValid(url))
            parameters.addParameter().setName(URL).setValue(newUri(url));
        if (isValid(valueSetVersion))
            parameters.addParameter().setName(VALUESET_VERSION).setValue(newStringType(valueSetVersion));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISPLAY_LANGUAGE).setValue(newStringType(displayLanguage));
        if (isValid(filter))
            parameters.addParameter().setName(FILTER).setValue(newStringType(filter));
        parameters.addParameter().setName(OFFSET).setValue(newInteger(offset));
        parameters.addParameter().setName(COUNT).setValue(newInteger(count));
        parameters.addParameter().setName(INCLUDE_DESIGNATIONS).setValue(newBoolean(includeDesignations));
        parameters.addParameter().setName(INCLUDE_DEFINITION).setValue(newBoolean(includeDefinition));
        parameters.addParameter().setName(ACTIVE_ONLY).setValue(newBoolean(activeOnly));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    private static String formatOrg(String org) {
        return ORG_ + org;
    }

    private static String formatUser(String user) {
        return USER_ + user;
    }
}
