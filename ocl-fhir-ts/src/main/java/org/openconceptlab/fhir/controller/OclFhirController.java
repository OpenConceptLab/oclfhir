package org.openconceptlab.fhir.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The OclFhirController class. This is used to define internal FHIR management endpoint.
 * @author hp11
 */
@RestController
public class OclFhirController {

    @GetMapping(path = "/manage")
    public String manage() {
        return "Welcome to FHIR management endpoint";
    }

}
