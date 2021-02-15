package org.openconceptlab.fhir.converter;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.openconceptlab.fhir.converter.CodeSystemConverter.DEFAULT_RES_VERSION;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.gson;

@Component
public class BaseConverter {

    protected SourceRepository sourceRepository;
    protected ConceptRepository conceptRepository;
    protected OclFhirUtil oclFhirUtil;
    protected UserProfile oclUser;
    protected ConceptsSourceRepository conceptsSourceRepository;
    protected AuthtokenRepository authtokenRepository;
    protected UserProfilesOrganizationRepository userProfilesOrganizationRepository;
    protected OrganizationRepository organizationRepository;
    protected UserRepository userRepository;
    protected SimpleJdbcInsert insertLocalizedText;
    protected SimpleJdbcInsert insertConcept;
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;
    protected CollectionRepository collectionRepository;
    protected SimpleJdbcInsert insertCollectionReference;
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected MappingRepository mappingRepository;

    protected static final String insertConceptNamesSql = "insert into concepts_names (localizedtext_id,concept_id) values (?,?)";
    protected static final String insertConceptDescSql = "insert into concepts_descriptions (localizedtext_id,concept_id) values (?,?)";
    protected static final String updateConceptVersionSql = "update concepts set version = ? where id = ?";
    protected static final String insertConceptsSources = "insert into concepts_sources (concept_id,source_id) values (?,?)";
    private static final Log log = LogFactory.getLog(BaseConverter.class);

