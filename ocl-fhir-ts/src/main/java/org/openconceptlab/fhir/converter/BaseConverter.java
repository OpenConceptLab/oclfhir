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
import org.hl7.fhir.r4.model.codesystems.PublicationStatus;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

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
    protected SimpleJdbcInsert insertMapping;
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;
    protected CollectionRepository collectionRepository;
    protected SimpleJdbcInsert insertCollectionReference;
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected MappingRepository mappingRepository;

    private static final Log log = LogFactory.getLog(BaseConverter.class);
    protected static final String DEFAULT_RES_VERSION = "0.1";

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
        insertMapping = new SimpleJdbcInsert(jdbcTemplate).withTableName("mappings");
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
        if (CODESYSTEM.equals(resourceType) || CONCEPTMAP.equals(resourceType)) {
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
        if (CODESYSTEM.equals(resourceType) || CONCEPTMAP.equals(resourceType)) {
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
        if (!codeSystem.getIdentifier().isEmpty())
            source.setIdentifier(convertToJsonString(getResIdentifierString(codeSystem), IDENTIFIER));
        if (!codeSystem.getContact().isEmpty())
            source.setContact(convertToJsonString(getResContactString(codeSystem), CONTACT));
        if (!codeSystem.getJurisdiction().isEmpty())
            source.setJurisdiction(convertToJsonString(getResJurisdictionString(codeSystem), JURISDICTION));
    }

    protected void addJsonStrings(final ValueSet valueSet, final Collection collection) {
        if (!valueSet.getIdentifier().isEmpty())
            collection.setIdentifier(convertToJsonString(getResIdentifierString(valueSet), IDENTIFIER));
        if (!valueSet.getContact().isEmpty())
            collection.setContact(convertToJsonString(getResContactString(valueSet), CONTACT));
        if (!valueSet.getJurisdiction().isEmpty())
            collection.setJurisdiction(convertToJsonString(getResJurisdictionString(valueSet), JURISDICTION));
    }

    protected void addJsonStrings(final ConceptMap conceptMap, final Source source) {
        if (!conceptMap.getIdentifier().isEmpty())
            source.setIdentifier(convertToJsonString(getResIdentifierString(conceptMap), IDENTIFIER));
        if (!conceptMap.getContact().isEmpty())
            source.setContact(convertToJsonString(getResContactString(conceptMap), CONTACT));
        if (!conceptMap.getJurisdiction().isEmpty())
            source.setJurisdiction(convertToJsonString(getResJurisdictionString(conceptMap), JURISDICTION));
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

    private String getResIdentifierString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setIdentifier(codeSystem.getIdentifier());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResContactString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setContact(codeSystem.getContact());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResJurisdictionString(final CodeSystem codeSystem) {
        CodeSystem system = new CodeSystem();
        system.setJurisdiction(codeSystem.getJurisdiction());
        return getFhirContext().newJsonParser().encodeResourceToString(system);
    }

    private String getResIdentifierString(final ValueSet valueSet) {
        ValueSet set = new ValueSet();
        set.setIdentifier(valueSet.getIdentifier());
        return getFhirContext().newJsonParser().encodeResourceToString(set);
    }

    private String getResContactString(final ValueSet valueSet) {
        ValueSet set = new ValueSet();
        set.setContact(valueSet.getContact());
        return getFhirContext().newJsonParser().encodeResourceToString(set);
    }

    private String getResJurisdictionString(final ValueSet valueSet) {
        ValueSet set = new ValueSet();
        set.setJurisdiction(valueSet.getJurisdiction());
        return getFhirContext().newJsonParser().encodeResourceToString(set);
    }

    private String getResIdentifierString(final ConceptMap conceptMap) {
        ConceptMap map = new ConceptMap();
        map.setIdentifier(conceptMap.getIdentifier());
        return getFhirContext().newJsonParser().encodeResourceToString(map);
    }

    private String getResContactString(final ConceptMap conceptMap) {
        ConceptMap map = new ConceptMap();
        map.setContact(conceptMap.getContact());
        return getFhirContext().newJsonParser().encodeResourceToString(map);
    }

    private String getResJurisdictionString(final ConceptMap conceptMap) {
        ConceptMap map = new ConceptMap();
        map.setJurisdiction(conceptMap.getJurisdiction());
        return getFhirContext().newJsonParser().encodeResourceToString(map);
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
                if (resource.getClass().getSimpleName().toLowerCase().equals(ar[3].toLowerCase()) && isValid(ar[4])) {
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

    protected Optional<String> getToken() {
        return oclUser.getAuthtokenTokens().stream()
                .filter(f -> isValid(f.getKey()))
                .map(m -> "Token " + m.getKey())
                .findFirst();
    }

    protected void removeAccessionIdentifier(List<Identifier> identifiers) {
        identifiers.removeIf(i -> i.getType().hasCoding(ACSN_SYSTEM, ACSN));
    }

    protected Source toBaseSource(final MetadataResource resource, final UserProfile user, final String uri) {
        Source source = new Source();
        // mnemonic
        source.setMnemonic(resource.getId());
        // canonical url
        source.setCanonicalUrl(resource.getUrl());
        // created by
        source.setCreatedBy(user);
        // updated by
        source.setUpdatedBy(user);

        // draft or unknown or empty
        source.setIsActive(True);
        source.setIsLatestVersion(True);
        source.setRetired(False);
        source.setReleased(False);
        if (resource.getStatus() != null) {
            // active
            if (PublicationStatus.ACTIVE.toCode().equals(resource.getStatus().toCode())) {
                source.setReleased(True);
                // retired
            } else if (PublicationStatus.RETIRED.toCode().equals(resource.getStatus().toCode())) {
                source.setRetired(True);
                source.setReleased(False);
                source.setIsActive(False);
                source.setIsLatestVersion(False);
            }
        }
        // version
        source.setVersion(resource.getVersion());
        // default locale
        source.setDefaultLocale(isValid(resource.getLanguage()) ? resource.getLanguage() : EN_LOCALE);
        // uri
        source.setUri(toOclUri(uri));
        // name
        String name = isValid(resource.getName()) ? resource.getName() : resource.getId();
        source.setName(name);
        // description
        if (isValid(resource.getDescription()))
            source.setDescription(resource.getDescription());
        // title
        if (isValid(resource.getTitle()))
            source.setFullName(resource.getTitle());
        // publisher
        if (isValid(resource.getPublisher()))
            source.setPublisher(resource.getPublisher());
        // revision date
        if (resource.getDate() != null)
            source.setRevisionDate(resource.getDate());
        // extras
        source.setExtras(EMPTY_JSON);
        if (resource instanceof CodeSystem) {
            CodeSystem codeSystem = (CodeSystem) resource;
            // content type
            if (codeSystem.getContent() != null && !codeSystem.getContent().toCode().equals(CodeSystem.CodeSystemContentMode.NULL.toCode()))
                source.setContentType(codeSystem.getContent().toCode());
            // copyright
            if (isValid(codeSystem.getCopyright()))
                source.setCopyright(codeSystem.getCopyright());
            // purpose
            if (isValid(codeSystem.getPurpose()))
                source.setPurpose(codeSystem.getPurpose());
        }
        if (resource instanceof ConceptMap) {
            ConceptMap conceptMap = (ConceptMap) resource;
            // copyright
            if (isValid(conceptMap.getCopyright()))
                source.setCopyright(conceptMap.getCopyright());
            // purpose
            if (isValid(conceptMap.getPurpose()))
                source.setPurpose(conceptMap.getPurpose());
        }
        return source;
    }

    protected void addParent(final Source source, final BaseOclEntity owner) {
        if (owner instanceof Organization) {
            Organization organization = (Organization) owner;
            source.setOrganization(organization);
            source.setPublicAccess(organization.getPublicAccess());
        } else if (owner instanceof UserProfile){
            source.setUserId((UserProfile) owner);
        }
    }

    protected String getVersionLessSourceUri(Source source) {
        String value = source.getUri().substring(0, source.getUri().lastIndexOf(FS));
        return value.substring(0, value.lastIndexOf(FS)) + FS;
    }

}

