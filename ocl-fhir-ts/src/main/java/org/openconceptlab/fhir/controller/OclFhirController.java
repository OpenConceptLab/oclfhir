package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.badRequest;
import static org.openconceptlab.fhir.util.OclFhirUtil.notFound;

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
        try {
            String resource = searchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}/_history",
                        "/orgs/{org}/CodeSystem/{id}/_history/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version) {
        try {
            String resource = searchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id,
                    _HISTORY, version.orElse(ALL));
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByOrg(@PathVariable String org) {
        return searchResource(CodeSystem.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org, @PathVariable String id) {
        try {
            String resource = searchResource(ValueSet.class, OWNER, formatOrg(org), ID, id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}/_history",
                        "/orgs/{org}/ValueSet/{id}/_history/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version) {
        try {
            String resource = searchResource(ValueSet.class, OWNER, formatOrg(org), ID, id,
                    _HISTORY, version.orElse(ALL));
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchValueSetsByOrg(@PathVariable String org) {
        return searchResource(ValueSet.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user, @PathVariable String id) {
        try {
            String resource = searchResource(CodeSystem.class, OWNER, formatUser(user), ID, id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}/_history",
                        "/users/{user}/CodeSystem/{id}/_history/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version) {
        try {
            String resource = searchResource(CodeSystem.class, OWNER, formatUser(user), ID, id,
                    _HISTORY, version.orElse(ALL));
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/users/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByUser(@PathVariable String user) {
        return searchResource(CodeSystem.class, OWNER, formatUser(user));
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user, @PathVariable String id) {
        try {
            String resource = searchResource(ValueSet.class, OWNER, formatUser(user), ID, id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}/_history",
                        "/users/{user}/ValueSet/{id}/_history/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version) {
        try {
            String resource = searchResource(ValueSet.class, OWNER, formatUser(user), ID, id,
                    _HISTORY, version.orElse(ALL));
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    @GetMapping(path = {"/users/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchValueSetsByUser(@PathVariable String user) {
        return searchResource(ValueSet.class, OWNER, formatUser(user));
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
        return oclFhirUtil.getResource(bundle);
    }

    private static String formatOrg(String org) {
        return ORG_ + org;
    }

    private static String formatUser(String user) {
        return USER_ + user;
    }
}
