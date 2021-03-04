package org.openconceptlab.fhir.controller;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Parameters;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.TRANSLATE;
import static org.openconceptlab.fhir.util.OclFhirUtil.getResource;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@RestController
@RequestMapping({"/orgs/{org}/ConceptMap"})
public class OclFhirOrgConceptMapController extends BaseOclFhirController {

    public OclFhirOrgConceptMapController(CodeSystemResourceProvider codeSystemResourceProvider,
                                          ValueSetResourceProvider valueSetResourceProvider,
                                          OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Read {@link ConceptMap} by Id.
     *
     * @param org  - the organization id
     * @param id   - the {@link ConceptMap} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapByOrg(@PathVariable(name = ORG) String org,
                                                     @PathVariable(name = ID) String id,
                                                     @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                     HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link ConceptMap} version.
     *
     * @param org     - the organization id
     * @param id      - the {@link ConceptMap} id
     * @param version - the {@link ConceptMap} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                             HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link ConceptMap} of org.
     *
     * @param org  - the organization id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchConceptMapsByOrg(@PathVariable String org,
                                                         @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                         HttpServletRequest request) {
        return handleSearchResource(ConceptMap.class, OWNER, formatOrg(org), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org               - the organization id
     * @param conceptMapUrl     - the {@link ConceptMap} url
     * @param conceptMapVersion - the {@link ConceptMap} version
     * @param system            - the source {@link CodeSystem} url
     * @param version           - the source {@link CodeSystem} version
     * @param code              - the concept code that needs to be translated
     * @param targetSystem      - the target {@link CodeSystem} url
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
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

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @PostMapping(path = {"/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrg(@PathVariable(name = ORG) String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE);
    }

}


