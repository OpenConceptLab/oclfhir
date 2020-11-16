package org.openconceptlab.fhir.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.ConceptsSourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.PURPOSE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.getOwner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The ValueSerConverter.
 * @author harpatel1
 */
@Component
public class ValueSetConverter {

    JsonParser parser = new JsonParser();

    OclFhirUtil oclFhirUtil;
    ConceptsSourceRepository conceptsSourceRepository;
    ConceptRepository conceptRepository;

    @Autowired
    public ValueSetConverter(OclFhirUtil oclFhirUtil, ConceptsSourceRepository conceptsSourceRepository, ConceptRepository conceptRepository) {
        this.oclFhirUtil = oclFhirUtil;
        this.conceptsSourceRepository = conceptsSourceRepository;
        this.conceptRepository = conceptRepository;
    }

    @Value("${ocl.servlet.baseurl}")
    private String baseUrl;

    public List<ValueSet> convertToValueSet(List<Collection> collections) {
        List<ValueSet> valueSets = new ArrayList<>();
        collections.forEach(collection -> {
            ValueSet valueSet = toBaseValueSet(collection);
            addCompose(valueSet, collection, false);
            valueSets.add(valueSet);
        });
        return valueSets;
    }

    private ValueSet toBaseValueSet(final Collection collection) {
        ValueSet valueSet = new ValueSet();
        // set id
        valueSet.setId(collection.getMnemonic());
        // set identifier
        getIdentifier(collection.getUri())
                .ifPresent(i -> valueSet.getIdentifier().add(i));
        // set Url
        if(isValid(collection.getCanonicalUrl()))
            valueSet.setUrl(collection.getCanonicalUrl());
        // set version
        valueSet.setVersion(collection.getVersion());
        // set name
        valueSet.setName(collection.getName());
        // set title
        if(isValid(collection.getFullName()))
            valueSet.setTitle(collection.getFullName());
        // set description
        if(isValid(collection.getDescription()))
            valueSet.setDescription(collection.getDescription());
        // set status
        addStatus(valueSet, collection.getIsActive(), collection.getRetired() != null ? collection.getRetired() : false,
                collection.getReleased());
        return valueSet;
    }

    private Optional<Concept> getConcept(List<ConceptsSource> conceptsSources, String conceptId, String conceptVersion) {
        if (isValid(conceptVersion)) {
            return conceptsSources.parallelStream().map(ConceptsSource::getConcept)
                    .filter(c -> c.getMnemonic().equals(conceptId) && conceptVersion.equals(c.getVersion()))
                    .findAny();
        } else {
            return conceptsSources.parallelStream().map(ConceptsSource::getConcept)
                    .filter(c -> c.getMnemonic().equals(conceptId))
                    .max(Comparator.comparing(Concept::getId));
        }
    }

    private String formatExpression(String expression) {
        String uri = expression;
        if (!uri.startsWith("/")) uri = "/" + uri;
        if (!uri.endsWith("/")) uri = uri + "/";
        return uri;
    }

