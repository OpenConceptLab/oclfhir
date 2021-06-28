package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;

/**
 * The OclCapabilityStatementProvider.
 * @author harpatel1
 */
@Component
public class OclCapabilityStatementProvider extends ServerCapabilityStatementProvider {

    @Autowired
    private ApplicationProperties properties;

    public static final String OPEN_CONCEPT_LAB = "Open Concept Lab";
    public static final String OPEN_CONCEPT_LAB_FHIR_CAPABILITY_STATEMENT = "Open Concept Lab FHIR Capability Statement";
    public static final String OPEN_CONCEPT_LAB_FHIR_API = "Open Concept Lab FHIR API";
    public static final String THE_CANONICAL_URL_OF_THE_CODE_SYSTEM = "The canonical url of the code system";
    public static final String THE_CANONICAL_URL_OF_THE_VALUESET = "The canonical url of the value set";
    public static final String THE_CANONICAL_URL_OF_THE_CONCEPTMAP = "The canonical url of the concept map";
    public static final String THE_BUSINESS_VERSION_OF_THE_CODE_SYSTEM = "The business version of the code system";
    public static final String THE_BUSINESS_VERSION_OF_THE_VALUESET = "The business version of the value set";
    public static final String THE_BUSINESS_VERSION_OF_THE_CONCEPTMAP = "The business version of the concept map";
    public static final String CODE_SYSTEM_PROFILE = "http://hl7.org/fhir/StructureDefinition/CodeSystem";
    public static final String VALUESET_PROFILE = "http://hl7.org/fhir/StructureDefinition/ValueSet";
    public static final String CONCEPTMAP_PROFILE = "http://hl7.org/fhir/StructureDefinition/ConceptMap";
    public static final String COMMA = ", ";

    public ApplicationProperties getProperties() {
        return properties;
    }

    @Override
    public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
        CapabilityStatement capabilityStatement = new CapabilityStatement();
        capabilityStatement.setVersion(getProperties().getOclFhirVersion());
        CapabilityStatement generated = getSuperServerConformance(theRequest, theRequestDetails);
        capabilityStatement.setStatus(Enumerations.PublicationStatus.ACTIVE);
        capabilityStatement.setPublisher(OPEN_CONCEPT_LAB);
        capabilityStatement.setDate(new Date());
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        capabilityStatement.setTitle(OPEN_CONCEPT_LAB_FHIR_CAPABILITY_STATEMENT);
        capabilityStatement.setSoftware(generated.getSoftware());
        CapabilityStatement.CapabilityStatementImplementationComponent implementation = capabilityStatement.getImplementation();
        implementation.setDescription(OPEN_CONCEPT_LAB_FHIR_API);
        implementation.setUrl(generated.getImplementation().getUrl());
        capabilityStatement.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        capabilityStatement.setFormat(generated.getFormat());

        List<CapabilityStatement.CapabilityStatementRestComponent> restComponents = capabilityStatement.getRest();
        CapabilityStatement.CapabilityStatementRestComponent codeSystemRestComponent = restComponent(CodeSystem.class.getSimpleName(), CODE_SYSTEM_PROFILE);
        CapabilityStatement.CapabilityStatementRestComponent valueSetRestComponent = restComponent(ValueSet.class.getSimpleName(), VALUESET_PROFILE);
        CapabilityStatement.CapabilityStatementRestComponent conceptMapRestComponent = restComponent(ConceptMap.class.getSimpleName(), CONCEPTMAP_PROFILE);

        codeSystemRestComponent.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
        valueSetRestComponent.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
        conceptMapRestComponent.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

        String codeSystemLookupDefinition = (getOperationDefinitionPrefix(theRequestDetails) + "OperationDefinition/" + "CodeSystem--lookup?_format=json");
        String codeSystemValidateCodeDefinition = (getOperationDefinitionPrefix(theRequestDetails) + "OperationDefinition/" + "CodeSystem--validate-code?_format=json");
        String valueSetValidateCodeDefinition = (getOperationDefinitionPrefix(theRequestDetails) + "OperationDefinition/" + "ValueSet--validate-code?_format=json");
        String valueSetExpandDefinition = (getOperationDefinitionPrefix(theRequestDetails) + "OperationDefinition/" + "ValueSet--expand?_format=json");
        String conceptMapTranslateDefinition = (getOperationDefinitionPrefix(theRequestDetails) + "OperationDefinition/" + "ConceptMap--translate?_format=json");

        codeSystemRestComponent.getOperation().add(operationComponent("lookup", codeSystemLookupDefinition,
                "Supported parameters: " + CODE + COMMA + SYSTEM + COMMA + VERSION + COMMA + DISP_LANG));
        codeSystemRestComponent.getOperation().add(operationComponent("validate-code", codeSystemValidateCodeDefinition,
                "Supported parameters: " + URL + COMMA + CODE + COMMA + VERSION + COMMA + DISPLAY + COMMA +
                        DISP_LANG + COMMA + CODING));

