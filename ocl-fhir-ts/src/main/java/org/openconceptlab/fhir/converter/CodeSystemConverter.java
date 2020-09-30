package org.openconceptlab.fhir.converter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemFilterComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.FilterOperator;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.StringType;
import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.ConceptsName;
import org.openconceptlab.fhir.model.LocalizedText;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.model.Source;
import org.openconceptlab.fhir.model.UserProfile;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The CodeSystemConverter.
 * @author hp11
 */
@Component
public class CodeSystemConverter {

	JsonParser parser = new JsonParser();
	
    public void convertToCodeSystem(List<CodeSystem> codeSystems, List<Source> sources, boolean includeConcepts, String version) {
        // filter by version if provided
    	String versionId = StringUtils.isNotBlank(version) ? version : HEAD;
        sources.forEach(source -> {
            if(source.getIsActive() && source.getVersion().equalsIgnoreCase(versionId)) {
                // convert to base
            	CodeSystem codeSystem = toBaseCodeSystem(source);
                if (includeConcepts) {
                	// add concepts
                    addConceptsToCodeSystem(codeSystem, source);
                }
                // add extras
                addExtras(codeSystem, source.getExtras());
                codeSystems.add(codeSystem);
            }
        });
    }

    public CodeSystem toBaseCodeSystem(final Source source){
        CodeSystem codeSystem = new CodeSystem();
        // set id
        codeSystem.setId(source.getMnemonic());
        // set version
        codeSystem.setVersion(source.getVersion());
        // set name
        codeSystem.setName(source.getName());
        // set title
        if(StringUtils.isNotBlank(source.getFullName())) {
            codeSystem.setTitle(source.getFullName());
        }
        // set publisher
        Organization organization = source.getOrganization();
        UserProfile user = source.getUserId();
        if(organization != null) {
            codeSystem.setPublisher(ORG_ + organization.getMnemonic());
        } else if(user != null) {
            codeSystem.setPublisher(USER_ + user.getUsername());
        }
        // set description
        if(StringUtils.isNotBlank(source.getDescription())) {
            codeSystem.setDescription(source.getDescription());
        }
        // set count
        codeSystem.setCount(source.getActiveConcepts());
        // set Url
        if(StringUtils.isNotBlank(source.getExternalId())) {
        	codeSystem.setUrl(source.getExternalId());
        }
        // set status
        addStatus(codeSystem, source.getIsActive(), source.getRetired() != null ? source.getRetired() : false,
				source.getReleased());
        return codeSystem;
    }
    
    private void addStatus(CodeSystem codeSystem, boolean active, boolean retired, boolean released) {
    	if(active || released) {
    		codeSystem.setStatus(PublicationStatus.ACTIVE);
    	} else if(retired) {
    		codeSystem.setStatus(PublicationStatus.RETIRED);
    	} else {
    		codeSystem.setStatus(PublicationStatus.UNKNOWN);
    	}
    }

    public void addConceptsToCodeSystem(final CodeSystem codeSystem, final Source source) {
    	for(Concept concept : source.getConcepts()) {
    		CodeSystem.ConceptDefinitionComponent definitionComponent = new CodeSystem.ConceptDefinitionComponent();
    		// set code
    		definitionComponent.setCode(concept.getMnemonic());
    		// set display
    		definitionComponent.setDisplay(concept.getName());
    		// TODO - add definition
    		// set designation
    		for(ConceptsName name : concept.getConceptsNames()) {
    			ConceptDefinitionDesignationComponent designation = new ConceptDefinitionDesignationComponent();
    			LocalizedText lt = name.getLocalizedText();
    			if(lt != null) {
    				designation.setLanguage(lt.getLocale());
    				designation.setValue(lt.getName());
    				definitionComponent.getDesignation().add(designation);
    			}
    		}
    		// set property - concept_class and data_type
    		definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(FILTER_CODE_CC),
    				new StringType(concept.getConceptClass())));    		
    		definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(FILTER_CODE_DT),
    				new StringType(concept.getDatatype())));
    		// set description
    		if(StringUtils.isNotBlank(concept.getDescription())) definitionComponent.setDefinition(concept.getDescription());
    		// add concept in CodeSystem
    		codeSystem.getConcept().add(definitionComponent);    		
    	}
    }
    
    private void addExtras(CodeSystem codeSystem, String extras) {
    	if(StringUtils.isNotBlank(extras)) {
    		JsonObject obj = parseExtras(extras);
    		// filters
    		JsonArray filters = obj.getAsJsonArray(FILTERS);
    		if (filters != null && filters.isJsonArray()) {
    			for(JsonElement je : filters) {
    				JsonObject filter = je.getAsJsonObject();
    				if(filter.get(CODE) != null) {
    					String code = filter.get(CODE).getAsString();
    					if(FILTER_CODE_CC.equals(code)) {
    						addFilter(codeSystem, FILTER_CODE_CC, filter.get(DESC), filter.get(OPERATOR),
    								filter.get(VALUE));
    					} else if (FILTER_CODE_DT.equals(code)) {
    						addFilter(codeSystem, FILTER_CODE_DT, filter.get(DESC), filter.get(OPERATOR),
    								filter.get(VALUE));
    					}
    				}
    			}
    		}
    		// identifier
    		JsonArray identifiers = obj.getAsJsonArray(IDENTIFIERS);
    		codeSystem.setIdentifier(getIdentifiers(identifiers));

    		// purpose
    		if(isValidElement(obj.get(PURPOSE)))
    			codeSystem.setPurpose(obj.get(PURPOSE).getAsString());
    		// copyright
    		if(isValidElement(obj.get(COPYRIGHT)))
    			codeSystem.setCopyright(obj.get(COPYRIGHT).getAsString());
    	}
    }
    
    private void addFilter(CodeSystem codeSystem, String code, JsonElement description, JsonElement operator, 
    		JsonElement value) {
    	if(StringUtils.isNotBlank(code)) {
    		CodeSystemFilterComponent c = new CodeSystemFilterComponent();
    		c.setCode(code);
    		if (description != null && description.getAsString() != null)
    			c.setDescription(description.getAsString());
    		if (operator != null && operator.getAsString() != null) {
    			if(allowedFilterOperators.contains(operator.getAsString())) {
    				c.addOperator(FilterOperator.fromCode(operator.getAsString()));
    			}
    		}
    		if (value != null && value.getAsString() != null)
    			c.setValue(value.getAsString());
    		codeSystem.getFilter().add(c);
    	}
    }

    public void toSource(final CodeSystem codeSystem){

    }

}
