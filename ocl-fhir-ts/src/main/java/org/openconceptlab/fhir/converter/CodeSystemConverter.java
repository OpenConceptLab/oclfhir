package org.openconceptlab.fhir.converter;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.codesystems.PublicationStatus;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirConstants;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

/**
 * The CodeSystemConverter.
 * @author harpatel1
 */
@Component
public class CodeSystemConverter extends BaseConverter {

	private static final Log log = LogFactory.getLog(CodeSystemConverter.class);
	private static final String insertConceptsSources = "insert into concepts_sources (concept_id,source_id) values (?,?) on conflict do nothing";
	private static final String insertConceptNamesSql = "insert into concepts_names (localizedtext_id,concept_id) values (?,?) on conflict do nothing";
	private static final String insertConceptDescSql = "insert into concepts_descriptions (localizedtext_id,concept_id) values (?,?) on conflict do nothing";
	private static final String updateConceptSql = "update concepts set " +
			" version = ?, " +
			" versioned_object_id = ?, " +
			" uri = (select c2.uri || c2.id || '/' from concepts c2 where c2.id = ?) " +
			" where id = ?";

	private static final String updateConceptBaseSql = "update concepts set " +
			" version = ?, " +
			" versioned_object_id = ? " +
			" where id = ?";

	private static final String conceptCountsql = "select count(*) from (select max(cs.concept_id) as concept_id , c1.mnemonic from concepts_sources cs \n" +
			"inner join concepts c1 on c1.id = cs.concept_id \n" +
			"where cs.source_id = ? \n" +
			"group by c1.mnemonic) as val";

