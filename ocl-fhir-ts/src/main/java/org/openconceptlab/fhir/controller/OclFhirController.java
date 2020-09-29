package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.classfile.Code;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The OclFhirController class. This is used to support OCL compatible end points.
 * @author hp11
 */
@RestController
@RequestMapping({"/"})
public class OclFhirController {

    CodeSystemResourceProvider codeSystemResourceProvider;
    OclFhirUtil oclFhirUtil;

    @Autowired
    public OclFhirController(CodeSystemResourceProvider codeSystemResourceProvider, OclFhirUtil oclFhirUtil) {
        this.codeSystemResourceProvider = codeSystemResourceProvider;
        this.oclFhirUtil = oclFhirUtil;
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCodeSystemByOrg(@PathVariable String org, @PathVariable String id) {
        return getCodeSystemByOwner(org, id);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCodeSystemByUser(@PathVariable String user, @PathVariable String id) {
        return getCodeSystemByOwner(user, id);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByOrg(@PathVariable String org) {
        return searchCodeSystem(CodeSystem.SP_PUBLISHER, org);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String searchCodeSystemsByUser(@PathVariable String user) {
        return searchCodeSystem(CodeSystem.SP_PUBLISHER, user);
    }

    private String searchCodeSystem(final String... filters) {
        IQuery q = oclFhirUtil.getClient().search().forResource(CodeSystem.class);
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

    private String getCodeSystemByOwner(final String owner, final String id) {
        CodeSystem codeSystem = null;
        try {
            codeSystem = oclFhirUtil.getClient()
                    .read().resource(CodeSystem.class)
                    .withId(id).execute();
        } catch (Exception e) {
            return oclFhirUtil.getResource(oclFhirUtil.getNotFoundOutcome(new IdType(CodeSystem.class.getSimpleName(), id)));
        }
        return codeSystem != null && owner.equals(codeSystem.getPublisher()) ? oclFhirUtil.getResource(codeSystem) : "{}";
    }


}