        valueSetRestComponent.getOperation().add(operationComponent("validate-code", valueSetValidateCodeDefinition,
                "Supported parameters: " + URL + COMMA + VALUESET_VERSION + COMMA + CODE + COMMA + SYSTEM + COMMA +
                        SYSTEM_VERSION + COMMA + DISPLAY + COMMA + DISP_LANG + COMMA + CODING));
        valueSetRestComponent.getOperation().add(operationComponent("expand", valueSetExpandDefinition,
                "Supported parameters: " + URL + COMMA + VALUESET_VERSION + COMMA + OFFSET + COMMA + COUNT + COMMA +
                        INCLUDE_DESIGNATIONS + COMMA + INCLUDE_DEFINITION + COMMA + ACTIVE_ONLY + COMMA + DISPLAY_LANGUAGE + COMMA +
                        EXCLUDE_SYSTEM + COMMA + SYSTEMVERSION + COMMA + FILTER));

        conceptMapRestComponent.getOperation().add(operationComponent("translate", conceptMapTranslateDefinition,
                "Supported parameters: " + URL + COMMA + CONCEPT_MAP_VERSION + COMMA + SYSTEM + COMMA + VERSION + COMMA +
                        CODE + COMMA + CODING + COMMA + TARGET_SYSTEM));

        restComponents.add(codeSystemRestComponent);
        restComponents.add(valueSetRestComponent);
        restComponents.add(conceptMapRestComponent);
        return capabilityStatement;
    }

    protected CapabilityStatement getSuperServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
        return super.getServerConformance(theRequest, theRequestDetails);
    }

    private CapabilityStatement.CapabilityStatementRestResourceOperationComponent operationComponent(String name, String definition, String note) {
        CapabilityStatement.CapabilityStatementRestResourceOperationComponent c = new CapabilityStatement.CapabilityStatementRestResourceOperationComponent();
        c.setName(name);
        c.setDefinition(definition);
        c.setDocumentation(note);
        return c;
    }


    private CapabilityStatement.CapabilityStatementRestComponent restComponent(String type, String profile) {
        CapabilityStatement.CapabilityStatementRestComponent component = new CapabilityStatement.CapabilityStatementRestComponent();
        List<CapabilityStatement.CapabilityStatementRestResourceComponent> resources = component.getResource();
        CapabilityStatement.CapabilityStatementRestResourceComponent resourceComponent = new CapabilityStatement.CapabilityStatementRestResourceComponent();
        resources.add(resourceComponent);

        List<CapabilityStatement.ResourceInteractionComponent> interactions = getInteractions();
        List<CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent> searchParams = getSearchParams(type);

        resourceComponent.setType(type);
        resourceComponent.setProfile(profile);
        resourceComponent.setInteraction(interactions);
        resourceComponent.setSearchParam(searchParams);
        return component;
    }

    private List<CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent> getSearchParams(String type) {
        List<CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent> searchParams = new ArrayList<>();
        if (CodeSystem.class.getSimpleName().equals(type)) {
            searchParams.add(searchParam(URL, Enumerations.SearchParamType.STRING, THE_CANONICAL_URL_OF_THE_CODE_SYSTEM));
            searchParams.add(searchParam(VERSION, Enumerations.SearchParamType.STRING, THE_BUSINESS_VERSION_OF_THE_CODE_SYSTEM));
        } else if (ValueSet.class.getSimpleName().equals(type)) {
            searchParams.add(searchParam(URL, Enumerations.SearchParamType.STRING, THE_CANONICAL_URL_OF_THE_VALUESET));
            searchParams.add(searchParam(VERSION, Enumerations.SearchParamType.STRING, THE_BUSINESS_VERSION_OF_THE_VALUESET));
        }
        return searchParams;
    }

    private List<CapabilityStatement.ResourceInteractionComponent> getInteractions() {
        List<CapabilityStatement.ResourceInteractionComponent> interactions = new ArrayList<>();
        interactions.add(interactionType(CapabilityStatement.TypeRestfulInteraction.READ));
        interactions.add(interactionType(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE));
        interactions.add(interactionType(CapabilityStatement.TypeRestfulInteraction.CREATE));
        interactions.add(interactionType(CapabilityStatement.TypeRestfulInteraction.UPDATE));
        interactions.add(interactionType(CapabilityStatement.TypeRestfulInteraction.DELETE));
        return interactions;
    }


    private CapabilityStatement.ResourceInteractionComponent interactionType(CapabilityStatement.TypeRestfulInteraction interaction) {
        CapabilityStatement.ResourceInteractionComponent c = new CapabilityStatement.ResourceInteractionComponent();
        c.setCode(interaction);
        return c;
    }

    private CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent searchParam(String name, Enumerations.SearchParamType type, String note) {
        CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent c = new CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent();
        c.setName(name);
        c.setType(type);
        c.setDocumentation(note);
        return c;
    }
}
