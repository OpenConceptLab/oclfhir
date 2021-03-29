package org.openconceptlab.fhir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;

/**
 * This is the work around to have GLOBAL NAMESPACE APIS listed in swagger document.
 * Since /fhir/* path is always going to be handled by the {@link org.openconceptlab.fhir.OclFhirRestfulServer},
 * the path of execution will NEVER reach to this controller.
 */

@RestController
@RequestMapping({"/fhir"})
public class SwaggerDocumentController extends BaseOclFhirController{

    public SwaggerDocumentController(CodeSystemResourceProvider codeSystemResourceProvider,
                                     ValueSetResourceProvider valueSetResourceProvider, OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = CREATE_CODESYSTEM)
    @PostMapping(path = "/CodeSystem", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystem(
            @RequestBody @Parameter(description = THE_CODESYSTEM_JSON_RESOURCE) String codeSystem,
            @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = GET_SEARCH_CODE_SYSTEMS)
    @ApiResponse()
    @GetMapping(path = "/CodeSystem", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUrl(
            @RequestParam(name = CodeSystem.SP_URL, required = false) @Parameter(description = THE_CODESYSTEM_URL) String url,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
            @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_LOOKUP_BY_URL)
    @GetMapping(path = "/CodeSystem/$lookup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> codeSystemLookup(
            @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
            @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
            @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DISPLAY_PARAMETER) String displayLanguage) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = CODESYSTEM_LOOKUP_REQ_BODY_EXAMPLE)
            })
    })
    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_LOOKUP_BY_URL)
    @PostMapping(path = "/CodeSystem/$lookup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> codeSystemLookup(@RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @GetMapping(path = "/CodeSystem/$validate-code", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> codeSystemValidateCode(
            @RequestParam(name = URL) @Parameter(description = THE_CODESYSTEM_URL) String url,
            @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
            @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
            @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example2", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE2)
            })
    })
    @Tag(name = CODE_SYSTEM_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @PostMapping(path = "/CodeSystem/$validate-code", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> codeSystemValidateCode(@RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = CREATE_VALUESET)
    @PostMapping(path = "/ValueSet", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSet(
            @RequestBody @Parameter(description = THE_VALUESET_JSON_RESOURCE) String valueSet,
            @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = GET_SEARCH_VALUE_SETS)
    @GetMapping(path = "/ValueSet", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUrl(
            @RequestParam(name = ValueSet.SP_URL, required = false) @Parameter(description = THE_VALUESET_URL) String url,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String version,
            @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @GetMapping(path = "/ValueSet/$validate-code", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> valueSetValidateCode(
            @RequestParam(name = URL) @Parameter(description = THE_VALUESET_URL) String url,
            @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
            @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
            @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
            @RequestParam(name = SYSTEM_VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String systemVersion,
            @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
            @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example2", value = VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE2)
            })
    })
    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @PostMapping(path = "/ValueSet/$validate-code", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> valueSetValidateCode(@RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_EXPAND_BY_URL)
    @GetMapping(path = "/ValueSet/$expand", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> valueSetExpand(
            @RequestParam(name = URL) @Parameter(description = THE_VALUESET_URL) String url,
            @RequestParam(name = VALUESET_VERSION, required = false) @Parameter(description = THE_VALUESET_VERSION) String valueSetVersion,
            @RequestParam(name = OFFSET, required = false, defaultValue = "0") @Parameter(description = STARTING_INDEX_IF_SUBSET_IS_DESIRED) Integer offset,
            @RequestParam(name = COUNT, required = false, defaultValue = "100") @Parameter(description = NUMBER_OF_CODES_TO_BE_RETURNED) Integer count,
            @RequestParam(name = INCLUDE_DESIGNATIONS, required = false, defaultValue = "true") @Parameter(description = INCLUDE_CONCEPT_DESIGNATIONS) Boolean includeDesignations,
            @RequestParam(name = INCLUDE_DEFINITION, required = false, defaultValue = "false") @Parameter(description = INCLUDE_VALUESET_DEFINITION) Boolean includeDefinition,
            @RequestParam(name = ACTIVE_ONLY, required = false, defaultValue = "true") @Parameter(description = ONLY_INCLUDE_ACTIVE_CONCEPTS) Boolean activeOnly,
            @RequestParam(name = DISPLAY_LANGUAGE, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_VALUE_SET_EXPANSION_CONTAINS_DISPLAY) String displayLanguage,
            @RequestParam(name = FILTER, required = false) @Parameter(description = VALUESET_EXPAND_FILTER_TEXT) String filter) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = VALUESET_EXPAND_REQ_BODY_EXAMPLE)
            })
    })
    @Tag(name = VALUE_SET_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_EXPAND_BY_URL)
    @PostMapping(path = "/ValueSet/$expand", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> valueSetExpand(@RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CONCEPT_MAP_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = CREATE_CONCEPTMAP)
    @PostMapping(path = "/ConceptMap", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createConceptMap(
            @RequestBody @Parameter(description = THE_CONCEPTMAP_JSON_RESOURCE) String conceptMap,
            @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CONCEPT_MAP_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = GET_SEARCH_CONCEPT_MAPS)
    @GetMapping(path = "/ConceptMap", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchConceptMapsByUrl(
            @RequestParam(name = ConceptMap.SP_URL, required = false) @Parameter(description = THE_CONCEPTMAP_URL) String url,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CONCEPTMAP_VERSION) String version,
            @RequestParam(name = PAGE, required = false) Optional<String> page) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @Tag(name = CONCEPT_MAP_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_TRANSLATE_BY_URL)
    @GetMapping(path = "/ConceptMap/$translate", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> conceptMapLookup(
            @RequestParam(name = URL) @Parameter(description = THE_CONCEPTMAP_URL) String conceptMapUrl,
            @RequestParam(name = CONCEPT_MAP_VERSION, required = false) @Parameter(description = THE_CONCEPTMAP_VERSION) String conceptMapVersion,
            @RequestParam(name = SYSTEM) @Parameter(description = THE_SOURCE_CODESYSTEM_URL) String sourceSystem,
            @RequestParam(name = VERSION, required = false) @Parameter(description = THE_SOURCE_CODESYSTEM_VERSION) String sourceVersion,
            @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE_TO_BE_TRANSLATED) String sourceCode,
            @RequestParam(name = TARGET_SYSTEM, required = false) @Parameter(description = THE_TARGET_CODESYSTEM_URL) String targetSystem) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example2", value = CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE2)
            })
    })
    @Tag(name = CONCEPT_MAP_GLOBAL_NAMESPACE, description = "global namespace")
    @Operation(summary = PERFORM_TRANSLATE_BY_URL)
    @PostMapping(path = "/ConceptMap/$translate", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> conceptMapLookup(@RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        return ResponseEntity.badRequest().body(DOCUMENTATION_CONTROLLER_CALLED);
    }

}
