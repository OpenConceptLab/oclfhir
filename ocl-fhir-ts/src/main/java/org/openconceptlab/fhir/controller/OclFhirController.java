package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * The OclFhirController class. This is used to support OCL compatible end points.
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
    public String getCodeSystemByOrg(@PathVariable String org, @PathVariable String id) {
        return searchResource(CodeSystem.class, OWNER, ORG_ + org, ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByOrg(@PathVariable String org) {
        return searchResource(CodeSystem.class, OWNER, ORG_ + org);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getValueSetByOrg(@PathVariable String org, @PathVariable String id) {
        return searchResource(ValueSet.class, OWNER, ORG_ + org, ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchValueSetsByOrg(@PathVariable String org) {
        return searchResource(ValueSet.class, OWNER, ORG_ + org);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCodeSystemByUser(@PathVariable String user, @PathVariable String id) {
        return searchResource(CodeSystem.class, OWNER, USER_ + user, ID, id);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByUser(@PathVariable String user) {
        return searchResource(CodeSystem.class, OWNER, USER_ + user);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getValueSetByUser(@PathVariable String user, @PathVariable String id) {
        return searchResource(ValueSet.class, OWNER, USER_ + user, ID, id);
    }

    @GetMapping(path = {"/users/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchValueSetsByUser(@PathVariable String user) {
        return searchResource(ValueSet.class, OWNER, USER_ + user);
    }

    private String searchResource(final Class<? extends MetadataResource> resourceClass, final String... filters) {
        IQuery q = oclFhirUtil.getClient().search().forResource(resourceClass);
        if(filters.length % 2 == 0) {
            for(int i=0; i<filters.length; i+=2) {
                if (i==0) {
                    q = q.where(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                } else {
                    q = q.and(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                }
            }
        }
        Bundle bundle = (Bundle) q.execute();
        return oclFhirUtil.getResource(bundle);
    }
}