    @Autowired
    public BaseConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                         UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                         AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                         OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository,
                         MappingRepository mappingRepository) {
        this.sourceRepository = sourceRepository;
        this.conceptRepository = conceptRepository;
        this.oclFhirUtil = oclFhirUtil;
        this.oclUser = oclUser;
        this.conceptsSourceRepository = conceptsSourceRepository;
        this.dataSource = dataSource;
        this.authtokenRepository = authtokenRepository;
        this.userProfilesOrganizationRepository = userProfilesOrganizationRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
        this.mappingRepository = mappingRepository;
    }

    @PostConstruct
    public void init() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        insertLocalizedText = new SimpleJdbcInsert(jdbcTemplate).withTableName("localized_texts");
        insertConcept = new SimpleJdbcInsert(jdbcTemplate).withTableName("concepts");
    }

    protected BaseOclEntity validateOwner(String org, String username) {
        if (isValid(org)) {
            return validateOrg(org);
        } else {
            return validateUser(username);
        }
    }

    protected BaseOclEntity validateOrg(String org) {
        Organization organization = organizationRepository.findByMnemonic(org);
        if (organization == null) {
            throw new InvalidRequestException("The organization of id = " + org + " does not exist.");
        } else {
            return organization;
        }
    }

    protected BaseOclEntity validateUser(String username) {
        UserProfile userProfile = userRepository.findByUsername(username);
        if (userProfile == null) {
            throw new InvalidRequestException("The user of username = " + username + " does not exist.");
        } else {
            return userProfile;
        }
    }

    protected AuthtokenToken validateToken(String authToken) {
        if (isValid(authToken)) {
            String tokenStr = authToken.replaceAll("Token\\s+", EMPTY);
            return authtokenRepository.findByKey(tokenStr.trim());
        } else {
            throw new AuthenticationException("The authentication token is not provided.");
        }
    }

    protected void authenticate(AuthtokenToken token, String username, String org) {
        if (token == null) {
            throw new AuthenticationException("Invalid authentication token.");
        }
        if (isValid(username)) {
            if (!username.equals(token.getUserProfile().getUsername())) {
                throw new AuthenticationException("The " + username + " is not authorized to use the token provided.");
            }
        } else if (isValid(org)) {
            boolean isMember = userProfilesOrganizationRepository.findByOrganizationMnemonic(org)
                    .stream()
                    .map(UserProfilesOrganization::getUserProfile)
                    .anyMatch(f -> f.getUsername().equals(token.getUserProfile().getUsername())
                            && f.getAuthtokenTokens().stream().anyMatch(t -> t.getKey().equals(token.getKey())));
            if (!isMember) {
                throw new AuthenticationException("The user " + token.getUserProfile().getUsername() + " is not authorized to access " +
                        org + " organization.");
            }
        } else {
            throw new InvalidRequestException("Owner can not be empty.");
        }
    }

    protected void validateId(String username, String org, String id, String version, String resourceType) {
        if (CODESYSTEM.equals(resourceType)) {
            Source userSource = sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsername(id, version, username);
            Source orgSource = sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonic(id, version, org);
            if (userSource != null || orgSource != null) {
                throw new ResourceVersionConflictException(String.format("The %s %s of version %s already exists.", resourceType, id, version));
            }
        } else if (VALUESET.equals(resourceType)) {
            Collection userCollection = collectionRepository.findFirstByMnemonicAndVersionAndUserIdUsername(id, version, username);
            Collection orgCollection = collectionRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonic(id, version, org);
            if (userCollection != null || orgCollection != null) {
                throw new ResourceVersionConflictException(String.format("The %s %s of version %s already exists.", resourceType, id, version));
            }
        } else {
            throw new InternalErrorException("Invalid resource type.");
        }
    }

    protected void validateCanonicalUrl(String username, String org, String url, String version, String resourceType) {
        if (CODESYSTEM.equals(resourceType)) {
            Source userSource = sourceRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsername(url, version, username);
            Source orgSource = sourceRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(url, version, org);
            if (userSource != null || orgSource != null) {
                throw new ResourceVersionConflictException(String.format("The %s of canonical url %s and version %s already exists.", resourceType, url, version));
            }
        } else if (VALUESET.equals(resourceType)) {
            Collection userCollection = collectionRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsername(url, version, username);
            Collection orgCollection = collectionRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(url, version, org);
            if (userCollection != null || orgCollection != null) {
                throw new ResourceVersionConflictException(String.format("The %s of canonical url %s and version %s already exists.", resourceType, url, version));
            }
        } else {
            throw new InternalErrorException("Invalid resource type.");
        }
    }

    protected void addJsonStrings(final CodeSystem codeSystem, final Source source) {
        source.setIdentifier(convertToJsonString(getResIdentifierString(codeSystem), IDENTIFIER));
        if (!codeSystem.getContact().isEmpty())
            source.setContact(convertToJsonString(getResContactString(codeSystem), CONTACT));
        if (!codeSystem.getJurisdiction().isEmpty())
            source.setJurisdiction(convertToJsonString(getResJurisdictionString(codeSystem), JURISDICTION));
    }

    protected void addJsonStrings(final ValueSet valueSet, final Collection collection) {
        collection.setIdentifier(convertToJsonString(getResIdentifierString(valueSet), IDENTIFIER));
        if (!valueSet.getContact().isEmpty())
            collection.setContact(convertToJsonString(getResContactString(valueSet), CONTACT));
        if (!valueSet.getJurisdiction().isEmpty())
            collection.setJurisdiction(convertToJsonString(getResJurisdictionString(valueSet), JURISDICTION));
    }

    protected void batchUpdateConceptVersion(List<Integer> conceptIds) {
        this.jdbcTemplate.batchUpdate(updateConceptVersionSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setInt(1, conceptIds.get(i));
                ps.setInt(2, conceptIds.get(i));
            }
            public int getBatchSize() {
                return conceptIds.size();
            }
        });
    }

    protected void batchUpdateConceptSources(List<Integer> conceptIds, Long sourceId) {
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

    protected void batchInsertConceptNames(String sql, List<Long> nameIds, Integer conceptId) {
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

    protected void batchConcepts(List<Concept> concepts, List<Integer> conceptIds) {
        concepts.forEach(c -> {
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
            conceptIds.add(conceptId);
        });
    }

    protected List<Long> insertRows(List<LocalizedText> texts) {
        List<Long> keys = new ArrayList<>();
        texts.forEach(t -> {
            keys.add(insert(insertLocalizedText, toMap(t)));
        });
        return keys;
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

    protected Long insert(SimpleJdbcInsert insert, Map<String, Object> parameters) {
        if (!insert.isCompiled())
            insert.usingGeneratedKeyColumns("id");
        Number n = insert.executeAndReturnKeyHolder(parameters).getKey();
        if (n instanceof Long)
            return n.longValue();
        if (n instanceof Integer)
            return Long.valueOf(String.valueOf(n.intValue()));
        return (Long) insert.executeAndReturnKeyHolder(parameters).getKey();
    }

    private String getResContactString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setContact(codeSystem.getContact());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResIdentifierString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setIdentifier(codeSystem.getIdentifier());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResJurisdictionString(final ValueSet valueSet) {
        CodeSystem system = new CodeSystem();
        system.setJurisdiction(valueSet.getJurisdiction());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResContactString(final ValueSet valueSet) {
        CodeSystem system = new CodeSystem();
        system.setContact(valueSet.getContact());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResIdentifierString(final ValueSet valueSet) {
        CodeSystem system = new CodeSystem();
        system.setIdentifier(valueSet.getIdentifier());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResJurisdictionString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setJurisdiction(codeSystem.getJurisdiction());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String convertToJsonString(String fhirResourceStr, String key) {
        JsonObject object = jsonParser.parse(fhirResourceStr).getAsJsonObject();
        if (object.has(RESOURCE_TYPE))
            object.remove(RESOURCE_TYPE);
        if (object.has(key)) {
            if (object.get(key) instanceof JsonArray) {
                return gson.toJson(object.getAsJsonArray(key));
            } else {
                return gson.toJson(object.getAsJsonObject(key));
            }
        }
        return EMPTY_JSON;
    }

    protected String getStringProperty(List<CodeSystem.ConceptPropertyComponent> properties, String property) {
        Optional<CodeSystem.ConceptPropertyComponent> component = properties.parallelStream().filter(p -> property.equals(p.getCode())).findAny();
        if (component.isPresent() && isValid(component.get().getValueStringType().getValue()))
            return component.get().getValueStringType().getValue();
        return NA;
    }

    protected boolean getBooleanProperty(List<CodeSystem.ConceptPropertyComponent> properties, String property) {
        Optional<CodeSystem.ConceptPropertyComponent> component = properties.parallelStream().filter(p -> property.equals(p.getCode())).findAny();
        if (component.isPresent()) {
            BooleanType value = component.get().getValueBooleanType();
            if(value.getValue() != null) return value.getValue();
        }
        return false;
    }

    protected class OclEntity {
        private BaseOclEntity owner;
        private UserProfile userProfile;
        private String accessionId;

        public OclEntity(MetadataResource resource, String accessionId, String authToken, boolean validateIfExists) {
            // we'll support two type of accession id patterns as input
            // 1. /users/testuser/<resource>/Test2/v20.0/
            // 2. /users/testuser/<resource>/Test2/version/v20.0/
            String org = EMPTY;
            String username = EMPTY;
            String resourceId = EMPTY;
            String formattedId = formatExpression(accessionId);
            String [] ar = formattedId.split(FS);
            if (ar.length >= 5) {
                if (ORGS.equals(ar[1]) && isValid(ar[2])) {
                    org = ar[2];
                } else if (USERS.equals(ar[1]) && isValid(ar[2])){
                    username = ar[2];
                }
                if (resource.getClass().getSimpleName().equals(ar[3]) && isValid(ar[4])) {
                    resourceId = ar[4];
                    resource.setId(resourceId);
                }
                if (ar.length >=6 && isValid(ar[5])) {
                    if (VERSION.equals(ar[5])) {
                        if (ar.length >=7 && isValid(ar[6]))
                            resource.setVersion(ar[6]);
                    } else {
                        resource.setVersion(ar[5]);
                    }
                } else if (!isValid(resource.getVersion())) {
                    resource.setVersion(DEFAULT_RES_VERSION);
                    formattedId = formattedId + DEFAULT_RES_VERSION + FS;
                } else {
                    formattedId = formattedId + resource.getVersion() + FS;
                }
            }

            if (org.isEmpty() && username.isEmpty())
                throw new InvalidRequestException("Owner type and id is required.");
            if (resourceId.isEmpty())
                throw new InvalidRequestException("Resource id is required.");

            BaseOclEntity owner = validateOwner(org, username);
            AuthtokenToken token = validateToken(authToken);
            authenticate(token, username, org);
            if (validateIfExists) {
                validateId(username, org, resourceId, resource.getVersion(), resource.getClass().getSimpleName());
                validateCanonicalUrl(username, org, resource.getUrl(), resource.getVersion(), resource.getClass().getSimpleName());
            }
            this.owner = owner;
            this.userProfile = token.getUserProfile();
            this.accessionId = formattedId;
        }

        public BaseOclEntity getOwner() {
            return owner;
        }

        public UserProfile getUserProfile() {
            return userProfile;
        }

        public String getAccessionId() {
            return accessionId;
        }
    }

    protected Parameters.ParametersParameterComponent getParameter(String name, Type value) {
        Parameters.ParametersParameterComponent component = new Parameters.ParametersParameterComponent();
        component.setName(name).setValue(value);
        return component;
    }

    protected Parameters.ParametersParameterComponent getParameter(String name, String value) {
        Parameters.ParametersParameterComponent component = new Parameters.ParametersParameterComponent();
        component.setName(name).setValue(new StringType(value));
        return component;
    }

    protected <T> List<T> paginate(List<T> resources, Integer offset, Integer count) {
        if (count == 0)
            return resources;
        if (offset < resources.size()) {
            int start = offset;
            int end = resources.size();
            if (start + count < end)
                end = start + count;
            resources = resources.subList(start, end);
        } else {
            resources.clear();
        }
        return resources;
    }

    protected void updateIndex(String resource, String... ids) {
        // Update indexes for given resource and ids
        // Use for existing resource
        URI url = null;
        try {
            url = new URI(oclFhirUtil.oclApiBaseUrl() + "/indexes/resources/" + resource + FS);
            new RestTemplate().postForEntity(url, oclFhirUtil.getRequest(getToken(), "ids", ids), String.class);
        } catch (Exception e) {
            String msg = String.format("Could not update index. Url - %s. Parameters - Key:ids, Value:%s. \n %s",
                    url, Arrays.toString(ids), e.getMessage());
            log.error(msg);
        }
    }

    protected void populateIndex(String... apps) {
        // populate index for new app
        // Use for new resource
        URI url = null;
        try {
            url = new URI(oclFhirUtil.oclApiBaseUrl() + "/indexes/apps/populate/");
            new RestTemplate().postForEntity(url, oclFhirUtil.getRequest(getToken(), "apps", apps), String.class);
        } catch (Exception e) {
            String msg = String.format("Could not populate index. Url - %s. Parameters - Key:apps, Value:%s. \n %s",
                    url, Arrays.toString(apps), e.getMessage());
            log.error(msg);
        }
    }

    protected void rebuildIndex(String... apps) {
        // rebuild index for given app
        URI url = null;
        try {
            url = new URI(oclFhirUtil.oclApiBaseUrl() + "/indexes/apps/rebuild/");
            new RestTemplate().postForEntity(url, oclFhirUtil.getRequest(getToken(), "apps", apps), String.class);
        } catch (Exception e) {
            String msg = String.format("Could not rebuild index. Url - %s. Parameters - Key:apps, Value:%s. \n %s",
                    url, Arrays.toString(apps), e.getMessage());
            log.error(msg);
        }
    }

    private Optional<String> getToken() {
        return oclUser.getAuthtokenTokens().stream()
                .filter(f -> isValid(f.getKey()))
                .map(m -> "Token " + m.getKey())
                .findFirst();
    }
}

