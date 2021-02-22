package org.openconceptlab.fhir.converter;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.PublicationStatus;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.getOwner;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * The ValueSerConverter.
 * @author harpatel1
 */
@Component
public class ValueSetConverter extends BaseConverter {

    @Value("${ocl.servlet.baseurl}")
    private String baseUrl;

    private String validateConceptIdSql =
            "select c1.id, c1.mnemonic from concepts c1 " +
                    "inner join (" +
                    "select c2.mnemonic as mnemonic, max(c2.created_at) as created_at from concepts c2 " +
                    "inner join concepts_sources cs on c2.id = cs.concept_id " +
                    "inner join sources s ON s.id = cs.source_id " +
                    "where cs.source_id = :sourceId and c2.mnemonic in (:conceptIds) group by c2.mnemonic) c3 " +
                    "on c1.mnemonic = c3.mnemonic and c1.created_at = c3.created_at";

    private static final Map<String,Object> collReferenceParamMap = new HashMap<>();
    private static final String insertCollectionsReferences = "insert into collections_references (collection_id,collectionreference_id) values (?,?)";
    private static final String insertCollectionsConcepts = "insert into collections_concepts (collection_id,concept_id) values (?,?)";

    public ValueSetConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                             UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                             AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                             OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
                             MappingRepository mappingRepository) {
        super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
                userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, mappingRepository);
    }

    @PostConstruct
    public void initValueSetConverter() {
        this.insertCollectionReference = new SimpleJdbcInsert(jdbcTemplate).withTableName("collection_references");
    }

    public List<ValueSet> convertToValueSet(List<Collection> collections, boolean includeCompose, Integer page, StringBuilder hasNext) {
        List<ValueSet> valueSets = new ArrayList<>();
        if (!includeCompose) {
            int offset = page * 10;
            int count = 10;
            if (page == 0) {
                if (collections.size() > count) hasNext.append(True);
            } else if (page < collections.size()/count) {
                hasNext.append(True);
            }
            collections = paginate(collections, offset, count);
        }
        collections.forEach(collection -> {
            ValueSet valueSet = toBaseValueSet(collection);
            if (includeCompose)
                addCompose(valueSet, collection, False, page, hasNext);
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
        addStatus(valueSet, collection.getRetired() != null ? collection.getRetired() : False,
                collection.getReleased());
        // publisher
        if (isValid(collection.getPublisher()))
            valueSet.setPublisher(collection.getPublisher());
        // override default identifier with database value
        // identifier, contact, jurisdiction
        addJsonFields(valueSet, isValid(collection.getIdentifier()) && !EMPTY_JSON.equals(collection.getIdentifier())
                ? collection.getIdentifier() : EMPTY, collection.getContact(), collection.getJurisdiction());
        // purpose
        if (isValid(collection.getPurpose()))
            valueSet.setPurpose(collection.getPurpose());
        // copyright
        if (isValid(collection.getCopyright()))
            valueSet.setCopyright(collection.getCopyright());
        // immutable
        if (collection.getImmutable() != null)
            valueSet.setImmutable(collection.getImmutable());
        // revision date
        if (collection.getRevisionDate() != null)
            valueSet.setDate(collection.getRevisionDate());
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

    private void addCompose(ValueSet valueSet, Collection collection, boolean includeConceptDesignation, Integer page, StringBuilder hasNext) {
        // We have to use expressions to determine actual Source version since its not possible through CollectionsConcepts
        IntegerType offset = new IntegerType(page * 100);
        IntegerType count = new IntegerType(100);

        List<String> allExpressions = getAllExpressions(collection);
        if (page < allExpressions.size()/count.getValue() + 1) hasNext.append(True);

        List<String> expressions = getExpressions(allExpressions, offset, count);

        // lets get all the source versions first to reduce the database calls
        List<Source> sources = getSourcesFromExpressions(expressions);

        // for each source let's evaluate expressions for concept and concept version and populate compose
        sources.forEach(source -> {
            expressions.stream().map(m -> formatExpression(m).split(FS))
                    .filter(m -> source.getMnemonic().equals(getSourceId(m)) && source.getVersion().equals(getSourceVersion(m)))
                    .forEachOrdered(m -> {
                        String conceptId = getConceptId(m);
                        String conceptVersion = getConceptVersion(m);
                        if (isValid(conceptId)) {
                            // we can not simply get concepts list from the source considering huge number of concepts, this is alternate
                            // way to get only concepts that we care about and not retrieve whole list. This has improved performance and consumes less memory
                            Optional<Concept> conceptOpt = oclFhirUtil.getSourceConcept(source, conceptId, conceptVersion);
                            conceptOpt.ifPresent(c -> {
                                populateCompose(valueSet, includeConceptDesignation, c, isValid(source.getCanonicalUrl()) ? source.getCanonicalUrl() : source.getUri()
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
                    throw new InvalidRequestException("Code system of url=" + ar[0] + ", version=" + ar[1] + " does not exist.");
                sourcesProvided.add(source);
            }
        }
        Map<String, String> map = sourcesProvided.parallelStream().collect(Collectors.toMap(Source::getMnemonic, Source::getVersion));

        // final list of sources and will only include sources that are referenced in expressions
        // this is to cover the edge case where user provides source that is not referenced in expression and we would
        // want to use sources that are referenced in expressions
        List<Source> filtered = new ArrayList<>();
        expressions.parallelStream().map(m -> formatExpression(m).split(FS))
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
        return expressions.parallelStream().map(m -> formatExpression(m).split(FS))
                .map(m -> ownerType(m) + "|" + ownerId(m) + "|" + getSourceId(m) + "|" + getSourceVersion(m))
                .distinct()
                .map(m -> m.split("\\|"))
                .filter(m -> m.length == 4)
                .map(m -> oclFhirUtil.getSourceVersion(m[2], m[3], publicAccess, m[0], m[1]))
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Source::getCanonicalUrl).thenComparing(Source::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    private List<String> getAllExpressions(Collection collection) {
        Map<String, String> map = new TreeMap<>();
        collection.getCollectionsReferences().stream()
                .map(CollectionsReference::getCollectionReference)
                .map(CollectionReference::getExpression)
                .forEach(e -> {
                    String [] ar = formatExpression(e).split(FS);
                    String conceptId = getConceptId(ar);
                    if (isValid(conceptId))
                        map.put(conceptId + getConceptVersion(ar) + getSourceId(ar) + getSourceVersion(ar), e);
                });

        return new ArrayList<>(map.values());
    }

    private List<String> getExpressions(Collection collection, IntegerType offset, IntegerType count) {
        List<String> expressions = getAllExpressions(collection);
        return getExpressions(expressions, offset, count);
    }

    private List<String> getExpressions(List<String> expressions, IntegerType offset, IntegerType count) {
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
                valueSet.getCompose().setInactive(True);
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
        referenceComponent.setDisplay(oclFhirUtil.getDisplayForLanguage(lts, dictDefaultLocale).orElse(""));
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
        BooleanType result = new BooleanType(False);
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
                        result.setValue(True);
                    }
                }
            } else {
                result.setValue(True);
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
            addStatus(valueSet, collection.getRetired() != null ? collection.getRetired() : False,
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
            addParameter(expansion, FILTER, filter);
        excludeSystem.forEach(e -> addParameter(expansion, EXCLUDE_SYSTEM, newStringType(e)));
        systemVersion.forEach(e -> addParameter(expansion, SYSTEMVERSION, newStringType(e)));

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
                                return False;
                            }
                        } else {
                            return !es.equals(canonical(s.getCanonicalUrl(), s.getVersion()));
                        }
                    }
                    return True;
                })
                .collect(Collectors.toList());
        Map<String, String> map = systemVersion.parallelStream().map(m -> m.split("\\|"))
                .filter(m -> m.length == 2)
                .collect(Collectors.toMap(m -> m[0], m->m[1]));
        sources.forEach(source -> {
            expressions.stream().map(m -> formatExpression(m).split(FS))
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
                                // only return non retired concepts when activeOnly is True
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
        return FS + (ORG.equals(ownerType) ? ORGS : USERS) +
                FS + ownerId +
                FS + SOURCES +
                FS + sourceId +
                FS + (!isValid(sourceVersion) || HEAD.equals(sourceVersion) ? EMPTY : sourceVersion + FS) +
                CONCEPTS + FS + conceptId + FS;
    }

    private String getSourceId(String[] ar) {
        return ar.length >= 5 && !ar[4].isEmpty() ? ar[4] : EMPTY;
    }

    private String getSourceVersion(String[] ar) {
        return ar.length >= 6 && !ar[5].isEmpty() && ar[5].equals(CONCEPTS) ? HEAD : ar[5];
    }

    private String getConceptId(String[] ar) {
        return ar.length >= 6 && !ar[5].isEmpty() && ar[5].equals(CONCEPTS) ?
                ar.length >= 7 ? ar[6] : EMPTY :
                ar.length >= 8 ? ar[7] : EMPTY;
    }

    private String getConceptVersion(String[] ar) {
        if (ar[6].equals(CONCEPTS)) {
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

    public void createValueSet(ValueSet valueSet, String accessionId, String authToken) {
        // validate and authenticate
        OclEntity oclEntity = new OclEntity(valueSet, accessionId, authToken, true);
        UserProfile user = oclEntity.getUserProfile();
        // base collection
        Collection collection = toBaseCollection(valueSet, user, oclEntity.getAccessionId());
        collection.setPublicAccess(VIEW);
        // add parent and access
        addParent(collection, oclEntity.getOwner());
        // add identifier, contact and jurisdiction
        addJsonStrings(valueSet, collection);
        // add concepts
        Map<Source,List<String>> sourceToConceptMap = toConcepts(valueSet.getCompose(), valueSet.getLanguage());
        Map<Long,String> validatedConceptIds = new HashMap<>();
        Set<String> expressions = new HashSet<>();
        sourceToConceptMap.forEach((source,conceptIds) -> {
            // validate source-concept relationship and build reference expressions
            String ownerType = source.getOrganization() != null ? ORGS : USERS;
            String owner = source.getOrganization() != null ? source.getOrganization().getMnemonic() :
                    source.getUserId().getUsername();
            Map<Long,String> validated = getValidatedConceptIds(source.getId(), conceptIds);
            validatedConceptIds.putAll(validated);
            expressions.addAll(toExpression(ownerType, owner, source.getMnemonic(), source.getVersion(), validated.values()));
        });
        // save collection
        collectionRepository.saveAndFlush(collection);
        // save collection reference
        List<Integer> referenceIds = expressions.stream().map(m -> insert(insertCollectionReference, toMap(m)))
                .map(Long::intValue)
                .collect(Collectors.toList());
        // save collections references
        batchInsert(insertCollectionsReferences, collection.getId().intValue(), referenceIds);
        // save collections concepts
        batchInsert(insertCollectionsConcepts, collection.getId().intValue(),
                validatedConceptIds.keySet().stream().map(Long::intValue).collect(Collectors.toList()));
        // clear data
        sourceToConceptMap.clear();
        validatedConceptIds.clear();
        expressions.clear();
    }

    private Map<Source,List<String>> toConcepts(ValueSet.ValueSetComposeComponent component, String defaultLocale) {
        Map<Source,List<String>> map = new HashMap<>();
        component.getInclude().forEach(c -> {
            // validate CodeSystem
            Source source;
            String system = c.getSystem();
            String version = c.getVersion();
            if (!isValid(system))
                throw new InvalidRequestException("Field 'system' of compose.include.* is required.");
            if (isValid(version)) {
                source = sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(system, version, publicAccess);
            } else {
                source = sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                        system, True, publicAccess
                );
                // we need to update source version for later use. If version if not provided then we don't want to store
                // source version info in expressions. This won't be persisted in db, it is for internal user only.
                source.setVersion(EMPTY);
            }
            if (source == null)
                throw new InvalidRequestException(String.format("The CodeSystem %s|%s does not exist.", system, version));
            // extract concept codes
            List<String> conceptIds = c.getConcept().parallelStream()
                    .filter(f -> isValid(f.getCode()))
                    .map(ValueSet.ConceptReferenceComponent::getCode)
                    .collect(Collectors.toList());
            // clear extra source data
            source.getConcepts().clear();
            source.getMappings().clear();
            source.getConceptsSources().clear();
            source.getMappingsSources().clear();

            map.put(source, conceptIds);
        });
        return map;
    }

    protected Map<Long,String> getValidatedConceptIds(Long sourceId, List<String> conceptIds) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sourceId", sourceId);
        parameters.addValue("conceptIds", conceptIds);
        Map<Long,String> map = new HashMap<>();
        namedParameterJdbcTemplate.query(validateConceptIdSql, parameters, (RowMapper<Void>) (rs, rowNum) -> {
            map.put(rs.getLong(1), rs.getString(2));
            return null;
        });
        return map;
    }

    private List<String> toExpression(String ownerType, String owner, String sourceId, String sourceVersion,
                                      java.util.Collection<String> conceptIds) {
        return conceptIds.parallelStream().map(m -> toExpression(ownerType, owner, sourceId, sourceVersion, m))
                .collect(Collectors.toList());
    }

    private String toExpression(String ownerType, String owner, String sourceId, String sourceVersion, String conceptId) {
        String pre = FS + ownerType + FS + owner + FS + SOURCES + FS + sourceId + FS;
        if (isValid(sourceVersion) && !HEAD.equals(sourceVersion))
            pre = pre + sourceVersion + FS;
        return pre + CONCEPTS + FS + conceptId + FS;
    }

    private Collection toBaseCollection(final ValueSet valueSet, final UserProfile user, final String uri) {
        Collection collection = new Collection();
        // mnemonic
        collection.setMnemonic(valueSet.getId());
        // canonical url
        collection.setCanonicalUrl(valueSet.getUrl());
        // created by
        collection.setCreatedBy(user);
        // updated by
        collection.setUpdatedBy(user);

        // draft or unknown or empty
        collection.setIsActive(True);
        collection.setIsLatestVersion(True);
        collection.setRetired(False);
        collection.setReleased(False);
        if (valueSet.getStatus() != null) {
            // active
            if (PublicationStatus.ACTIVE.toCode().equals(valueSet.getStatus().toCode())) {
                collection.setReleased(True);
                // retired
            } else if (PublicationStatus.RETIRED.toCode().equals(valueSet.getStatus().toCode())) {
                collection.setRetired(True);
                collection.setReleased(False);
                collection.setIsActive(False);
                collection.setIsLatestVersion(False);
            }
        }
        // version
        collection.setVersion(valueSet.getVersion());
        // default locale
        collection.setDefaultLocale(isValid(valueSet.getLanguage()) ? valueSet.getLanguage() : EN_LOCALE);
        // uri
        collection.setUri(toOclUri(uri));
        // name
        String name = isValid(valueSet.getName()) ? valueSet.getName() : valueSet.getId();
        collection.setName(name);
        // copyright
        if (isValid(valueSet.getCopyright()))
            collection.setCopyright(valueSet.getCopyright());
        // description
        if (isValid(valueSet.getDescription()))
            collection.setDescription(valueSet.getDescription());
        // title
        if (isValid(valueSet.getTitle()))
            collection.setFullName(valueSet.getTitle());
        // publisher
        if (isValid(valueSet.getPublisher()))
            collection.setPublisher(valueSet.getPublisher());
        // purpose
        if (isValid(valueSet.getPurpose()))
            collection.setPurpose(valueSet.getPurpose());
        // revision date
        if (valueSet.getDate() != null)
            collection.setRevisionDate(valueSet.getDate());
        // extras
        collection.setExtras(EMPTY_JSON);
        // immutable
        collection.setImmutable(valueSet.getImmutable());
        return collection;
    }

    private void addParent(final Collection collection, final BaseOclEntity owner) {
        if (owner instanceof Organization) {
            Organization organization = (Organization) owner;
            collection.setOrganization(organization);
            collection.setPublicAccess(organization.getPublicAccess());
        } else if (owner instanceof UserProfile){
            collection.setUserId((UserProfile) owner);
        }
    }

    private static Map<String,Object> toMap(String expression) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        collReferenceParamMap.put(EXPRESSION, expression);
        collReferenceParamMap.put(CREATED_AT, timestamp);
        collReferenceParamMap.put(UPDATED_AT, timestamp);
        collReferenceParamMap.put(LAST_RESOLVED_AT, timestamp);
        return collReferenceParamMap;
    }

    private void batchInsert(String sql, Integer collectionId, List<Integer> integerList) {
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setInt(1, collectionId);
                ps.setInt(2, integerList.get(i));
            }
            public int getBatchSize() {
                return integerList.size();
            }
        });
    }
}
