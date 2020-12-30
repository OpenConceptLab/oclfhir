package org.openconceptlab.fhir.controller;

import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.web.bind.annotation.*;

/**
 * The OclFhirController class. This is used to support base ocl end points.
 *
 * @author harpatel1
 */
@RestController
@RequestMapping({"/"})
public class OclFhirController extends BaseOclFhirController{

    public OclFhirController(CodeSystemResourceProvider codeSystemResourceProvider,
                             ValueSetResourceProvider valueSetResourceProvider,
                             OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

}

