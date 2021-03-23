package org.openconceptlab.fhir.controller;

import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.EXPAND;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@RestController
@RequestMapping({"/orgs/{org}/ValueSet"})
public class OclFhirOrgValueSetController extends BaseOclFhirController {

    public OclFhirOrgValueSetController(CodeSystemResourceProvider codeSystemResourceProvider,
                                        ValueSetResourceProvider valueSetResourceProvider,
                                        OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link ValueSet}
     *
     * @param org      - the organization id
     * @param valueSet - the {@link ValueSet} resource
     * @return ResponseEntity
     */
    @PostMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createValueSetForOrg(@PathVariable(name = ORG) String org,
                                                       @RequestBody String valueSet,
                                                       @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(set.getIdentifier());
        ResponseEntity<String> response = validate(set.getIdElement().getIdPart(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty())
            addIdentifier(set.getIdentifier(), ORGS, org, VALUESET, set.getIdElement().getIdPart(), set.getVersion());

        performCreate(set, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link ValueSet} version.
     *
     * @param id       - the {@link ValueSet} id
     * @param version  - the {@link ValueSet} version
     * @param org      - the organization id
     * @param valueSet - the {@link ValueSet} resource
     * @return ResponseEntity
     */
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateValueSetForOrg(@PathVariable(name = ID) String id,
                                                       @PathVariable(name = VERSION) String version,
                                                       @PathVariable(name = ORG) String org,
                                                       @RequestBody String valueSet,
                                                       @RequestHeader(name = AUTHORIZATION) String auth) {
        ValueSet set = (ValueSet) parser.parseResource(valueSet);
        IdType idType = new IdType(VALUESET, id, version);
        performUpdate(set, auth, idType, formatOrg(org));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link ValueSet} version.
     *
     * @param id      - the {@link ValueSet} id
     * @param version - the {@link ValueSet} version
     * @param org     - the organization id
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteValueSetByOrg(@PathVariable(name = ID) String id,
                                                      @PathVariable(name = VERSION) String version,
                                                      @PathVariable(name = ORG) String org,
                                                      @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + COLLECTIONS + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Delete concepts from {@link ValueSet} version.
     *
     * @param id        - the {@link ValueSet} id
     * @param version   - the {@link ValueSet} version
     * @param conceptId - the concept code
     * @param org       - the organization id
     * @return ResponseEntity
     */
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInValueSetForOrg(@PathVariable(name = ID) String id,
                                                                @PathVariable(name = VERSION) String version,
                                                                @PathVariable(name = CONCEPT_ID) String conceptId,
                                                                @PathVariable(name = ORG) String org,
                                                                @RequestHeader(name = AUTHORIZATION) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + COLLECTIONS + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Read {@link ValueSet} by Id.
     *
     * @param org  - the organization id
     * @param id   - the {@link ValueSet} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org,
                                                   @PathVariable String id,
                                                   @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                   HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link ValueSet} version.
     *
     * @param org     - the organization id
     * @param id      - the {@link ValueSet} id
     * @param version - the {@link ValueSet} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version,
                                                           @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                           HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link ValueSet} of org.
     *
     * @param org  - the organization id
     * @param page - the page number
     * @return ResponseEntity
     */
    @GetMapping(path = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByOrg(@PathVariable String org,
                                                       @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                       HttpServletRequest request) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param org             - the organization id
     * @param url             - the {@link ValueSet} url
     * @param valueSetVersion - the {@link ValueSet} version
     * @param code            - the concept code
     * @param system          - the {@link CodeSystem} url
     * @param systemVersion   - the {@link CodeSystem} version
     * @param display         - the display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrg(@PathVariable String org,
                                                        @RequestParam(name = URL) String url,
                                                        @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                        @RequestParam(name = CODE) String code,
                                                        @RequestParam(name = SYSTEM) String system,
                                                        @RequestParam(name = SYSTEM_VERSION, required = false) String systemVersion,
                                                        @RequestParam(name = DISPLAY, required = false) String display,
                                                        @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return
     */
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param org                 - the organization id
     * @param url                 - the {@link ValueSet} url
     * @param valueSetVersion     - the {@link ValueSet} version
     * @param offset              - the offset (for partial output)
     * @param count               - the count (for partial output)
     * @param includeDesignations - flag to include/exclude designations
     * @param includeDefinition   - flag to include/exclude definition
     * @param activeOnly          - flag to include/exclude active concepts
     * @param displayLanguage     - the display language
     * @param filter              - the concept code filter
     * @return
     */
    @GetMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrg(@PathVariable String org,
                                                      @RequestParam(name = URL) String url,
                                                      @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                      @RequestParam(name = OFFSET, required = false, defaultValue = "0") Integer offset,
                                                      @RequestParam(name = COUNT, required = false, defaultValue = "100") Integer count,
                                                      @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") Boolean includeDesignations,
                                                      @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") Boolean includeDefinition,
                                                      @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") Boolean activeOnly,
                                                      @RequestParam(name = DISPLAY_LANGUAGE, required = false) String displayLanguage,
                                                      @RequestParam(name = FILTER, required = false) String filter) {
        Parameters parameters = valueSetExpandParameters(url, valueSetVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param org        - the organization id
     * @param parameters - the input paramters
     * @return
     */
    @PostMapping(path = {"/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrg(@PathVariable String org, @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, ValueSet.class, EXPAND);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param org             - the organization id
     * @param id              - the {@link ValueSet} id
     * @param url             - the {@link ValueSet} url
     * @param valueSetVersion - the {@link ValueSet} version
     * @param code            - the concept code
     * @param system          - the {@link CodeSystem} url
     * @param systemVersion   - the {@link CodeSystem} version
     * @param display         - the display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @GetMapping(path = {"/{id}/$validate-code", "/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrgAndId(@PathVariable String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION, required = false) String pathVersion,
                                                             @RequestParam(name = URL, required = false) String url,
                                                             @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                             @RequestParam(name = CODE) String code,
                                                             @RequestParam(name = SYSTEM) String system,
                                                             @RequestParam(name = SYSTEM_VERSION, required = false) String systemVersion,
                                                             @RequestParam(name = DISPLAY, required = false) String display,
                                                             @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = valueSetVCParameters(url, EMPTY, isValid(pathVersion) ? pathVersion : valueSetVersion, code, system, systemVersion, display,
                displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $validate-code.
     *
     * @param org        - the organization id
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return
     */
    @PostMapping(path = {"/{id}/$validate-code", "/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateValueSetByOrgAndId(@PathVariable String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION, required = false) String pathVersion,
                                                             @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param org                 - the organization id
     * @param id                  - the {@link ValueSet} id
     * @param url                 - the {@link ValueSet} url
     * @param valueSetVersion     - the {@link ValueSet} version
     * @param offset              - the offset (for partial output)
     * @param count               - the count (for partial output)
     * @param includeDesignations - flag to include/exclude designations
     * @param includeDefinition   - flag to include/exclude definition
     * @param activeOnly          - flag to include/exclude active concepts
     * @param displayLanguage     - the display language
     * @param filter              - the concept code filter
     * @return
     */
    @GetMapping(path = {"/{id}/$expand", "/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrgAndId(@PathVariable String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION, required = false) String pathVersion,
                                                           @RequestParam(name = URL, required = false) String url,
                                                           @RequestParam(name = VALUESET_VERSION, required = false) String valueSetVersion,
                                                           @RequestParam(name = OFFSET, required = false, defaultValue = "0") Integer offset,
                                                           @RequestParam(name = COUNT, required = false, defaultValue = "100") Integer count,
                                                           @RequestParam(name = INCLUDE_DESIGNATIONS, defaultValue = "true") Boolean includeDesignations,
                                                           @RequestParam(name = INCLUDE_DEFINITION, defaultValue = "false") Boolean includeDefinition,
                                                           @RequestParam(name = ACTIVE_ONLY, defaultValue = "true") Boolean activeOnly,
                                                           @RequestParam(name = DISPLAY_LANGUAGE, required = false) String displayLanguage,
                                                           @RequestParam(name = FILTER, required = false) String filter) {
        Parameters parameters = valueSetExpandParameters(url, isValid(pathVersion) ? pathVersion : valueSetVersion, offset, count, includeDesignations,
                includeDefinition, activeOnly, displayLanguage, filter, formatOrg(org));
        return handleFhirOperation(parameters, ValueSet.class, EXPAND, id);
    }

    /**
     * Perform {@link ValueSet} $expand.
     *
     * @param org        - the organization id
     * @param id         - the {@link ValueSet} id
     * @param parameters - the input paramters
     * @return
     */
    @PostMapping(path = {"/{id}/$expand", "/{id}/version/{version}/$expand"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> expandValueSetByOrgAndId(@PathVariable String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION, required = false) String pathVersion,
                                                           @RequestBody String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        if (isValid(pathVersion))
            params.setParameter(VALUESET_VERSION, pathVersion);
        return handleFhirOperation(params, ValueSet.class, EXPAND, id);
    }

}


