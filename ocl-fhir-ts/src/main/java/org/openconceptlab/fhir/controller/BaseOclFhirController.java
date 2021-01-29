package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.getIdentifier;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.FW_SLASH;

@Component
public class BaseOclFhirController {

    CodeSystemResourceProvider codeSystemResourceProvider;
    ValueSetResourceProvider valueSetResourceProvider;
    OclFhirUtil oclFhirUtil;

    @Autowired
    public BaseOclFhirController(CodeSystemResourceProvider codeSystemResourceProvider,
                             ValueSetResourceProvider valueSetResourceProvider,
                             OclFhirUtil oclFhirUtil) {
        this.codeSystemResourceProvider = codeSystemResourceProvider;
        this.valueSetResourceProvider = valueSetResourceProvider;
        this.oclFhirUtil = oclFhirUtil;
    }

    protected ResponseEntity<String> handleSearchResource(final Class<? extends MetadataResource> resourceClass, final String... args) {
        try {
            String resource = searchResource(resourceClass, args);
            return ResponseEntity.ok(resource);
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    protected ResponseEntity<String> handleDeleteResource(final Class<? extends MetadataResource> resourceClass, final String id,
                                                          final String version, final String owner, final String auth) {
        try {
            performDelete(resourceClass.getSimpleName(), id, version, owner, auth);
            return ResponseEntity.noContent().build();
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    protected ResponseEntity<String> handleFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        try {
            return ResponseEntity.ok(oclFhirUtil.getResourceAsString(performFhirOperation(parameters, type, operation)));
        } catch (BaseServerResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBody());
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    protected String searchResource(final Class<? extends MetadataResource> resourceClass, final String... filters) {
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
        return oclFhirUtil.getResourceAsString(bundle);
    }

    protected Parameters performFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        return oclFhirUtil.getClient()
                .operation()
                .onType(type)
                .named(operation)
                .withParameters(parameters)
                .execute();
    }

    protected void performCreate(MetadataResource resource, String auth) {
        oclFhirUtil.getClient()
                .create()
                .resource(resource).withAdditionalHeader(AUTHORIZATION, auth)
                .execute();
    }

    protected void performDelete(String type, String id, String version, String owner, String auth) {
        oclFhirUtil.getClient()
                .delete()
                .resourceById(new IdType(type + FW_SLASH + "1"))
                .withAdditionalHeader(ID, id)
                .withAdditionalHeader(VERSION, version)
                .withAdditionalHeader(OWNER, owner)
                .withAdditionalHeader(AUTHORIZATION, auth)
        .execute();
    }

    protected Parameters generateParameters(String code, String displayLanguage, String owner) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISP_LANG).setValue(new CodeType(displayLanguage));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    protected Parameters lookupParameters(String system, String code, String version, String displayLanguage, String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        return parameters;
    }

    protected Parameters codeSystemVCParameters(String url, String code, String version, String display, String displayLanguage,
                                              String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(newStringType(display));
        return parameters;
    }

    protected Parameters valueSetVCParameters(String url, String valueSetId, String valueSetVersion, String code, String system, String systemVersion,
                                            String display, String displayLanguage, String owner) {
        Parameters parameters = generateParameters(code, displayLanguage, owner);
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        if (isValid(url))
            parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(valueSetId))
            parameters.addParameter().setName("valueSetId").setValue(newStringType(valueSetId));
        if (isValid(systemVersion))
            parameters.addParameter().setName(SYSTEM_VERSION).setValue(newStringType(systemVersion));
        if (isValid(valueSetVersion))
            parameters.addParameter().setName(VALUESET_VERSION).setValue(newStringType(valueSetVersion));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(newStringType(display));
        return parameters;
    }

    protected Parameters valueSetExpandParameters(String url, String valueSetVersion, Integer offset, Integer count, Boolean includeDesignations,
                                                Boolean includeDefinition, Boolean activeOnly, String displayLanguage, String filter, String owner) {
        Parameters parameters = new Parameters();
        if (isValid(url))
            parameters.addParameter().setName(URL).setValue(newUri(url));
        if (isValid(valueSetVersion))
            parameters.addParameter().setName(VALUESET_VERSION).setValue(newStringType(valueSetVersion));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISPLAY_LANGUAGE).setValue(newStringType(displayLanguage));
        if (isValid(filter))
            parameters.addParameter().setName(FILTER).setValue(newStringType(filter));
        parameters.addParameter().setName(OFFSET).setValue(newInteger(offset));
        parameters.addParameter().setName(COUNT).setValue(newInteger(count));
        parameters.addParameter().setName(INCLUDE_DESIGNATIONS).setValue(newBoolean(includeDesignations));
        parameters.addParameter().setName(INCLUDE_DEFINITION).setValue(newBoolean(includeDefinition));
        parameters.addParameter().setName(ACTIVE_ONLY).setValue(newBoolean(activeOnly));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    protected Parameters conceptMapTranslateParameters(String url, String conceptMapVersion, String system,
                                                       String version, String code, String targetSystem, String owner) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName(URL).setValue(newUri(url));
        if (isValid(conceptMapVersion))
            parameters.addParameter().setName(CONCEPT_MAP_VERSION).setValue(newStringType(conceptMapVersion));
        parameters.addParameter().setName(SYSTEM).setValue(newUri(system));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(targetSystem))
            parameters.addParameter().setName(TARGET_SYSTEM).setValue(newUri(targetSystem));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    protected static String formatOrg(String org) {
        return ORG_ + org;
    }

    protected static String formatUser(String user) {
        return USER_ + user;
    }

    protected ResponseEntity<String> validate(String user, String resId, Optional<Identifier> acsnOpt,
                                              String ownerType, String ownerId) {
        ResponseEntity<String> response1 = validateId(resId);
        if (response1 != null) return response1;
        return validateAccessionId(acsnOpt, ownerType, ownerId);
    }

    protected ResponseEntity<String> validateId(String id) {
        if (!isValid(id))
            return badRequest("The id can not be empty.");
        return null;
    }

    protected ResponseEntity<String> validateAccessionId(Optional<Identifier> acsnOpt, String ownerType, String ownerId) {
        if (acsnOpt.isPresent()) {
            String[] values = formatExpression(acsnOpt.get().getValue()).split(FW_SLASH);
            if (!(values.length >= 3 && ownerType.equals(values[1]) && ownerId.equals(values[2]))) {
                return badRequest("The Accession id does not match with given request.");
            }
        }
        return null;
    }

    protected void addIdentifier(List<Identifier> identifiers, String ownerType, String ownerId, String resType, String resId) {
        identifiers.add(getIdentifier(FW_SLASH + ownerType + FW_SLASH + ownerId + FW_SLASH + resType + FW_SLASH + resId + FW_SLASH).get());
    }

}
