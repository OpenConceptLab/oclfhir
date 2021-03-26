package org.openconceptlab.fhir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.*;
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

@Tag(name = CONCEPT_MAP_ORGANIZATION_NAMESPACE, description = "organization namespace")
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
    @Operation(description = CREATE_CONCEPTMAP)
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createConceptMapForOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestBody @Parameter(description = THE_CONCEPTMAP_JSON_RESOURCE) String conceptMap,
                                                         @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(description = UPDATE_CONCEPTMAP_VERSION)
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateConceptMapForOrg(@PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                         @PathVariable(name = VERSION) @Parameter(description = THE_CONCEPTMAP_VERSION) String version,
                                                         @PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestBody @Parameter(description = THE_CONCEPTMAP_JSON_RESOURCE) String conceptMap,
                                                         @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(description = DELETE_CONCEPTMAP_VERSION)
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptMapByOrg(@PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                        @PathVariable(name = VERSION) @Parameter(description = THE_CONCEPTMAP_VERSION) String version,
                                                        @PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                        @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
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
    @Operation(description = GET_CONCEPTMAP_BY_ORGANIZATION_AND_ID)
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapByOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                     @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
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
    @Operation(description = GET_SEARCH_CONCEPTMAP_VERSIONS)
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getConceptMapVersionsByOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                             @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                             @PathVariable(name = VERSION) @Parameter(description = THE_CONCEPTMAP_VERSION) Optional<String> version,
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
    @Operation(description = SEARCH_CONCEPTMAPS_FOR_ORGANIZATION)
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchConceptMapsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
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
    @Operation(description = PERFORM_TRANSLATE_BY_URL)
    @GetMapping(path = {"/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                           @RequestParam(name = URL) @Parameter(description = THE_CONCEPTMAP_URL) String conceptMapUrl,
                                                           @RequestParam(name = CONCEPT_MAP_VERSION, required = false) @Parameter(description = THE_CONCEPTMAP_VERSION) String conceptMapVersion,
                                                           @RequestParam(name = SYSTEM) @Parameter(description = THE_SOURCE_CODESYSTEM_URL) String system,
                                                           @RequestParam(name = VERSION, required = false) @Parameter(description = THE_SOURCE_CODESYSTEM_VERSION) String version,
                                                           @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE_TO_BE_TRANSLATED)String code,
                                                           @RequestParam(name = TARGET_SYSTEM, required = false) @Parameter(description = THE_TARGET_CODESYSTEM_URL) String targetSystem) {
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE2)
            })
    })
    @Operation(description = PERFORM_TRANSLATE_BY_URL)
    @PostMapping(path = {"/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                           @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE);
    }

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org               - the organization id
     * @param id                - the {@link ConceptMap} id
     * @param conceptMapUrl     - the {@link ConceptMap} url
     * @param conceptMapVersion - the {@link ConceptMap} version
     * @param system            - the source {@link ConceptMap} url
     * @param version           - the source {@link ConceptMap} version
     * @param code              - the concept code that needs to be translated
     * @param targetSystem      - the target {@link ConceptMap} url
     * @return ResponseEntity
     */
    @Operation(description = PERFORM_TRANSLATE_BY_ID)
    @GetMapping(path = {"/{id}/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrgAndId(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                                @RequestParam(name = URL, required = false) @Parameter(description = THE_CONCEPTMAP_URL) String conceptMapUrl,
                                                                @RequestParam(name = CONCEPT_MAP_VERSION, required = false) @Parameter(description = THE_CONCEPTMAP_VERSION) String conceptMapVersion,
                                                                @RequestParam(name = SYSTEM) @Parameter(description = THE_SOURCE_CODESYSTEM_URL) String system,
                                                                @RequestParam(name = VERSION, required = false) @Parameter(description = THE_SOURCE_CODESYSTEM_VERSION) String version,
                                                                @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE_TO_BE_TRANSLATED) String code,
                                                                @RequestParam(name = TARGET_SYSTEM, required = false) @Parameter(description = THE_TARGET_CODESYSTEM_URL) String targetSystem) {
        Parameters parameters = conceptMapTranslateParameters(conceptMapUrl, conceptMapVersion, system, version, code,
                targetSystem, formatOrg(org));
        return handleFhirOperation(parameters, ConceptMap.class, TRANSLATE, id);
    }

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org               - the organization id
     * @param id                - the {@link ConceptMap} id
     * @param conceptMapUrl     - the {@link ConceptMap} url
     * @param pathVersion       - the {@link ConceptMap} version
     * @param system            - the source {@link ConceptMap} url
     * @param version           - the source {@link ConceptMap} version
     * @param code              - the concept code that needs to be translated
     * @param targetSystem      - the target {@link ConceptMap} url
     * @return ResponseEntity
     */
    @Operation(description = PERFORM_TRANSLATE_BY_ID)
    @GetMapping(path = {"/{id}/version/{version}/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrgAndIdAndVersion(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                          @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                                          @PathVariable(name = VERSION) @Parameter(description = THE_CONCEPTMAP_VERSION) String pathVersion,
                                                                          @RequestParam(name = URL, required = false) @Parameter(description = THE_CONCEPTMAP_URL) String conceptMapUrl,
                                                                          @RequestParam(name = SYSTEM) @Parameter(description = THE_SOURCE_CODESYSTEM_URL) String system,
                                                                          @RequestParam(name = VERSION, required = false) @Parameter(description = THE_SOURCE_CODESYSTEM_VERSION) String version,
                                                                          @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE_TO_BE_TRANSLATED) String code,
                                                                          @RequestParam(name = TARGET_SYSTEM, required = false) @Parameter(description = THE_TARGET_CODESYSTEM_URL) String targetSystem) {
        Parameters parameters = conceptMapTranslateParameters(conceptMapUrl, pathVersion, system, version, code,
                targetSystem, formatOrg(org));
        return handleFhirOperation(parameters, ConceptMap.class, TRANSLATE, id);
    }

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org        - the organization id
     * @param id         - the {@link ConceptMap} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_ID_EXAMPLE1),
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_ID_EXAMPLE2)
            })
    })
    @Operation(description = PERFORM_TRANSLATE_BY_ID)
    @PostMapping(path = {"/{id}/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrgAndId(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                                @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE, id);
    }

    /**
     * Perform {@link ConceptMap} $translate.
     *
     * @param org        - the organization id
     * @param id         - the {@link ConceptMap} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_ID_VERSION_EXAMPLE1),
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_ID_VERSION_EXAMPLE2)
            })
    })
    @Operation(description = PERFORM_TRANSLATE_BY_ID)
    @PostMapping(path = {"/{id}/version/{version}/$translate"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> translateConceptMapByOrgAndIdAndVersion(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                          @PathVariable(name = ID) @Parameter(description = THE_CONCEPTMAP_ID) String id,
                                                                          @PathVariable(name = VERSION) @Parameter(description = THE_CONCEPTMAP_VERSION) String pathVersion,
                                                                          @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        if (isValid(pathVersion))
            params.setParameter(CONCEPT_MAP_VERSION, pathVersion);
        return handleFhirOperation(params, ConceptMap.class, TRANSLATE, id);
    }

}