    private void addCompose(ValueSet valueSet, Collection collection, boolean includeConceptDesignation) {
        List<CollectionsConcept> collectionsConcepts = collection.getCollectionsConcepts();
        // We have to use expressions to determine actual Source version since its not possible through CollectionsConcepts
        List<String> expressions = collection.getCollectionsReferences().parallelStream()
                .map(CollectionsReference::getCollectionReference)
                .map(CollectionReference::getExpression)
                .collect(Collectors.toList());

        // lets get all the source versions first to reduce the database calls
        Set<Source> sources = expressions.parallelStream().map(m -> formatExpression(m).split("/"))
                .map(m -> ownerType(m) + "|" + ownerId(m) + "|" + getSourceId(m) + "|" + getSourceVersion(m))
                .distinct()
                .map(m -> m.split("\\|"))
                .filter(m -> m.length == 4)
                .map(m -> oclFhirUtil.getSourceVersion(m[2], m[3], publicAccess, m[0], m[1]))
                .collect(Collectors.toSet());

        // for each source let's evaluate expressions for concept and concept version and populate compose
        sources.forEach(source -> {
            expressions.stream().map(m -> formatExpression(m).split("/"))
                    .filter(m -> source.getMnemonic().equals(getSourceId(m)) && source.getVersion().equals(getSourceVersion(m)))
                    .forEach(m -> {
                        String conceptId = getConceptId(m);
                        String conceptVersion = getConceptVersion(m);
                        if (isValid(conceptId)) {
                            // we can not simply get concepts list from the source considering huge number of concepts, this is alternate
                            // way to get only concepts that we care about and not retrieve whole list. This has improved performance and consumes less memory
                            Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, conceptId, conceptVersion);
                            conceptOpt.ifPresent(c -> populateCompose(valueSet, includeConceptDesignation, c, source.getCanonicalUrl()
                                    , source.getVersion(), source.getDefaultLocale()));
                        }
                    });
        });
    }

    private void populateCompose(ValueSet valueSet, boolean includeConceptDesignation, Concept concept, String sourceCanonicalUrl,
                                 String sourceVersion, String sourceDefaultLocale) {
        // compose.include
        if (isValid(sourceCanonicalUrl)) {
            Optional<ValueSet.ConceptSetComponent> includeComponent = valueSet.getCompose().getInclude().parallelStream()
                    .filter(i -> sourceCanonicalUrl.equals(i.getSystem()) &&
                            sourceVersion.equals(i.getVersion())).findAny();
            if (includeComponent.isPresent()) {
                ValueSet.ConceptSetComponent include = includeComponent.get();
                // compose.include.concept
                addConceptReference(include, concept.getMnemonic(), concept.getName(), concept.getConceptsNames(),
                        sourceDefaultLocale, includeConceptDesignation);
            } else {
                ValueSet.ConceptSetComponent include = new ValueSet.ConceptSetComponent();
                include.setSystem(sourceCanonicalUrl);
                include.setVersion(sourceVersion);
                // compose.include.concept
                addConceptReference(include, concept.getMnemonic(), concept.getName(), concept.getConceptsNames(),
                        sourceDefaultLocale, includeConceptDesignation);
                valueSet.getCompose().addInclude(include);
            }
            // compose.inactive
            if (!valueSet.getCompose().getInactive() && concept.getRetired()) {
                valueSet.getCompose().setInactive(true);
            }
        }
    }

    private void addConceptReference(ValueSet.ConceptSetComponent includeComponent, String code, String display,
                                     List<ConceptsName> names, String dictDefaultLocale, boolean includeConceptDesignation) {
        ValueSet.ConceptReferenceComponent referenceComponent = new ValueSet.ConceptReferenceComponent();
        // code
        referenceComponent.setCode(code);
        // display
        List<LocalizedText> lts = names.stream().filter(c -> c.getLocalizedText() != null).map(ConceptsName::getLocalizedText)
                .collect(Collectors.toList());
        referenceComponent.setDisplay(oclFhirUtil.getDefinition(lts, dictDefaultLocale));
        // designation
        if (includeConceptDesignation)
            addConceptReferenceDesignation(names, referenceComponent);
        includeComponent.getConcept().add(referenceComponent);
    }

    private void addConceptReferenceDesignation(List<ConceptsName> names, ValueSet.ConceptReferenceComponent referenceComponent) {
        names.parallelStream().forEach(n -> {
            LocalizedText lt = n.getLocalizedText();
            ValueSet.ConceptReferenceDesignationComponent designationComponent = new ValueSet.ConceptReferenceDesignationComponent();
            if(lt != null) {
                designationComponent.setLanguage(lt.getLocale());
                if (isValid(lt.getType()))
                    designationComponent.getUse().setCode(lt.getType());
                designationComponent.setValue(lt.getName());
                referenceComponent.addDesignation(designationComponent);
            }
        });
    }

    public Parameters validateCode(Collection collection, UriType system, StringType systemVersion, String code, StringType display,
                                   CodeType displayLanguage, StringType owner, List<String> access) {
        Parameters parameters = new Parameters();
        BooleanType result = new BooleanType(false);
        parameters.addParameter().setName(RESULT).setValue(result);
        // determine owner
        owner = !isValid(owner) ? determineOwner(collection) : owner;
        String ownerType = getOwnerType(owner.getValue());
        String ownerId = getOwner(owner.getValue());
        // determine source
        Source source = oclFhirUtil.getSourceByOwnerAndUrl(owner, newStringType(system.getValue()), systemVersion, access);
        // determine expression
        String expression = buildExpression(source.getMnemonic(), source.getVersion(), code, ownerType, ownerId);
        Optional<CollectionReference> reference = collection.getCollectionsReferences().parallelStream()
                .map(CollectionsReference::getCollectionReference)
                .filter(f -> f.getExpression().contains(expression))
                .findAny();
        if (reference.isPresent()) {
            if (isValid(display)) {
                Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, code, EMPTY);
                if (conceptOpt.isPresent()) {
                    List<LocalizedText> names = oclFhirUtil.getNames(conceptOpt.get());
                    boolean match = oclFhirUtil.validateDisplay(names, display, displayLanguage);
                    if (!match) {
                        parameters.addParameter().setName(MESSAGE).setValue(newStringType("Invalid display."));
                    } else {
                        result.setValue(true);
                    }
                }
            } else {
                result.setValue(true);
            }
        }
        return parameters;
    }

    private StringType determineOwner(Collection collection) {
        if (collection.getOrganization() != null) {
            return new StringType(ORG_ + collection.getOrganization().getMnemonic());
        } else {
            return new StringType(USER_ + collection.getUserId().getUsername());
        }
    }

    private String buildExpression(String sourceId, String sourceVersion, String conceptId, String ownerType, String ownerId) {
        return new StringBuilder()
                .append("/")
                .append(ORG.equals(ownerType) ? ORGS : USERS)
                .append("/").append(ownerId)
                .append("/sources/")
                .append(sourceId)
                .append("/")
                .append(!isValid(sourceVersion) || HEAD.equals(sourceVersion) ? EMPTY : sourceVersion+"/")
                .append("concepts/")
                .append(conceptId)
                .append("/")
                .toString();
    }

    private String getSourceId(String[] ar) {
        return ar.length >= 5 && !ar[4].isEmpty() ? ar[4] : EMPTY;
    }

    private String getSourceVersion(String[] ar) {
        return ar.length >= 6 && !ar[5].isEmpty() && ar[5].equals("concepts") ? HEAD : ar[5];
    }

    private String getConceptId(String[] ar) {
        return ar.length >= 6 && !ar[5].isEmpty() && ar[5].equals("concepts") ?
                ar.length >= 7 ? ar[6] : EMPTY :
                ar.length >= 8 ? ar[7] : EMPTY;
    }

    private String getConceptVersion(String[] ar) {
        if (ar[6].equals("concepts")) {
            if (ar.length >=9 && !ar[8].isEmpty()) return ar[8];
        } else if (ar.length >= 8 && !ar[7].trim().isEmpty()) {
            return  ar[7];
        }
        return EMPTY;
    }

    private String ownerType(String [] ar) {
        return ar[1].contains(ORG) ? ORG : USER;
    }

    private String ownerId(String [] ar) {
        return ar[2];
    }

    private Source getSource(String [] ar) {
        String ownerType = ar[1].contains(ORG) ? ORG : USER;
        String owner = ar[2];
        String sourceId = getSourceId(ar);
        String sourceVersion = getSourceVersion(ar);
        return oclFhirUtil.getSourceVersion(sourceId, sourceVersion, publicAccess, ownerType, owner);
    }

    private String getSystemUrl(String parentUri) {
        String url = baseUrl.split("fhir")[0];
        String[] source = formatExpression(parentUri).split("/");
        if (source.length >= 6 && isValid(source[5]))
            parentUri = String.join("/", source[0], source[1], source[2], source[3], source[4],
                    VERSION, source[5]);
        return url.substring(0, url.length() - 1) + parentUri.replace("sources","CodeSystem");
    }

    private void addExtras(ValueSet valueSet, String extras) {
        JsonObject obj = parseExtras(extras);
        // identifier
        JsonArray identifiers = obj.getAsJsonArray(IDENTIFIERS);
        valueSet.setIdentifier(getIdentifiers(identifiers));
        // purpose
        if(isValidElement(obj.get(PURPOSE)))
            valueSet.setPurpose(obj.get(PURPOSE).getAsString());
        // copyright
        if(isValidElement(obj.get(COPYRIGHT)))
            valueSet.setCopyright(obj.get(COPYRIGHT).getAsString());

        // compose
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        // compose.inactive
        if(isValidElement(obj.get(VS_COMPOSE_INACTIVE)))
            compose.setInactive(Boolean.parseBoolean(obj.get(VS_COMPOSE_INACTIVE).getAsString()));
        // compose.include
        JsonArray includesArray = obj.getAsJsonArray(VS_COMPOSE_INCLUDE);
        List<ValueSet.ConceptSetComponent> includes = new ArrayList<>();
        if(includesArray != null && includesArray.isJsonArray()) {
            for(JsonElement je : includesArray) {
                ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
                JsonObject include = je.getAsJsonObject();
                if(include.get(SYSTEM) != null) {
                    component.setSystem(include.get(SYSTEM).getAsString());
                    if(include.get(VERSION) != null)
                        component.setVersion(include.get(VERSION).getAsString());
                    JsonArray filters = include.getAsJsonArray(FILTERS);
                    for (JsonElement filter : filters) {
                        JsonObject filterObj = filter.getAsJsonObject();
                        if(filterObj != null) {
                            ValueSet.ConceptSetFilterComponent filterComponent = new ValueSet.ConceptSetFilterComponent();
                            JsonElement property = filterObj.get(PROPERTY);
                            JsonElement op = filterObj.get(OP);
                            JsonElement value = filterObj.get(VALUE);
                            if (property != null) {
                                if (CONCEPT_CLASS.equals(property.getAsString()) || DATATYPE.equals(property.getAsString())) {
                                    filterComponent.setProperty(property.getAsString());
                                    if (op != null && allowedFilterOperators.contains(op.getAsString()))
                                        filterComponent.setOp(ValueSet.FilterOperator.fromCode(op.getAsString()));
                                    if (value != null)
                                        filterComponent.setValue(value.getAsString());
                                }
                            }
                            component.getFilter().add(filterComponent);
                        }
                    }
                }
                compose.getInclude().add(component);
            }
        }
        valueSet.setCompose(compose);
    }
}
