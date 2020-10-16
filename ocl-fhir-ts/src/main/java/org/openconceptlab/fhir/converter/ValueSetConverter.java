package org.openconceptlab.fhir.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.PURPOSE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The ValueSerConverter.
 * @author harpatel1
 */
@Component
public class ValueSetConverter {

    JsonParser parser = new JsonParser();

    OclFhirUtil oclFhirUtil;

    @Autowired
    public ValueSetConverter(OclFhirUtil oclFhirUtil) {
        this.oclFhirUtil = oclFhirUtil;
    }

    @Value("${ocl.servlet.baseurl}")
    private String baseUrl;

    public List<ValueSet> convertToValueSet(List<Collection> collections, String version) {
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
        if(isValid(collection.getExternalId()))
            valueSet.setUrl(collection.getExternalId());
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

    private void addCompose(ValueSet valueSet, Collection collection, boolean includeConceptDesignation) {
        List<CollectionsConcept> collectionsConcepts = collection.getCollectionsConcepts();
        collectionsConcepts.forEach(cc -> {
            Concept concept = cc.getConcept();
            Source parent = concept.getParent();
            // compose.include
            if (isValid(parent.getUri())) {
                String parentUri = getSystemUrl(parent.getUri());
                Optional<ValueSet.ConceptSetComponent> includeComponent = valueSet.getCompose().getInclude().parallelStream()
                        .filter(i -> parentUri.equals(i.getSystem()) &&
                                parent.getVersion().equals(i.getVersion())).findAny();
                if (includeComponent.isPresent()) {
                    ValueSet.ConceptSetComponent include = includeComponent.get();
                    // compose.include.concept
                    addConceptReference(include, concept.getMnemonic(), concept.getName(), concept.getConceptsNames(),
                            parent.getDefaultLocale(), includeConceptDesignation);
                } else {
                    ValueSet.ConceptSetComponent include = new ValueSet.ConceptSetComponent();
                    include.setSystem(getSystemUrl(parent.getUri()));
                    include.setVersion(parent.getVersion());
                    // compose.include.concept
                    addConceptReference(include, concept.getMnemonic(), concept.getName(), concept.getConceptsNames(),
                            parent.getDefaultLocale(), includeConceptDesignation);
                    valueSet.getCompose().addInclude(include);
                }
                // compose.inactive
                if (!valueSet.getCompose().getInactive() && !concept.getIsActive()) {
                    valueSet.getCompose().setInactive(true);
                }
            }
        });
    }

    private void addConceptReference(ValueSet.ConceptSetComponent includeComponent, String code, String display,
                                     List<ConceptsName> names, String dictDefaultLocale, boolean includeConceptDesignation) {
        ValueSet.ConceptReferenceComponent referenceComponent = new ValueSet.ConceptReferenceComponent();
        // code
        referenceComponent.setCode(code);
        // display
        List<LocalizedText> lts = names.stream().filter(c -> c.getLocalizedText() != null).map(d -> d.getLocalizedText())
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

    private String getSystemUrl(final String parentUri) {
        String url = baseUrl.split("fhir")[0];
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
