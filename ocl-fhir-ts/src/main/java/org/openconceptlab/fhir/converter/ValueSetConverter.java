package org.openconceptlab.fhir.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.model.UserProfile;
import org.springframework.stereotype.Component;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.PURPOSE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The ValueSerConverter.
 * @author harpatel1
 */
@Component
public class ValueSetConverter {

    JsonParser parser = new JsonParser();

    public void convertToValueSet(List<ValueSet> valueSets, List<Collection> collections, String version) {
        // filter by version if provided
        String versionId = StringUtils.isNotBlank(version) ? version : HEAD;
        collections.forEach(collection -> {
            if(collection.getIsActive() && collection.getVersion().equalsIgnoreCase(versionId)) {
                // convert to base
                ValueSet valueSet = toBaseValueSet(collection);
                // add extras
                addExtras(valueSet, collection.getExtras());
                valueSets.add(valueSet);
            }
        });
    }

    public ValueSet toBaseValueSet(final Collection collection) {
        ValueSet valueSet = new ValueSet();
        // set id
        valueSet.setId(collection.getMnemonic());
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
        // set publisher
        Organization organization = collection.getOrganization();
        UserProfile user = collection.getUserId();
        if(organization != null) {
            valueSet.setPublisher(ORG_ + organization.getMnemonic());
        } else if(user != null) {
            valueSet.setPublisher(USER_ + user.getUsername());
        }
        // set description
        if(isValid(collection.getDescription()))
            valueSet.setDescription(collection.getDescription());
        // set status
        addStatus(valueSet, collection.getIsActive(), collection.getRetired() != null ? collection.getRetired() : false,
                collection.getReleased());
        // set immutable
        if(collection.getReleased() != null)
            valueSet.setImmutable(collection.getReleased());

        return valueSet;
    }

    private void addStatus(ValueSet valueSet, boolean active, boolean retired, boolean released) {
        if(active || released) {
            valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
        } else if(retired) {
            valueSet.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            valueSet.setStatus(Enumerations.PublicationStatus.UNKNOWN);
        }
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
                                if (FILTER_CODE_CC.equals(property.getAsString()) || FILTER_CODE_DT.equals(property.getAsString())) {
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
