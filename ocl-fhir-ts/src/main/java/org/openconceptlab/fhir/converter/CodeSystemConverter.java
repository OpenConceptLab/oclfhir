package org.openconceptlab.fhir.converter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.codesystems.PublicationStatus;
import org.openconceptlab.fhir.model.*;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirConstants;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * The CodeSystemConverter.
 * @author harpatel1
 */
@Component
public class CodeSystemConverter extends BaseConverter {

	public static final String DEFAULT_RES_VERSION = "0.1";
	private static final Log log = LogFactory.getLog(CodeSystemConverter.class);
	public CodeSystemConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
							   UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
							   AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
							   OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
							   MappingRepository mappingRepository) {
		super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
				userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, mappingRepository);
	}

	public List<CodeSystem> convertToCodeSystem(List<Source> sources, boolean includeConcepts, int page) {
		List<CodeSystem> codeSystems = new ArrayList<>();
		if (!includeConcepts) {
			int offset = page * 10;
			int count = 10;
			sources = paginate(sources, offset, count);
		}
		sources.forEach(source -> {
			// convert to base
			CodeSystem codeSystem = toBaseCodeSystem(source);
			if (includeConcepts) {
				// add concepts
				addConceptsToCodeSystem(codeSystem, source, page);
			}
			codeSystems.add(codeSystem);
		});
		return codeSystems;
	}

	private CodeSystem toBaseCodeSystem(final Source source){
        CodeSystem codeSystem = new CodeSystem();
        // Url
        if(StringUtils.isNotBlank(source.getCanonicalUrl())) {
        	codeSystem.setUrl(source.getCanonicalUrl());
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
        addStatus(codeSystem, source.getRetired() != null ? source.getRetired() : False,
				source.getReleased() != null ? source.getReleased() : False);
		// language
		codeSystem.setLanguage(source.getDefaultLocale());

        // description
        if(StringUtils.isNotBlank(source.getDescription()))
            codeSystem.setDescription(source.getDescription());
        // count
		codeSystem.setCount(conceptRepository.findConceptCountInSource(source.getId()));
        // property
		addProperty(codeSystem);
		// publisher
		if (isValid(source.getPublisher()))
			codeSystem.setPublisher(source.getPublisher());
		// override default identifier with database value
		// identifier, contact, jurisdiction
		addJsonFields(codeSystem, isValid(source.getIdentifier()) && !EMPTY_JSON.equals(source.getIdentifier()) ? source.getIdentifier() : EMPTY,
				source.getContact(), source.getJurisdiction());
		// purpose
		if (isValid(source.getPurpose()))
			codeSystem.setPurpose(source.getPurpose());
		// copyright
		if (isValid(source.getCopyright()))
			codeSystem.setCopyright(source.getCopyright());
		// content_type
		if (isValid(source.getContentType())) {
			Optional<String> content = Stream.of(CodeSystem.CodeSystemContentMode.values()).map(Enum::toString).filter(m -> source.getContentType().equalsIgnoreCase(m))
					.findAny();
			if (content.isPresent()) {
				codeSystem.setContent(CodeSystem.CodeSystemContentMode.fromCode(content.get().toLowerCase()));
			} else {
				codeSystem.setContent(CodeSystem.CodeSystemContentMode.NULL);
			}
		} else {
			codeSystem.setContent(CodeSystem.CodeSystemContentMode.NULL);
		}
		// revision date
		if (source.getRevisionDate() != null)
			codeSystem.setDate(source.getRevisionDate());
        return codeSystem;
    }

	private void addProperty(final CodeSystem codeSystem) {
		// concept class
		codeSystem.getProperty().add(getPropertyComponent(OclFhirConstants.CONCEPTCLASS,
				SYSTEM_CC,
				DESC_CC,
				CodeSystem.PropertyType.STRING));
		// data type
		codeSystem.getProperty().add(getPropertyComponent(OclFhirConstants.DATATYPE,
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

	private void addConceptsToCodeSystem(final CodeSystem codeSystem, final Source source, int page) {
		List<Concept> concepts = conceptRepository.findConcepts(source.getId(), PageRequest.of(page, 100));
		for (Concept concept : concepts) {
			CodeSystem.ConceptDefinitionComponent definitionComponent = new CodeSystem.ConceptDefinitionComponent();
			// code
			definitionComponent.setCode(concept.getMnemonic());
			// display
			List<LocalizedText> names = concept.getConceptsNames().stream()
					.filter(c -> c.getLocalizedText() != null)
					.map(ConceptsName::getLocalizedText)
					.collect(Collectors.toList());
			Optional<String> display = oclFhirUtil.getDisplayForLanguage(names, source.getDefaultLocale());
			definitionComponent.setDisplay(display.orElse(EMPTY));

			// definition
			List<LocalizedText> definitions = concept.getConceptsDescriptions().stream()
					.filter(c -> c.getLocalizedText() != null)
					.filter(c -> isValid(c.getLocalizedText().getType()) && DEFINITION.equalsIgnoreCase(c.getLocalizedText().getType()))
					.map(ConceptsDescription::getLocalizedText)
					.collect(Collectors.toList());
			Optional<String> definition = oclFhirUtil.getDisplayForLanguage(definitions, source.getDefaultLocale());
			definitionComponent.setDefinition(definition.orElse(EMPTY));

			// designation
			addConceptDesignation(concept, definitionComponent);

			// property - concept_class, data_type, ,inactive
			definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(OclFhirConstants.CONCEPTCLASS),
					new StringType(concept.getConceptClass())));
			definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(OclFhirConstants.DATATYPE),
					new StringType(concept.getDatatype())));
			definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(INACTIVE),
					new BooleanType(concept.getRetired())));
			// add concept in CodeSystem
			codeSystem.getConcept().add(definitionComponent);
		}
	}

    public Parameters getLookupParameters(final Source source, final CodeType code, final CodeType displayLanguage) {
		Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, code.getCode(), EMPTY);
		if (conceptOpt.isPresent()) {
			Concept concept = conceptOpt.get();
			Parameters parameters = new Parameters();
			parameters.addParameter(getParameter(OclFhirConstants.NAME, source.getName()));
			parameters.addParameter(getParameter(OclFhirConstants.VERSION, source.getVersion()));
			List<LocalizedText> names = oclFhirUtil.getNames(concept);
			getDisplayForLookUp(names, isValid(displayLanguage) ? displayLanguage.getCode() : EMPTY, source.getDefaultLocale())
					.ifPresent(display -> parameters.addParameter(getParameter(DISPLAY, display)));
			addDesignationParameters(parameters, names, getCode(displayLanguage));
			return parameters;
		} else {
			throw new ResourceNotFoundException("The code " + code.getCode() + " is invalid.");
		}
	}

	private Optional<String> anyDisplay(List<LocalizedText> names) {
		return names.stream().map(LocalizedText::getName).findFirst();
	}

	private Optional<String> getDisplayForLookUp(List<LocalizedText> names, String displayLanguage, String defaultLocale) {
		if (isValid(displayLanguage)) {
			return oclFhirUtil.getDisplayForLanguage(names, displayLanguage);
		}
		Optional<String> defaultLocaleDisp = oclFhirUtil.getDisplayForLanguage(names, defaultLocale);
		if (defaultLocaleDisp.isPresent()) {
			return defaultLocaleDisp;
		} else {
			return anyDisplay(names);
		}
	}

	private void addDesignationParameters(Parameters parameters, List<LocalizedText> names, String displayLanguage) {
		names.stream()
				.filter(name -> !isValid(displayLanguage) || name.getLocale().equals(displayLanguage))
				.forEach(name -> {
					parameters.addParameter().setName(DESIGNATION).setPart(getDesignationParameters(name));
				});
	}

	public Parameters validateCode(final Source source, final String code, final StringType display, final CodeType displayLanguage) {
		Parameters parameters = new Parameters();
		BooleanType result = new BooleanType(False);
		parameters.addParameter().setName(RESULT).setValue(result);
		Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, code, EMPTY);
		if (conceptOpt.isPresent()) {
			if (isValid(display)) {
				StringType updated = newStringType(display.getValue().replaceAll("^\"", "")
						.replaceAll("\"$", ""));
				List<LocalizedText> names = oclFhirUtil.getNames(conceptOpt.get());
				boolean match = oclFhirUtil.validateDisplay(names, updated, displayLanguage);
				if (!match) {
					parameters.addParameter().setName(MESSAGE).setValue(newStringType("Invalid display."));
				} else {
					result.setValue(True);
				}
			} else {
				result.setValue(True);
			}
		}
		return parameters;
	}

	private List<Parameters.ParametersParameterComponent> getDesignationParameters(final LocalizedText text) {
		List<Parameters.ParametersParameterComponent> componentList = new ArrayList<>();
		if (isValid(text.getLocale()))
			componentList.add(getParameter(LANGUAGE, new CodeType(text.getLocale())));
		if (isValid(text.getType()))
			componentList.add(getParameter(USE, new Coding(EMPTY, EMPTY, text.getType())));
		if (isValid(text.getName()))
			componentList.add(getParameter(VALUE, new StringType(text.getName())));
		return componentList;
	}

	public void createCodeSystem(CodeSystem codeSystem, String accessionId, String authToken) {
		// validate and authenticate
		OclEntity oclEntity = new OclEntity(codeSystem, accessionId, authToken);
		UserProfile user = oclEntity.getUserProfile();
		// base source
		Source source = toBaseSource(codeSystem, user, oclEntity.getAccessionId());
		// add parent and access
		addParent(source, oclEntity.getOwner());
		// add identifier, contact and jurisdiction
		addJsonStrings(codeSystem, source);
		// version-less source uri
		String value = source.getUri().substring(0, source.getUri().lastIndexOf(FW_SLASH));
		String versionLessSourceUri = value.substring(0, value.lastIndexOf(FW_SLASH)) + FW_SLASH;
		// add concepts
		List<Concept> concepts = toConcepts(codeSystem.getConcept(), codeSystem.getLanguage());
		concepts.forEach(c -> {
			c.setParent(source);
			c.setPublicAccess(source.getPublicAccess());
			c.setVersion("1");
			c.setIsLatestVersion(True);
			c.setReleased(c.getIsActive());
			c.setRetired(c.getIsActive());
			c.setDefaultLocale(source.getDefaultLocale());
			c.setCreatedBy(user);
			c.setUpdatedBy(user);
			c.setVersionedObject(c);
			c.setUri(versionLessSourceUri + "concepts/" + c.getMnemonic() + FW_SLASH + c.getVersion() + FW_SLASH);
			c.setExtras(EMPTY_JSON);
		});

		// save source
		sourceRepository.saveAndFlush(source);
		log.info("saved source - " + source.getMnemonic());

		// save concepts
		List<Integer> conceptIds = new CopyOnWriteArrayList<>();
		// concept, concept names, concept descriptions, localized texts
		List<List<Concept>> conceptBatches = ListUtils.partition(concepts, 1000);
		int i = 1;
		for (List<Concept> cb: conceptBatches) {
			log.info("Saving " + cb.size() + " concepts, batch " + i + " of " + conceptBatches.size());
			batchConcepts(cb, conceptIds);
			i++;
		}

		// update concept version = concept id
		List<List<Integer>> conceptIdBatches = ListUtils.partition(conceptIds, 1000);
		conceptIdBatches.forEach(this::batchUpdateConceptVersion);

		// save concepts sources
		conceptIdBatches.forEach(b -> batchUpdateConceptSources(b, source.getId().intValue()));
		log.info("saved " + conceptIds.size() + " concepts");
	}

	private List<Concept> toConcepts(List<CodeSystem.ConceptDefinitionComponent> components, String defaultLocale) {
		List<Concept> concepts = new ArrayList<>();
		for (CodeSystem.ConceptDefinitionComponent component : components) {
			Concept concept = toConcept(component, defaultLocale);
			if (concept != null) concepts.add(concept);
		}
		return concepts;
	}

	private Concept toConcept(CodeSystem.ConceptDefinitionComponent component, String defaultLocale) {
		Concept concept = new Concept();
		String code = component.getCode();
		if (isValid(code)) {
			// code
			concept.setMnemonic(code);
			// name
			concept.setName(code);
			// definition
			addDefinition(concept, component.getDefinition(), defaultLocale);
			// designation
			List<CodeSystem.ConceptDefinitionDesignationComponent> designationComponents = component.getDesignation();
			List<LocalizedText> names = toLocalizedText(designationComponents, defaultLocale);
			if (!names.isEmpty()) {
				addDesignation(concept, names);
			}
			// property
			List<ConceptPropertyComponent> properties = component.getProperty();
			// -- concept class
			concept.setConceptClass(getStringProperty(properties, OclFhirConstants.CONCEPT_CLASS));
			// -- data type
			concept.setDatatype(getStringProperty(properties, OclFhirConstants.DATATYPE));
			// -- inactive
			concept.setIsActive(!getBooleanProperty(properties, INACTIVE));
			return concept;
		}
		return null;
	}

	private void addDefinition(Concept concept, String definition, String defaultLocale) {
		if (isValid(definition)) {
			LocalizedText text = new LocalizedText();
			text.setName(definition);
			text.setType(DEFINITION);
			text.setLocale(defaultLocale);
			text.setLocalePreferred(True);

			ConceptsDescription description = new ConceptsDescription();
			description.setConcept(concept);
			description.setLocalizedText(text);
			concept.getConceptsDescriptions().add(description);
		}
	}

	private void addDesignation(Concept concept, List<LocalizedText> names) {
		List<ConceptsName> conceptsNames = names.stream().map(name -> {
			ConceptsName conceptsName = new ConceptsName();
			conceptsName.setConcept(concept);
			conceptsName.setLocalizedText(name);
			return conceptsName;
		}).collect(Collectors.toList());
		concept.setConceptsNames(conceptsNames);
	}

	private List<LocalizedText> toLocalizedText(List<CodeSystem.ConceptDefinitionDesignationComponent> components, String defaultLocale) {
		List<LocalizedText> texts = new ArrayList<>();
		for (CodeSystem.ConceptDefinitionDesignationComponent component : components) {
			String locale = component.getLanguage();
			String type = component.getUse().getCode();
			String name = component.getValue();
			if (locale != null && name != null) {
				LocalizedText text = new LocalizedText();
				text.setLocale(locale);
				if (type != null)
					text.setType(type);
				text.setName(name);
				text.setLocalePreferred(locale.equals(defaultLocale));
				texts.add(text);
			}
		}
		return texts;
	}

	private Source toBaseSource(final CodeSystem codeSystem, final UserProfile user, final String uri) {
		Source source = new Source();
		// mnemonic
		source.setMnemonic(codeSystem.getId());
		// canonical url
		source.setCanonicalUrl(codeSystem.getUrl());
		// created by
		source.setCreatedBy(user);
		// updated by
		source.setUpdatedBy(user);

		// draft or unknown or empty
		source.setIsActive(True);
		source.setIsLatestVersion(True);
		source.setRetired(False);
		source.setReleased(False);
		if (codeSystem.getStatus() != null) {
			// active
			if (PublicationStatus.ACTIVE.toCode().equals(codeSystem.getStatus().toCode())) {
				source.setReleased(True);
				// retired
			} else if (PublicationStatus.RETIRED.toCode().equals(codeSystem.getStatus().toCode())) {
				source.setRetired(True);
				source.setReleased(False);
				source.setIsActive(False);
				source.setIsLatestVersion(False);
			}
		}
		// version
		source.setVersion(codeSystem.getVersion());
		// default locale
		source.setDefaultLocale(isValid(codeSystem.getLanguage()) ? codeSystem.getLanguage() : EN_LOCALE);
		// uri
		source.setUri(uri.replaceAll("(?i)"+ Pattern.quote(CODESYSTEM), "sources"));
		// name
		String name = isValid(codeSystem.getName()) ? codeSystem.getName() : codeSystem.getId();
		source.setName(name);
		// content type
		if (codeSystem.getContent() != null)
			source.setContentType(codeSystem.getContent().toCode());
		// copyright
		if (isValid(codeSystem.getCopyright()))
			source.setCopyright(codeSystem.getCopyright());
		// description
		if (isValid(codeSystem.getDescription()))
			source.setDescription(codeSystem.getDescription());
		// title
		if (isValid(codeSystem.getTitle()))
			source.setFullName(codeSystem.getTitle());
		// publisher
		if (isValid(codeSystem.getPublisher()))
			source.setPublisher(codeSystem.getPublisher());
		// purpose
		if (isValid(codeSystem.getPurpose()))
			source.setPurpose(codeSystem.getPurpose());
		// revision date
		if (codeSystem.getDate() != null)
			source.setRevisionDate(codeSystem.getDate());
		// extras
		source.setExtras(EMPTY_JSON);
		return source;
	}

	private void addParent(final Source source, final BaseOclEntity owner) {
		if (owner instanceof Organization) {
			Organization organization = (Organization) owner;
			source.setOrganization(organization);
			source.setPublicAccess(organization.getPublicAccess());
		} else if (owner instanceof UserProfile){
			source.setUserId((UserProfile) owner);
		}
	}

	public void retireCodeSystem(String id, String version, StringType owner, String authToken) {
		// id, version and owner are all required
		if (!isValid(id))
			throw new InvalidRequestException("The id can not be empty. Please provide id.");
		if (!isValid(version))
			throw new InvalidRequestException("The version can not be empty. Please provide version.");
		if (!isValid(owner))
			throw new InvalidRequestException("The owner can not be empty.");

		// validate ownerType and owner
		if (!isValid(owner))
			throw new InvalidRequestException("Invalid owner.");
		String ownerType = getOwnerType(owner.getValue());
		String value = getOwner(owner.getValue());
		if (ORG.equals(ownerType)) {
			validateOrg(value);
		} else if (USER.equals(ownerType)){
			validateUser(value);
		} else {
			throw new InvalidRequestException("Invalid owner type.");
		}

		// validate token and authenticate
		AuthtokenToken token = validateToken(authToken);
		authenticate(token, USER.equals(ownerType) ? value : null,
				ORG.equals(ownerType) ? value : null);

		// find source based on ownerType and owner
		Source source;
		if (ORG.equals(ownerType)) {
			source = sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonic(id, version, value);
		} else {
			source = sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsername(id, version, value);
		}

		if (source == null)
			throw new ResourceNotFoundException(String.format("The CodeSystem of id %s and version %s does not exist.", id, version));

		// retire source and update
		source.setRetired(True);
		sourceRepository.saveAndFlush(source);
	}

}