	public CodeSystemConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
							   UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
							   AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
							   OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
							   MappingRepository mappingRepository) {
		super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
				userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, mappingRepository);
	}

	public Bundle convertToCodeSystem(List<Source> sources, boolean includeConcepts, StringType page,
									  String url, OclFhirUtil.Filter filter) {
		StringBuilder hasNext = new StringBuilder();
		int pageInt = getPage(page);
		// convert source into codesystem
		List<CodeSystem> codeSystems = new ArrayList<>();
		for (Source source : sources) {
			// convert to base
			CodeSystem codeSystem = toBaseCodeSystem(source);
			if (includeConcepts) {
				// add concepts
				addConceptsToCodeSystem(codeSystem, source, pageInt, hasNext);
			}
			codeSystems.add(codeSystem);
		}
		// apply codesystem filter
		List<CodeSystem> codeSystemsFiltered = applyFilter(codeSystems, filter);
		int total = codeSystemsFiltered.size();
		// paginate filtered list
		if (!includeConcepts) {
			int offset = pageInt * 10;
			int count = 10;
			if (pageInt == 0) {
				if (codeSystemsFiltered.size() > count) hasNext.append(True);
			} else if (pageInt < codeSystemsFiltered.size()/count) {
				hasNext.append(True);
			}
			codeSystemsFiltered = paginate(codeSystemsFiltered, offset, count);
		}
		// bundle resources
		Bundle bundle = OclFhirUtil.getBundle(codeSystemsFiltered, url, getPrevPage(page), getNextPage(page, hasNext));
		bundle.setTotal(total);
		return bundle;
	}

	/**
	 * Converts {@link Source} into {@link CodeSystem}. Used for retrieving CodeSystems.
	 * @param source
	 * @return CodeSystem
	 */
	private CodeSystem toBaseCodeSystem(final Source source){
        CodeSystem codeSystem = new CodeSystem();
        // Url
        if(StringUtils.isNotBlank(source.getCanonicalUrl())) {
        	codeSystem.setUrl(source.getCanonicalUrl());
        }
        // id
        codeSystem.setId(source.getMnemonic());
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
		codeSystem.setCount(getConceptCount(source.getId()));
        // property
		addProperty(codeSystem);
		// publisher
		if (isValid(source.getPublisher()))
			codeSystem.setPublisher(source.getPublisher());
		// identifier, contact, jurisdiction
		addJsonFields(codeSystem, isValid(source.getIdentifier()) && !EMPTY_JSON.equals(source.getIdentifier()) ? source.getIdentifier() : EMPTY,
				source.getContact(), source.getJurisdiction(), source.getText(), source.getMeta());
		// add lastUpdated date is not populated in codeSystem.meta.lastUpdated
		if (codeSystem.getMeta().getLastUpdated() == null) {
			codeSystem.getMeta().setLastUpdated(source.getUpdatedAt());
		}
		// add accession identifier if not present
		Optional<Identifier> identifierOpt = codeSystem.getIdentifier().stream()
				.filter(i -> i.getType().hasCoding(ACSN_SYSTEM, ACSN)).findAny();
		if (codeSystem.getIdentifier().isEmpty() || identifierOpt.isEmpty()) {
			getIdentifier(source.getUri())
					.ifPresent(i -> codeSystem.getIdentifier().add(i));
		}
		// purpose
		if (isValid(source.getPurpose()))
			codeSystem.setPurpose(source.getPurpose());
		// copyright
		if (isValid(source.getCopyright()))
			codeSystem.setCopyright(source.getCopyright());
		// The ? value is not parsable and invalid.
		if (isValid(source.getContentType())) {
			Optional<String> content = Stream.of(CodeSystem.CodeSystemContentMode.values()).map(Enum::toString)
					.filter(m -> source.getContentType().equalsIgnoreCase(m))
					.filter(m -> !m.equals(CodeSystem.CodeSystemContentMode.NULL.toCode()))
					.findAny();
			content.ifPresent(s -> codeSystem.setContent(CodeSystem.CodeSystemContentMode.fromCode(s.toLowerCase())));
		}
		// revision date
		if (source.getRevisionDate() != null)
			codeSystem.setDate(source.getRevisionDate());
		// experimental
		if (source.isExperimental() != null) codeSystem.setExperimental(source.isExperimental());
		// case_sensitive
		if (source.isCaseSensitive() != null) codeSystem.setCaseSensitive(source.isCaseSensitive());
		// collection_reference
		if (isValid(source.getCollectionReference())) codeSystem.setValueSet(source.getCollectionReference());
		// hierarchy_meaning
		if (isValid(source.getHierarchyMeaning())) {
			Optional<String> hierarchyMeaning = Stream.of(CodeSystem.CodeSystemHierarchyMeaning.values()).map(Enum::toString)
					.filter(m -> source.getHierarchyMeaning().equalsIgnoreCase(m))
					.filter(m -> !m.equals(CodeSystem.CodeSystemContentMode.NULL.toCode()))
					.findAny();
			hierarchyMeaning.ifPresent(hm -> codeSystem.setHierarchyMeaning(CodeSystem.CodeSystemHierarchyMeaning.fromCode(hm.toLowerCase())));
		}
		// compositional
		if (source.isCompositional() != null) codeSystem.setCompositional(source.isCompositional());
		// version_needed
		if (source.isVersionNeeded() != null) codeSystem.setVersionNeeded(source.isVersionNeeded());
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

	private void addConceptsToCodeSystem(final CodeSystem codeSystem, final Source source, int page, StringBuilder hasNext) {
		Page<Concept> concepts = conceptRepository.findConcepts(source.getId(), PageRequest.of(page, 100));
		if (page < concepts.getTotalPages() - 1) hasNext.append(True);
		for (Concept concept : concepts.getContent()) {
			CodeSystem.ConceptDefinitionComponent definitionComponent = new CodeSystem.ConceptDefinitionComponent();
			// code
			definitionComponent.setCode(concept.getMnemonic());
			// display
			if (isValid(concept.getName())) {
				definitionComponent.setDisplay(concept.getName());
			} else {
				List<LocalizedText> names = concept.getConceptsNames().stream()
						.filter(c -> c.getLocalizedText() != null)
						.map(ConceptsName::getLocalizedText)
						.collect(Collectors.toList());
				Optional<String> display = oclFhirUtil.getDisplayForLanguage(names, source.getDefaultLocale());
				definitionComponent.setDisplay(display.orElse(EMPTY));
			}
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
			if (!NA.equals(concept.getConceptClass()) || !NA.equals(concept.getDatatype()) || concept.getRetired()) {
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(OclFhirConstants.CONCEPTCLASS),
						new StringType(concept.getConceptClass())));
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(OclFhirConstants.DATATYPE),
						new StringType(concept.getDatatype())));
				definitionComponent.getProperty().add(new ConceptPropertyComponent(new CodeType(INACTIVE),
						new BooleanType(concept.getRetired())));
			}
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
		OclEntity oclEntity = new OclEntity(codeSystem, accessionId, authToken, true);
		UserProfile user = oclEntity.getUserProfile();
		// base source
		Source source = toBaseSource(codeSystem, user, oclEntity.getAccessionId());
		// add parent and access
		addParent(source, oclEntity.getOwner());
		// add identifier, contact and jurisdiction
		removeVersionFromIdentifier(codeSystem.getIdentifier());
		addJsonStrings(codeSystem, source);
		// add concepts
		List<Concept> concepts = toConcepts(codeSystem.getConcept(), isValid(codeSystem.getLanguage()) ? codeSystem.getLanguage() : EN_LOCALE);
		populateBaseConceptField(concepts, source, user);

		// save given version
		saveSource(source, concepts);

		boolean isHeadExists = checkHeadVersionId(oclEntity.getUsername(), oclEntity.getOrg(), oclEntity.getResourceId(), oclEntity.getResourceType()) ||
				checkHeadVersionUrl(oclEntity.getUsername(), oclEntity.getOrg(), oclEntity.getUrl(), oclEntity.getResourceType());
		if (!isHeadExists) {
			// save HEAD version
			log.info("Creating HEAD version - " + source.getMnemonic());
			source.setId(null);
			source.setVersion(HEAD);
			source.setIsLatestVersion(false);
			source.setReleased(false);
			source.setUri(removeVersion(source.getUri()));
			saveSource(source, concepts);
		}
		// populate index
		oclFhirUtil.populateIndex(getToken(), SOURCES, CONCEPTS);
	}

	@Transactional
	protected void saveSource(Source source, List<Concept> concepts) {
		// save source
		sourceRepository.saveAndFlush(source);
		log.info("saved source - " + source.getMnemonic());

		// save concepts
		saveConcepts(source, concepts);
	}

	private void saveConcepts(Source source, List<Concept> concepts) {
		List<Integer> conceptIds = new CopyOnWriteArrayList<>();
		// save concept
		persistConcepts(concepts, conceptIds);
		// save concepts sources
		saveConceptsSources(source.getId(), conceptIds);
	}

	private void persistConcepts(List<Concept> concepts, List<Integer> conceptIds) {
		// concept, concept names, concept descriptions, localized texts
		List<List<Concept>> conceptBatches = ListUtils.partition(concepts, 1000);
		int i = 1;
		for (List<Concept> cb: conceptBatches) {
			log.info("Saving " + cb.size() + " concepts, batch " + i + " of " + conceptBatches.size());
			batchConcepts(cb, conceptIds);
			i++;
		}
	}

	private void saveConceptsSources(Long sourceId, List<Integer> conceptIds) {
		// save concepts sources
		List<List<Integer>> conceptIdBatches = ListUtils.partition(conceptIds, 1000);
		conceptIdBatches.forEach(b -> batchUpdateConceptSources(b, sourceId));
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
			concept.setName(isValid(component.getDisplay()) ? component.getDisplay() : code);
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
			concept.setRetired(getBooleanProperty(properties, INACTIVE));
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

	@Deprecated
	private void retireCodeSystem(String id, String version, StringType owner, String authToken) {
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

	public void updateCodeSystem(final CodeSystem codeSystem, final Source source, final String accessionId, final String authToken) {
		// update and save in database
		boolean newConcepts = update(codeSystem, source, accessionId, authToken);
		// update index
		oclFhirUtil.updateIndex(getToken(), SOURCES, source.getMnemonic());
		if (newConcepts) {
			oclFhirUtil.populateIndex(getToken(), CONCEPTS);
		}
	}

	@Transactional
	 boolean update(final CodeSystem codeSystem, final Source source, final String accessionId, final String authToken) {
		final OclEntity oclEntity = new OclEntity(codeSystem, accessionId, authToken, false);
		// update status
		if (codeSystem.getStatus() != null) {
			if (PublicationStatus.DRAFT.toCode().equals(codeSystem.getStatus().toCode()) || PublicationStatus.UNKNOWN.toCode().equals(codeSystem.getStatus().toCode())) {
				source.setReleased(False);
			} else if (PublicationStatus.ACTIVE.toCode().equals(codeSystem.getStatus().toCode())) {
				source.setReleased(True);
			} else if (PublicationStatus.RETIRED.toCode().equals(codeSystem.getStatus().toCode())) {
				source.setRetired(True);
				source.setReleased(False);
			}
		}
		// update canonical url
		if (isValid(codeSystem.getUrl()))
			source.setCanonicalUrl(codeSystem.getUrl());
		// update language
		if (isValid(codeSystem.getLanguage()))
			source.setDefaultLocale(codeSystem.getLanguage());
		// update name
		if (isValid(codeSystem.getName()))
			source.setName(codeSystem.getName());
		// content type
		if (codeSystem.getContent() != null &&
				isValid(codeSystem.getContent().toCode()) &&
				!codeSystem.getContent().toCode().equals(CodeSystem.CodeSystemContentMode.NULL.toCode()))
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
		// updated by
		source.setUpdatedBy(oclEntity.getUserProfile());
		// updated at
		source.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		// we won't allow to update accession identifier, so let's remove it if its present
		removeAccessionIdentifier(codeSystem.getIdentifier());
		// update identifier, contact and jurisdiction
		addJsonStrings(codeSystem, source);
		// case_sensitive
		if (codeSystem.getCaseSensitiveElement().getValue() != null)
			source.setCaseSensitive(codeSystem.getCaseSensitiveElement().booleanValue());
		// collection_reference
		if (isValid(codeSystem.getValueSet())) source.setCollectionReference(codeSystem.getValueSet());
		// hierarchy_meaning
		if (codeSystem.getHierarchyMeaning() != null) source.setHierarchyMeaning(codeSystem.getHierarchyMeaning().toCode());
		// compositional
		if (codeSystem.getCompositionalElement().getValue() != null)
			source.setCompositional(codeSystem.getCompositionalElement().booleanValue());
		// version_needed
		if (codeSystem.getVersionNeededElement().getValue() != null)
			source.setVersionNeeded(codeSystem.getVersionNeededElement().booleanValue());
		// experimental
		if (codeSystem.getExperimentalElement().getValue() != null)
			source.setExperimental(codeSystem.getExperimentalElement().booleanValue());
		// source_type
		String sourceType = getSourceType(codeSystem);
		if (isValid(sourceType))
			source.setSourceType(sourceType);
		// update base source resource
		sourceRepository.saveAndFlush(source);
		log.info("updated codesystem - " + source.getMnemonic());

		// We create new concepts if provided
		List<Concept> concepts = toConcepts(codeSystem.getConcept(), codeSystem.getLanguage());
		List<List<Concept>> conceptBatches = ListUtils.partition(concepts, 25000);
		for (List<Concept> batch : conceptBatches) {
			List<String> existingConcepts = getExistingConcepts(batch.stream().map(Concept::getMnemonic)
					.distinct()
					.collect(Collectors.toList()), source.getId());
			List<Concept> newConcepts = batch.stream().filter(f -> !existingConcepts.contains(f.getMnemonic())).collect(Collectors.toList());
			if (!newConcepts.isEmpty()) {
				populateBaseConceptField(newConcepts, source, oclEntity.getUserProfile());
				saveConcepts(source, newConcepts);
				return true;
			}
		}
		return false;
	}

	private void populateBaseConceptField(List<Concept> concepts, Source source, UserProfile user) {
		concepts.forEach(c -> {
			c.setParent(source);
			c.setPublicAccess(source.getPublicAccess());
			// will be replaced with generated id, DO NOT CHANGE THE VALUE
			c.setVersion("1");
			c.setIsLatestVersion(True);
			c.setIsActive(True);
			c.setReleased(!c.getRetired());
			c.setDefaultLocale(source.getDefaultLocale());
			c.setCreatedBy(user);
			c.setUpdatedBy(user);
			c.setVersionedObject(c);
			c.setUri(getVersionLessSourceUri(source) + CONCEPTS + FS + c.getMnemonic() + FS);
			c.setExtras(EMPTY_JSON);
		});
	}

	private List<String> getExistingConcepts(List<String> concepts, Long sourceId) {
		MapSqlParameterSource s = new MapSqlParameterSource();
		s.addValue("sourceId", sourceId);
		s.addValue("mnemonic", concepts);
		return namedParameterJdbcTemplate.query("select c.mnemonic from concepts_sources cs inner join concepts c on cs.concept_id = c.id " +
				" where cs.source_id = :sourceId and c.mnemonic in (mnemonic)", s, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});
	}

	private void batchUpdateConceptSources(List<Integer> conceptIds, Long sourceId) {
		this.jdbcTemplate.batchUpdate(insertConceptsSources, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				ps.setInt(1, conceptIds.get(i));
				ps.setLong(2, sourceId);
			}
			public int getBatchSize() {
				return conceptIds.size();
			}
		});
	}

	private void batchInsertConceptNames(String sql, List<Long> nameIds, Integer conceptId) {
		this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				ps.setInt(1, nameIds.get(i).intValue());
				ps.setInt(2, conceptId);
			}
			public int getBatchSize() {
				return nameIds.size();
			}
		});
	}

	private void batchConcepts(List<Concept> concepts, List<Integer> conceptIds) {
		concepts.forEach(c -> {
			boolean isLatestVersion = c.getIsLatestVersion();
			boolean released = c.getReleased();
			// base concept version
			c.setIsLatestVersion(false);
			c.setReleased(false);
			Integer id1 = saveConcept(c);
			this.jdbcTemplate.update(updateConceptBaseSql, new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setInt(1, id1);
					ps.setInt(2, id1);
					ps.setInt(3, id1);
				}
			});
  			// first concept version (Ideally we should only be creating one concept record, but we need to create two for OCL API compatibility)
			c.setIsLatestVersion(isLatestVersion);
			c.setReleased(released);
			Integer id2 = saveConcept(c);
			this.jdbcTemplate.update(updateConceptSql, new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setInt(1, id2);
					ps.setInt(2, id1);
					ps.setInt(3, id2);
					ps.setInt(4, id2);
				}
			});
			conceptIds.add(id1);
			conceptIds.add(id2);
		});
	}

	private Integer saveConcept(Concept c) {
		Integer conceptId = insert(insertConcept, toMap(c)).intValue();
		if (!c.getConceptsNames().isEmpty()) {
			List<Long> nameIds = insertRows(
					c.getConceptsNames().stream().filter(Objects::nonNull).filter(f -> f.getLocalizedText() != null).map(ConceptsName::getLocalizedText).collect(Collectors.toList())
			);
			batchInsertConceptNames(insertConceptNamesSql, nameIds, conceptId);
		}
		if (!c.getConceptsDescriptions().isEmpty()) {
			List<Long> descIds = insertRows(
					c.getConceptsDescriptions().stream().filter(Objects::nonNull).filter(f -> f.getLocalizedText() != null).map(ConceptsDescription::getLocalizedText).collect(Collectors.toList())
			);
			batchInsertConceptNames(insertConceptDescSql, descIds, conceptId);
		}
		return conceptId;
	}

	private Map<String, Object> toMap(LocalizedText text) {
		Map<String, Object> map = new HashMap<>();
		map.put(NAME, text.getName());
		map.put(TYPE, text.getType());
		map.put(LOCALE, text.getLocale());
		map.put(LOCALE_PREFERRED, text.getLocalePreferred());
		map.put(CREATED_AT, text.getCreatedAt());
		return map;
	}

	private Map<String, Object> toMap(Concept obj) {
		Map<String, Object> map = new HashMap<>();
		map.put(PUBLIC_ACCESS, obj.getPublicAccess());
		map.put(IS_ACTIVE, obj.getIsActive());
		map.put(EXTRAS, obj.getExtras());
		map.put(URI, obj.getUri());
		map.put(MNEMONIC, obj.getMnemonic());
		map.put(VERSION, obj.getVersion());
		map.put(RELEASED, obj.getReleased());
		map.put(RETIRED, obj.getRetired());
		map.put(IS_LATEST_VERSION, obj.getIsLatestVersion());
		map.put(NAME, obj.getName());
		map.put(FULL_NAME, obj.getFullName());
		map.put(DEFAULT_LOCALE, obj.getDefaultLocale());
		map.put(CONCEPT_CLASS, obj.getConceptClass());
		map.put(DATATYPE, obj.getDatatype());
		map.put(COMMENT, obj.getComment());
		map.put(CREATED_BY_ID, obj.getCreatedBy().getId());
		map.put(UPDATED_BY_ID, obj.getUpdatedBy().getId());
		map.put(PARENT_ID, obj.getParent().getId());
		map.put(CREATED_AT, obj.getParent().getCreatedAt());
		map.put(UPDATED_AT, obj.getParent().getUpdatedAt());
		return map;
	}

	private List<Long> insertRows(List<LocalizedText> texts) {
		List<Long> keys = new ArrayList<>();
		texts.forEach(t -> {
			keys.add(insert(insertLocalizedText, toMap(t)));
		});
		return keys;
	}

	private List<CodeSystem> applyFilter(List<CodeSystem> codeSystems, Filter filter) {
		log.info("CodeSystem filter - " + filter);
		return codeSystems.stream()
				.filter(c ->
						!isValid(filter.getStatus()) || (c.getStatus() != null && c.getStatus().toCode().equalsIgnoreCase(filter.getStatus())))
				.filter(c ->
						!isValid(filter.getContentMode()) || (c.getContent() != null && c.getContent().toCode().equalsIgnoreCase(filter.getContentMode())))
				.filter(c ->
						!isValid(filter.getPublisher()) || (isValid(c.getPublisher()) && c.getPublisher().equals(filter.getPublisher())))
				.filter(c ->
						!isValid(filter.getVersion()) || isVersionAll(filter.getVersion()) || (isValid(c.getVersion()) && c.getVersion().equals(filter.getVersion())))
				.collect(Collectors.toList());
	}

	private int getConceptCount(Long sourceId) {
		return this.jdbcTemplate.queryForObject(conceptCountsql, new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
				return resultSet.getInt(1);
			}
		}, sourceId);
	}

}

