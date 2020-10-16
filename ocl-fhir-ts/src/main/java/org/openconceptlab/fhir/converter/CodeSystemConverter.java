package org.openconceptlab.fhir.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemFilterComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeSystem.FilterOperator;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.openconceptlab.fhir.model.*;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The CodeSystemConverter.
 * @author harpatel1
 */
@Component
public class CodeSystemConverter {

	JsonParser parser = new JsonParser();

	private SourceRepository sourceRepository;
	private ConceptRepository conceptRepository;
	private OclFhirUtil oclFhirUtil;
	private UserProfile oclUser;

	@Autowired
	public CodeSystemConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil
			, UserProfile oclUser) {
		this.sourceRepository = sourceRepository;
		this.conceptRepository = conceptRepository;
		this.oclFhirUtil = oclFhirUtil;
		this.oclUser = oclUser;
	}

	public List<CodeSystem> convertToCodeSystem(List<Source> sources, boolean includeConcepts, String version) {
		List<CodeSystem> codeSystems = new ArrayList<>();
				sources.forEach(source -> {
        	// convert to base
			CodeSystem codeSystem = toBaseCodeSystem(source);
			if (includeConcepts) {
				// add concepts
				addConceptsToCodeSystem(codeSystem, source);
			}
			codeSystems.add(codeSystem);
        });
		return codeSystems;
    }

	private CodeSystem toBaseCodeSystem(final Source source){
        CodeSystem codeSystem = new CodeSystem();
        // Url
        if(StringUtils.isNotBlank(source.getExternalId())) {
        	codeSystem.setUrl(source.getExternalId());
        }
        // id
        codeSystem.setId(source.getMnemonic());
        // identifier
		getIdentifier(source.getUri())
				.ifPresent(i -> codeSystem.getIdentifier().add(i));
        // version
        codeSystem.setVersion(source.getVersion());
        // name
        codeSystem.setName(source.getName());
        // title
        if(StringUtils.isNotBlank(source.getFullName())) {
            codeSystem.setTitle(source.getFullName());
        }
        // status
        addStatus(codeSystem, source.getIsActive(), source.getRetired() != null ? source.getRetired() : false,
				source.getReleased() != null ? source.getReleased() : false);
		// language
		codeSystem.setLanguage(source.getDefaultLocale());

        // description
        if(StringUtils.isNotBlank(source.getDescription())) {
            codeSystem.setDescription(source.getDescription());
        }
        // count
        codeSystem.setCount((int) source.getConcepts().stream().filter(c -> c.getIsLatestVersion()).count());
        // property
		addProperty(codeSystem);
        return codeSystem;
    }

	private void addProperty(final CodeSystem codeSystem) {
		// concept class
		codeSystem.getProperty().add(getPropertyComponent(CONCEPT_CLASS,
				SYSTEM_CC,
				DESC_CC,
				CodeSystem.PropertyType.STRING));
		// data type
		codeSystem.getProperty().add(getPropertyComponent(DATATYPE,
				SYSTEM_DT,
				DESC_DT,
				CodeSystem.PropertyType.STRING));
		// inactive - http://hl7.org/fhir/concept-properties
		codeSystem.getProperty().add(getPropertyComponent(INACTIVE, SYSTEM_HL7_CONCEPT_PROP,
				DESC_HL7_CONCEPT_PROP, CodeSystem.PropertyType.CODING));
	}

	private CodeSystem.PropertyComponent getPropertyComponent(String code, String system, String description, CodeSystem.PropertyType type) {
		CodeSystem.PropertyComponent component = new CodeSystem.PropertyComponent();
		component.setCode(code);
		component.setUri(system);
		component.setDescription(description);
		component.setType(type);
		return component;
	}

	private void addConceptsToCodeSystem(final CodeSystem codeSystem, final Source source) {
    	for(Concept concept : source.getConcepts()) {
    		if (concept.getIsLatestVersion()) {
				CodeSystem.ConceptDefinitionComponent definitionComponent = new CodeSystem.ConceptDefinitionComponent();
				// code
				definitionComponent.setCode(concept.getMnemonic());
				// display
				List<LocalizedText> names = concept.getConceptsNames().stream()
						.filter(c -> c.getLocalizedText() != null)
						.map(d -> d.getLocalizedText())
						.collect(Collectors.toList());
				definitionComponent.setDisplay(oclFhirUtil.getDefinition(names, source.getDefaultLocale()));

				// definition
				List<LocalizedText> definitions = concept.getConceptsDescriptions().stream()
						.filter(c -> c.getLocalizedText() != null)
						.filter(c -> isValid(c.getLocalizedText().getType()) && "definition".equalsIgnoreCase(c.getLocalizedText().getType()))
						.map(d -> d.getLocalizedText())
						.collect(Collectors.toList());
				definitionComponent.setDefinition(oclFhirUtil.getDefinition(definitions, source.getDefaultLocale()));

				// designation
				addConceptDesignation(concept, definitionComponent);

				// property - concept_class, data_type, ,inactive
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(CONCEPT_CLASS),
						new StringType(concept.getConceptClass())));
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(DATATYPE),
						new StringType(concept.getDatatype())));
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(INACTIVE),
						new BooleanType(!concept.getIsActive())));
				// add concept in CodeSystem
				codeSystem.getConcept().add(definitionComponent);
			}
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
    					if(CONCEPT_CLASS.equals(code)) {
    						addFilter(codeSystem, CONCEPT_CLASS, filter.get(DESC), filter.get(OPERATOR),
    								filter.get(VALUE));
    					} else if (DATATYPE.equals(code)) {
    						addFilter(codeSystem, DATATYPE, filter.get(DESC), filter.get(OPERATOR),
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

	/**
	 * TODO: Update when ready to implement POST
	 * @param codeSystem
	 */
	public void validateCodeSystem(CodeSystem codeSystem) {
		Optional<Identifier> id = codeSystem.getIdentifier().stream().filter(i -> i.getType().hasCoding("http://hl7.org/fhir/v2/0203", "ACSN"))
				.filter(i -> "http://fhir.openconceptlab.org".equals(i.getSystem()))
				.filter(i -> isValid(i.getValue()))
				.findFirst();
		String url = codeSystem.getUrl();
		if (!id.isPresent() && !isValid(url)) {
			throw new UnprocessableEntityException("CodeSystem must have either Identifier or URL value.");
		}
		// check for unique id
		if(id.isPresent()) {
			if (!sourceRepository.findByMnemonicAndPublicAccessIn(id.get().getValue(), publicAccess).isEmpty()) {
				throw new UnprocessableEntityException(String.format("The CodeSystem of Id '%s' already exists", id.get().getValue()));
			}
			codeSystem.setId(id.get().getValue());
		}
		// check for unique URL
		if(isValid(url) && !sourceRepository.findByExternalIdIs(url).isEmpty())
			throw new UnprocessableEntityException(String.format("The CodeSystem of URL '%s' already exists", url));

		// check publisher
		validatePublisher(codeSystem.getPublisher());

		// meta.updatedAt = source.updated_at
		toSource(codeSystem);
	}

	/**
	 * TODO: Update when ready to implement POST
	 * @param codeSystem
	 */
    public void toSource(final CodeSystem codeSystem){
		Source source = new Source();
		source.setMnemonic(codeSystem.getId());
		source.setExternalId(codeSystem.getUrl());
		source.setCreatedBy(new UserProfile(oclUser.getId()));
		source.setUpdatedBy(new UserProfile(oclUser.getId()));
		source.setIsActive(PublicationStatus.ACTIVE.equals(codeSystem.getStatus()));
		source.setRetired(PublicationStatus.RETIRED.equals(codeSystem.getStatus()));
		if (isValid(codeSystem.getVersion())) source.setVersion(codeSystem.getVersion());
		if (isValid(codeSystem.getName())) source.setName(codeSystem.getName());
		if (isValid(codeSystem.getLanguage())) source.setDefaultLocale(codeSystem.getLanguage());
		source.setActiveConcepts(codeSystem.getCount() == 0 ? codeSystem.getConcept().size() : codeSystem.getCount());
		source.setExtras(getExtras(codeSystem));
		if (isValid(source.getMnemonic())) {
			sourceRepository.save(source);
		} else {
			source.setMnemonic("TEMP");
			Source temp = sourceRepository.save(source);
			sourceRepository.updateMnemonic(temp.getId());
		}
    }

    private String getExtras(final CodeSystem codeSystem) {
    	CodeSystem system = codeSystem.copy();
		nullifyBaseFields(system);
		return toFhirString(system);
    }

    private void nullifyBaseFields(final CodeSystem codeSystem) {
		codeSystem.setIdentifier(null);
		codeSystem.setUrl(null);
		codeSystem.setStatus(null);
		codeSystem.setVersion(null);
		codeSystem.setName(null);
		codeSystem.setLanguage(null);
	}
}







