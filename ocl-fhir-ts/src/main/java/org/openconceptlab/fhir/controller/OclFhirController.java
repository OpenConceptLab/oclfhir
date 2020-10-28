package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable(name = ORG) String org, @PathVariable(name = ID) String id) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}/version",
                        "/orgs/{org}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version) {
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
        Parameters parameters = generateParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleLookup(parameters);
    }

    @PostMapping(path = {"/orgs/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(PROPERTY).setValue(new StringType(formatOrg(org)));
        return handleLookup(params);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org, @PathVariable String id) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}/version",
                        "/orgs/{org}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByOrg(@PathVariable String org) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user, @PathVariable String id) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}/version",
                        "/users/{user}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version) {
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
        Parameters parameters = generateParameters(system, code, version, displayLanguage, formatUser(user));
        return handleLookup(parameters);
    }

    @PostMapping(path = {"/users/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(PROPERTY).setValue(new StringType(formatUser(user)));
        return handleLookup(params);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user, @PathVariable String id) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}/version",
                        "/users/{user}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/users/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user));
    }

    private ResponseEntity<String> handleSearchResource(final Class<? extends MetadataResource> resourceClass, final String... args) {
        try {
            String resource = searchResource(resourceClass, args);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    private ResponseEntity<String> handleLookup(Parameters parameters) {
        try {
            return ResponseEntity.ok(oclFhirUtil.getResourceAsString(lookUpCodeSystem(parameters)));
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
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

    private Parameters lookUpCodeSystem(Parameters parameters) {
        return oclFhirUtil.getClient()
                .operation()
                .onType(CodeSystem.class)
                .named(LOOKUP)
                .withParameters(parameters)
                .execute();
    }

    private Parameters generateParameters(String system, String code, String version, String displayLanguage, String owner) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(new StringType(version));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISP_LANG).setValue(new CodeType(displayLanguage));
        parameters.addParameter().setName(PROPERTY).setValue(new StringType(owner));
        return parameters;
    }

    private static String formatOrg(String org) {
        return ORG_ + org;
    }

    private static String formatUser(String user) {
        return USER_ + user;
    }
}
