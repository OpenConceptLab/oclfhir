package org.openconceptlab.fhir.converter;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.ConceptsSourceRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;

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
    SourceRepository sourceRepository;

    @Autowired
    public ValueSetConverter(OclFhirUtil oclFhirUtil, ConceptsSourceRepository conceptsSourceRepository,
                             ConceptRepository conceptRepository, SourceRepository sourceRepository) {
        this.oclFhirUtil = oclFhirUtil;
        this.conceptsSourceRepository = conceptsSourceRepository;
        this.conceptRepository = conceptRepository;
        this.sourceRepository = sourceRepository;
    }

    @Value("${ocl.servlet.baseurl}")
    private String baseUrl;

    public List<ValueSet> convertToValueSet(List<Collection> collections, Integer page) {
        List<ValueSet> valueSets = new ArrayList<>();
        collections.forEach(collection -> {
            ValueSet valueSet = toBaseValueSet(collection);
            if (page != null)
                addCompose(valueSet, collection, false, page);
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

    private void addCompose(ValueSet valueSet, Collection collection, boolean includeConceptDesignation, Integer page) {
        // We have to use expressions to determine actual Source version since its not possible through CollectionsConcepts
        IntegerType offset = new IntegerType(page * 100);
        IntegerType count = new IntegerType(100);

        List<String> expressions = getExpressions(collection, offset, count);

        // lets get all the source versions first to reduce the database calls
        List<Source> sources = getSourcesFromExpressions(expressions);

        // for each source let's evaluate expressions for concept and concept version and populate compose
        sources.forEach(source -> {
            expressions.stream().map(m -> formatExpression(m).split("/"))
                    .filter(m -> source.getMnemonic().equals(getSourceId(m)) && source.getVersion().equals(getSourceVersion(m)))
                    .forEachOrdered(m -> {
                        String conceptId = getConceptId(m);
                        String conceptVersion = getConceptVersion(m);
                        if (isValid(conceptId)) {
                            // we can not simply get concepts list from the source considering huge number of concepts, this is alternate
                            // way to get only concepts that we care about and not retrieve whole list. This has improved performance and consumes less memory
                            Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, conceptId, conceptVersion);
                            conceptOpt.ifPresent(c -> {
                                populateCompose(valueSet, includeConceptDesignation, c, source.getCanonicalUrl()
                                        , source.getVersion(), source.getDefaultLocale());
                            });
                        }
                    });
        });
    }

    private List<Source> getSourcesFromExpressions(List<String> expressions, List<String> sourceVersions) {
        if (sourceVersions.isEmpty()) return getSourcesFromExpressions(expressions);
        // get the sources given based on input parameter source-version
        List<Source> sourcesProvided = new ArrayList<>();
        for (String sv : sourceVersions) {
            String[] ar = sv.split("\\|");
            if (ar.length == 2) {
                Source source = sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(ar[0], ar[1], publicAccess);
                if (source == null)
                    throw new UnprocessableEntityException("Code system of url=" + ar[0] + ", version=" + ar[1] + " does not exist.");
                sourcesProvided.add(source);
            }
        }
        Map<String, String> map = sourcesProvided.parallelStream().collect(Collectors.toMap(Source::getMnemonic, Source::getVersion));

        // final list of sources and will only include sources that are referenced in expressions
        // this is to cover the edge case where user provides source that is not referenced in expression and we would
        // want to use sources that are referenced in expressions
        List<Source> filtered = new ArrayList<>();
        expressions.parallelStream().map(m -> formatExpression(m).split("/"))
                .map(m -> ownerType(m) + "|" + ownerId(m) + "|" + getSourceId(m) + "|" + getSourceVersion(m))
                .distinct()
                .map(m -> m.split("\\|"))
                .filter(m -> m.length == 4)
                .map(m -> {
                    String sourceId = m[2];
                    String sourceVersion = m[3];
                    // If sourceId is part of input system-version and expression's source's version is HEAD only then we'll
                    // want to override the source.
                    if (map.containsKey(sourceId) && HEAD.equals(sourceVersion)) {
                        return sourcesProvided.parallelStream().filter(s -> s.getMnemonic().equals(sourceId)).findFirst().get();
                    } else {
                        // if source is not part of the system-version then don't override
                        return oclFhirUtil.getSourceVersion(m[2], m[3], publicAccess, m[0], m[1]);
                    }
                })
                .filter(Objects::nonNull)
                .forEach(filtered::add);

        return filtered.stream().sorted(Comparator.comparing(Source::getCanonicalUrl).thenComparing(Source::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }


    private List<Source> getSourcesFromExpressions(List<String> expressions) {
        return expressions.parallelStream().map(m -> formatExpression(m).split("/"))
                .map(m -> ownerType(m) + "|" + ownerId(m) + "|" + getSourceId(m) + "|" + getSourceVersion(m))
                .distinct()
                .map(m -> m.split("\\|"))
                .filter(m -> m.length == 4)
                .map(m -> oclFhirUtil.getSourceVersion(m[2], m[3], publicAccess, m[0], m[1]))
                .sorted(Comparator.comparing(Source::getCanonicalUrl).thenComparing(Source::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    private List<String> getExpressions(Collection collection) {
        Map<String, String> map = new TreeMap<>();
        collection.getCollectionsReferences().stream()
                .map(CollectionsReference::getCollectionReference)
                .map(CollectionReference::getExpression)
                .forEach(e -> {
                    String [] ar = formatExpression(e).split("/");
                    String conceptId = getConceptId(ar);
                    if (isValid(conceptId))
                        map.put(conceptId + getConceptVersion(ar), e);
                });

        return new ArrayList<>(map.values());
    }

    private List<String> getExpressions(Collection collection, IntegerType offset, IntegerType count) {
        List<String> expressions = getExpressions(collection);
        if (count.getValue() == 0)
            return expressions;
        if (offset.getValue() < expressions.size()) {
            int start = offset.getValue();
            int end = expressions.size();
            if (start + count.getValue() < end)
                end = start + count.getValue();
            expressions = expressions.subList(start, end);
        } else {
            expressions.clear();
        }
        return expressions;
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
            addConceptReferenceDesignation(lts, referenceComponent);
        includeComponent.getConcept().add(referenceComponent);
    }

    private void addConceptReferenceDesignation(List<LocalizedText> names, ValueSet.ConceptReferenceComponent referenceComponent) {
        names.parallelStream().forEach(lt -> {
            ValueSet.ConceptReferenceDesignationComponent component = toConceptRefDesignationComp(lt);
            if (component != null)
                referenceComponent.addDesignation(component);
        });
    }

    private void addConceptReferenceDesignation(List<LocalizedText> names, ValueSet.ValueSetExpansionContainsComponent expansionComponent) {
        names.parallelStream().forEach(lt -> {
            ValueSet.ConceptReferenceDesignationComponent component = toConceptRefDesignationComp(lt);
            if (component != null)
                expansionComponent.addDesignation(component);
        });
    }

    private ValueSet.ConceptReferenceDesignationComponent toConceptRefDesignationComp(LocalizedText text) {
        if (text != null) {
            ValueSet.ConceptReferenceDesignationComponent designationComponent = new ValueSet.ConceptReferenceDesignationComponent();
            designationComponent.setLanguage(text.getLocale());
            if (isValid(text.getType()))
                designationComponent.getUse().setCode(text.getType());
            designationComponent.setValue(text.getName());
            return designationComponent;
        }
        return null;
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
                StringType updated = newStringType(display.getValue().replaceAll("^\"", "")
                        .replaceAll("\"$", ""));
                Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, code, EMPTY);
                if (conceptOpt.isPresent()) {
                    List<LocalizedText> names = oclFhirUtil.getNames(conceptOpt.get());
                    boolean match = oclFhirUtil.validateDisplay(names, updated, displayLanguage);
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

    public ValueSet expand(Collection collection, IntegerType offset, IntegerType count, BooleanType includeDesignations,
                           BooleanType includeDefinition, BooleanType activeOnly, CodeType displayLanguage,
                           List<String> excludeSystem, List<String> systemVersion, StringType filter) {
        ValueSet valueSet;
        if (isTrue(includeDefinition)) {
            valueSet = toBaseValueSet(collection);
        } else {
            valueSet = new ValueSet();
            addStatus(valueSet, collection.getIsActive(), collection.getRetired() != null ? collection.getRetired() : false,
                    collection.getReleased());
        }
        CanonicalType canonicalReference = new CanonicalType(
                collection.getCanonicalUrl() + "|" + collection.getVersion());
        valueSet.getCompose().getIncludeFirstRep().getValueSet().add(canonicalReference);

        ValueSet.ValueSetExpansionComponent expansion = valueSet.getExpansion();
        // identifier
        expansion.setIdentifier(UUID.randomUUID().toString());
        // timestamp
        expansion.setTimestamp(new Date());
        // add expansion parameters
        addParameter(expansion, URL, new UriType(collection.getCanonicalUrl()));
        addParameter(expansion, VALUESET_VERSION, newStringType(collection.getVersion()));
        addParameter(expansion, OFFSET, offset);
        addParameter(expansion, COUNT, count);
        addParameter(expansion, INCLUDE_DESIGNATIONS, includeDesignations);
        addParameter(expansion, INCLUDE_DEFINITION, includeDefinition);
        addParameter(expansion, ACTIVE_ONLY, activeOnly);
        if (isValid(displayLanguage))
            addParameter(expansion, DISPLAY_LANGUAGE, displayLanguage);
        if (isValid(filter))
            addParameter(expansion, "filter", filter);

        // offset
        expansion.setOffset(offset.getValue());

        // get expressions and filter by offset and count
        // returns all expression if count = 0
        List<String> expressions = getExpressions(collection, offset, count);

        // if count = 0 then client is asking how large the expansion is.(As per FHIR spec)
        if (count.getValue() == 0) {
            expansion.setTotal(expressions.size());
            return valueSet;
        }

        // separator = __ and if value itself has _ then enclose value in ""
        // filter = EMR__HRP__KP , match on EMR, HRP and KP
        // filter = EMR__"HRH_"__KP, match on EMR, HRP_ and KP
        List<String> filters = new ArrayList<>();
        if (isValid(filter)) {
            String[] arr = filter.getValue().split("__");
            for (String s : arr) {
                filters.add(s.replaceAll("\"", EMPTY));
            }
        }

        List<Source> sources = getSourcesFromExpressions(expressions, systemVersion)
                .stream()
                .filter(s -> {
                    for(String es : excludeSystem) {
                        String [] ar = es.split("\\|");
                        if ((ar.length == 2 && !isValid(ar[1])) || ar.length == 1) {
                            if (s.getCanonicalUrl().equals(es) || s.getCanonicalUrl().equals(ar[0])) {
                                return false;
                            }
                        } else {
                            return !es.equals(canonical(s.getCanonicalUrl(), s.getVersion()));
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        Map<String, String> map = systemVersion.parallelStream().map(m -> m.split("\\|"))
                .filter(m -> m.length == 2)
                .collect(Collectors.toMap(m -> m[0], m->m[1]));
        sources.forEach(source -> {
            expressions.stream().map(m -> formatExpression(m).split("/"))
                    .filter(m -> {
                        if (map.containsKey(source.getCanonicalUrl()))
                            return map.get(source.getCanonicalUrl()).equals(source.getVersion());
                        return source.getMnemonic().equals(getSourceId(m)) && source.getVersion().equals(getSourceVersion(m));
                    })
                    .forEachOrdered(m -> {
                        String conceptId = getConceptId(m);
                        String conceptVersion = getConceptVersion(m);
                        if (isValid(conceptId)) {
                            Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, conceptId, conceptVersion);
                             conceptOpt.ifPresent(c -> {
                                // only return non retired concepts when activeOnly is true
                                if (c.getRetired() && activeOnly.booleanValue()) {
                                    return;
                                }
                                // apply concept code filter if provided
                                if (!filters.isEmpty()) {
                                    if (filters.parallelStream().noneMatch(conceptId::contains))
                                        return;
                                }
                                ValueSet.ValueSetExpansionContainsComponent component = new ValueSet.ValueSetExpansionContainsComponent();
                                component.setSystem(source.getCanonicalUrl());
                                component.setVersion(source.getVersion());
                                component.setInactive(c.getRetired());
                                component.setCode(c.getMnemonic());
                                List<LocalizedText> names = oclFhirUtil.getNames(c);
                                if (isValid(displayLanguage)) {
                                    oclFhirUtil.getDisplayForLanguage(names, displayLanguage.getCode())
                                            .ifPresent(component::setDisplay);
                                } else {
                                    oclFhirUtil.getDisplayForLanguage(names, source.getDefaultLocale())
                                            .ifPresent(component::setDisplay);
                                }
                                if (includeDesignations.getValue()) {
                                    addConceptReferenceDesignation(names, component);
                                }
                                expansion.getContains().add(component);
                            });
                        }
                    });
        });
        // sort based on canonical_url,version desc and code asc
        List<ValueSet.ValueSetExpansionContainsComponent> sorted = expansion.getContains().stream()
                        .sorted(Comparator.comparing(ValueSet.ValueSetExpansionContainsComponent::getSystem)
                        .thenComparing(ValueSet.ValueSetExpansionContainsComponent::getVersion)
                        .reversed()
                        .thenComparing(ValueSet.ValueSetExpansionContainsComponent::getCode))
                        .collect(Collectors.toList());
        expansion.setContains(sorted);
        // total
        expansion.setTotal(expansion.getContains().size());
        return valueSet;
    }

    private String canonical(String url, String version) {
        return url + "|" + version;
    }

    private void addParameter(ValueSet.ValueSetExpansionComponent expansion, String name, Type value) {
        ValueSet.ValueSetExpansionParameterComponent component = new ValueSet.ValueSetExpansionParameterComponent();
        component.setName(name);
        component.setValue(value);
        expansion.getParameter().add(component);
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
