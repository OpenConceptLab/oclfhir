package org.openconceptlab.fhir.controller;

import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.TRANSLATE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

@RestController
@RequestMapping({"/orgs/{org}/ConceptMap"})
public class OclFhirOrgConceptMapController extends BaseOclFhirController {

    public OclFhirOrgConceptMapController(CodeSystemResourceProvider codeSystemResourceProvider,
                                          ValueSetResourceProvider valueSetResourceProvider,
                                          OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link ConceptMap}
     *
     * @param org       - the username
     * @param conceptMap - the {@link ConceptMap} resource
     * @return ResponseEntity
     */
    @PostMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createConceptMapForOrg(@PathVariable(name = ORG) String org,
                                                         @RequestBody String conceptMap,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        ConceptMap map = (ConceptMap) parser.parseResource(conceptMap);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(Collections.singletonList(map.getIdentifier()));
        ResponseEntity<String> response = validate(map.getIdElement().getIdPart(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) map.setIdentifier(
                getIdentifier(ORGS, org, CONCEPTMAP, map.getIdElement().getIdPart(), map.getVersion())
        );

        performCreate(map, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link ConceptMap} version.
     *
     * @param id         - the {@link ConceptMap} id
     * @param version    - the {@link ConceptMap} version
     * @param org        - the organization id
     * @param conceptMap - the {@link ConceptMap} resource
     * @return ResponseEntity
     */
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateConceptMapForOrg(@PathVariable(name = ID) String id,
                                                         @PathVariable(name = VERSION) String version,
                                                         @PathVariable(name = ORG) String org,
                                                         @RequestBody String conceptMap,
                                                         @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CONCEPTMAP, id, version, ORG, org))
            return badRequest("The ConceptMap can not be edited.");
        ConceptMap map = (ConceptMap) parser.parseResource(conceptMap);
        IdType idType = new IdType(CONCEPTMAP, id, version);
        performUpdate(map, auth, idType, formatOrg(org));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link ConceptMap} version.
     *
     * @param id      - the {@link ConceptMap} id
     * @param version - the {@link ConceptMap} version
     * @param org     - the organization id
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptMapByOrg(@PathVariable(name = ID) String id,
                                                        @PathVariable(name = VERSION) String version,
                                                        @PathVariable(name = ORG) String org,
                                                        @RequestHeader(name = AUTHORIZATION) String auth) {
        if (!validateIfEditable(CONCEPTMAP, id, version, ORG, org))
            return badRequest("The ConceptMap can not be deleted.");
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
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
     * @param system            - the source {@link ConceptMap} url
     * @param version           - the source {@link ConceptMap} version
     * @param code              - the concept code that needs to be translated
     * @param targetSystem      - the target {@link ConceptMap} url
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


