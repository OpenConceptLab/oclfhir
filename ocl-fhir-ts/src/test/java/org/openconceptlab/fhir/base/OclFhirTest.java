package org.openconceptlab.fhir.base;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.OclCapabilityStatementProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.repository.*;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.mockito.Mockito.spy;
import static org.openconceptlab.fhir.util.OclFhirConstants.*;

public class OclFhirTest {

    public static final String AD = "AD";
    public static final String ES = "es";
    public static final String TM = "TM";
    public static final String ALLERGIC_DISORDER = "allergic disorder";
    public static final String TUMOR_DISORDER = "tumor (disorder)";
    public static final String TRASTORNO_ALERGICO = "trastorno alérgico";
    public static final String TUMOR_TRASTORNO = "tumor (trastorno)";

    public static final String VS = "diagnosis-vs";
    public static final String VS_URL = "http://snomed.org/ValueSet/diagnosis-vs";
    public static final String V_11_1 = "v11.1";
    public static final String V_11_2 = "v11.2";

    public static final String CS = "diagnosis-cs";
    public static final String CS_URL = "http://snomed.org/CodeSystem/diagnosis-cs";
    public static final String V_21_1 = "v21.1";
    public static final String V_21_2 = "v21.2";

    public static final String C_54_3 = "C54.3";
    public static final String DISP = "Allergic Disorder";
    public static final String EN = "en";
    public static final String OWNER_VAL = "org:OCL";

    public static final String VEIN_PROCEDURE = "Vein Procedure";
    public static final String LUNG_PROCEDURE = "Lung Procedure";
    public static final String NECK_PROCEDURE = "Neck Procedure";
    public static final String VEIN_PROCEDURE_1 = "Vein Procedure1";
    public static final String VEIN_PROCEDURE_2 = "Vein Procedure2";
    public static final String VEIN_PROCEDURE_3 = "Vein Procedure3";
    public static final String LUNG_PROCEDURE_1 = "Lung Procedure1";
    public static final String NECK_PROCEDURE_1 = "Neck Procedure1";

    public static final String URL_SOURCE_1 = "http://openconceptlab.org/source1";
    public static final String SOURCE_1 = "source1";
    public static final String URI_SOURCE_1 = "/orgs/OCL/sources/source1";
    public static final String SOURCE_1_NAME = "source1 name";
    public static final String SOURCE_1_FULL_NAME = "source1 full name";
    public static final String SOURCE_1_DESCRIPTION = "source1 description";
    public static final String SOURCE_1_COPYRIGHT_TEXT = "source1 copyright text";
    public static final String TEST_SOURCE = "Test Source";
    public static final String EXAMPLE = "example";

    public static final String URL_SOURCE_2 = "http://openconceptlab.org/source2";
    public static final String SOURCE_2 = "source2";
    public static final String URI_SOURCE_2 = "/orgs/OCL/sources/source2";
    public static final String SOURCE_2_NAME = "source2 name";
    public static final String SOURCE_2_FULL_NAME = "source2 full name";
    public static final String SOURCE_2_DESCRIPTION = "source2 description";
    public static final String SOURCE_2_COPYRIGHT_TEXT = "source2 copyright text";
    public static final String TEST = "TEST";
    public static final String V_1_0 = "v1.0";
    public static final String V_2_0 = "v2.0";

    public static final String URL_COLLECTION_1 = "http://openconceptlab.org/collection1";
    public static final String URI_COLLECTION_1 = "/orgs/OCL/collections/collection1";
    public static final String URL_COLLECTION_2 = "http://openconceptlab.org/collection2";
    public static final String URI_COLLECTION_2 = "/orgs/OCL/collections/collection2";
    public static final String test_user = "testuser";

    protected Source source1;
    protected Source source2;
    protected Source source3;

    protected ConceptsSource cs11 = conceptsSource(concept1(), source1);
    protected ConceptsSource cs21 = conceptsSource(concept1(), source2);
    protected ConceptsSource cs22 = conceptsSource(concept2(), source2);
    protected ConceptsSource cs23 = conceptsSource(concept3(), source2);
    protected ConceptsSource cs24 = conceptsSource(concept4(), source2);
    protected ConceptsSource cs31 = conceptsSource(concept1(), source3);
    protected ConceptsSource cs32 = conceptsSource(concept2(), source3);
    protected ConceptsSource cs33 = conceptsSource(concept3(), source3);
    protected ConceptsSource cs34 = conceptsSource(concept4(), source3);

    protected Date date1 = Date.from(LocalDate.of(2020, 12, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    protected Date date2 = Date.from(LocalDate.of(2020, 12, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());

    @Mock
    protected SourceRepository sourceRepository;

    @Mock
    protected CollectionRepository collectionRepository;

    @Mock
    protected ConceptRepository conceptRepository;

    @Mock
    protected ConceptsSourceRepository conceptsSourceRepository;

    @Mock
    protected AuthtokenRepository authtokenRepository;

    @Mock
    protected UserProfilesOrganizationRepository userProfilesOrganizationRepository;

    @Mock
    protected UserProfile oclUser;

    @Mock
    protected OrganizationRepository organizationRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected RequestDetails requestDetails;

    @Mock
    protected HttpServletRequest servletRequest;

    @Mock
    protected DataSource dataSource;

    @Mock
    protected SimpleJdbcInsert insertLocalizedText;

    @Mock
    protected SimpleJdbcInsert insertCollectionReference;

    @Mock
    protected SimpleJdbcInsert insertConcept;

    @Mock
    protected JdbcTemplate jdbcTemplate;

    @Mock
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    protected ResultSet resultSet;

    @Mock
    protected EntityManager entityManager;

    @Spy
    protected OclCapabilityStatementProvider capabilityStatementProvider;

    public void assertTrue(Parameters parameters) {
        Assert.assertTrue(parameters.getParameter("result") != null
                && ((BooleanType) parameters.getParameter("result")).getValue());
    }

    public void assertFalse(Parameters parameters) {
        Assert.assertTrue(parameters.getParameter("result") != null
                && !((BooleanType) parameters.getParameter("result")).getValue());
    }

    public Parameters getParameters(String url, String valueSetVersion, String code, String system, String systemVersion,
                                     String display, String displayLanguage, Coding coding, String owner) {
        Parameters parameters = new Parameters();
        if (isValid(url))
            parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(valueSetVersion))
            parameters.addParameter().setName(VALUESET_VERSION).setValue(new StringType(valueSetVersion));
        if (isValid(code))
            parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(system))
            parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        if (isValid(systemVersion))
            parameters.addParameter().setName(SYSTEM_VERSION).setValue(new StringType(systemVersion));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(new StringType(display));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISP_LANG).setValue(new StringType(displayLanguage));
        if (coding != null)
            parameters.addParameter().setName(CODING).setValue(coding);
        if (isValid(owner))
            parameters.addParameter().setName(OWNER).setValue(new StringType(owner));
        return parameters;
    }

    public boolean isValid(String value) {
        return StringUtils.isNotBlank(value);
    }

    public ValueSetResourceProvider valueSetProvider() {
        OclFhirUtil oclFhirUtil = new OclFhirUtil(sourceRepository, conceptRepository, conceptsSourceRepository, collectionRepository);
        oclFhirUtil.setBaseUrl("http://test.org");
        ValueSetConverter converter = new TestValueSetConverter(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource,
                authtokenRepository, userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository);
        return spy(new ValueSetResourceProvider(null, null, collectionRepository, converter, null, oclFhirUtil));
    }

    class TestCodeSystemConverter extends CodeSystemConverter {

        public TestCodeSystemConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                           UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                           AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                           OrganizationRepository organizationRepository, UserRepository userRepository) {
            super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
                    userProfilesOrganizationRepository, organizationRepository, userRepository, OclFhirTest.this.collectionRepository, null);
            this.jdbcTemplate = OclFhirTest.this.jdbcTemplate;
            this.insertConcept = OclFhirTest.this.insertConcept;
            this.insertLocalizedText = OclFhirTest.this.insertLocalizedText;
        }

        @Override
        public void init() {
            this.jdbcTemplate = OclFhirTest.this.jdbcTemplate;
            this.insertConcept = OclFhirTest.this.insertConcept;
            this.insertLocalizedText = OclFhirTest.this.insertLocalizedText;
        }
    }

    class TestValueSetConverter extends ValueSetConverter {

        public TestValueSetConverter(SourceRepository sourceRepository, ConceptRepository conceptRepository, OclFhirUtil oclFhirUtil,
                                     UserProfile oclUser, ConceptsSourceRepository conceptsSourceRepository, DataSource dataSource,
                                     AuthtokenRepository authtokenRepository, UserProfilesOrganizationRepository userProfilesOrganizationRepository,
                                     OrganizationRepository organizationRepository, UserRepository userRepository, CollectionRepository collectionRepository) {
            super(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository, dataSource, authtokenRepository,
                    userProfilesOrganizationRepository, organizationRepository, userRepository, collectionRepository, null);
            this.jdbcTemplate = OclFhirTest.this.jdbcTemplate;
            this.insertCollectionReference = OclFhirTest.this.insertCollectionReference;
            this.namedParameterJdbcTemplate = OclFhirTest.this.namedParameterJdbcTemplate;
            this.entityManager = OclFhirTest.this.entityManager;
        }

        @Override
        public void initValueSetConverter() {
            this.insertCollectionReference = OclFhirTest.this.insertCollectionReference;
        }

        @Override
        protected Map<Long, String> getValidatedConceptIds(Long sourceId, List<String> conceptIds) {
            Map<Long,String> map = new HashMap<>();
            map.put(1L, "TEST");
            return map;
        }
    }

    public CodeSystemResourceProvider codeSystemProvider() {
        OclFhirUtil oclFhirUtil = new OclFhirUtil(sourceRepository, conceptRepository, conceptsSourceRepository, collectionRepository);
        oclFhirUtil.setBaseUrl("http://test.org/fhir");
        CodeSystemConverter converter = new TestCodeSystemConverter(sourceRepository, conceptRepository, oclFhirUtil,
                oclUser, conceptsSourceRepository, dataSource, authtokenRepository, userProfilesOrganizationRepository,
                organizationRepository, userRepository);
        return new CodeSystemResourceProvider(sourceRepository, converter, null, null, null, oclFhirUtil);
    }

    public UriType newUrl(String url) {
        return new UriType(url);
    }

    public StringType newString(String value) {
        return new StringType(value);
    }

    public CodeType newCode(String code) {
        return new CodeType(code);
    }

    public Collection collection(List<CollectionsReference> references) {
        Collection collection = new Collection();
        collection.setCanonicalUrl(VS_URL);
        collection.setVersion(V_11_1);
        collection.setMnemonic("diagnosis-vs");
        org.openconceptlab.fhir.model.Organization organization = new Organization();
        organization.setMnemonic("OCL");
        collection.setOrganization(organization);
        collection.setCollectionsReferences(references);
        collection.setIsActive(true);
        collection.setReleased(true);
        collection.setRetired(false);
        return collection;
    }

    public CollectionReference newReference(String expression) {
        CollectionReference reference = new CollectionReference();
        reference.setExpression(expression);
        return reference;
    }

    public List<CollectionsReference> newReferences(String... expression) {
        List<CollectionsReference> references = new ArrayList<>();
        for (String e: expression) {
            CollectionsReference reference = new CollectionsReference();
            reference.setCollectionReference(newReference(e));
            references.add(reference);
        }
        return references;
    }

    public Source source(Long id, String version, Concept... concepts) {
        Source source = new Source();
        source.setId(id);
        source.setCanonicalUrl(CS_URL);
        source.setMnemonic(CS);
        source.setVersion(version);
        for (Concept concept : concepts) {
            source.getConcepts().add(concept);
            ConceptsSource cs = new ConceptsSource();
            cs.setConcept(concept);
            source.getConceptsSources().add(cs);
        }
        return source;
    }

    public Concept concept(Long id, String code) {
        Concept concept = new Concept();
        concept.setId(id);
        concept.setMnemonic(code);
        return concept;
    }

    public ConceptsName newName( String name, String type, String locale, Boolean preferred) {
        ConceptsName cname = new ConceptsName();
        cname.setLocalizedText(newText(name, type, locale, preferred));
        return cname;
    }

    public LocalizedText newText(String name, String type, String locale, Boolean preferred) {
        LocalizedText text = new LocalizedText();
        text.setName(name);
        text.setType(type);
        text.setLocale(locale);
        text.setLocalePreferred(preferred);
        return text;
    }

    protected ConceptsSource conceptsSource(Concept concept, Source source) {
        ConceptsSource cs = new ConceptsSource();
        cs.setConcept(concept);
        cs.setSource(source);
        return cs;
    }

    protected Concept concept1() {
        return newConcept(1L, "123", AD, newName(ALLERGIC_DISORDER, "", EN, false),
                newName(TRASTORNO_ALERGICO, "", ES, false));
    }

    protected Concept concept2() {
        return newConcept(2L,"123", TM, newName(TUMOR_DISORDER, "", EN, false),
                newName(TUMOR_TRASTORNO, "", ES, false));
    }

    protected Concept concept3() {
        return newConcept(3L, "123", VEIN_PROCEDURE, newName(VEIN_PROCEDURE_1, "", EN, true),
                newName(VEIN_PROCEDURE_2, "", ES, false),
                newName(VEIN_PROCEDURE_3, "", "fr", false));
    }

    protected Concept concept4() {
        return newConcept(4L, "123", LUNG_PROCEDURE, newName(LUNG_PROCEDURE_1, "", EN, true));
    }

    protected Concept concept5() {
        return newConcept(5L, "123", NECK_PROCEDURE, newName(NECK_PROCEDURE_1, "", EN, true));
    }

    protected Concept newConcept(Long id, String version, String code, ConceptsName... names) {
        Concept concept = concept(id, code);
        concept.setVersion(version);
        for (ConceptsName name: names) {
            concept.getConceptsNames().add(name);
        }
        return concept;
    }

    protected List<Concept> concepts(Concept... concept) {
        return new ArrayList<>(Arrays.asList(concept));
    }

    protected void populateSource1(Source source1) {
        source1.setCanonicalUrl(URL_SOURCE_1);
        source1.setMnemonic(SOURCE_1);
        source1.setUri(URI_SOURCE_1);
        source1.setName(SOURCE_1_NAME);
        source1.setFullName(SOURCE_1_FULL_NAME);
        source1.setIsActive(true);
        source1.setDescription(SOURCE_1_DESCRIPTION);
        source1.setContact("[{\"name\": \"Jon Doe 1\", \"telecom\": [{\"use\": \"work\", \"rank\": 1, \"value\": \"jondoe1@gmail.com\", " +
                "\"period\": {\"end\": \"2025-10-29T10:26:15-04:00\", \"start\": \"2020-10-29T10:26:15-04:00\"}, \"system\": \"email\"}]}]");
        source1.setJurisdiction("[{\"coding\": [{\"code\": \"USA\", \"system\": \"http://unstats.un.org/unsd/methods/m49/m49.htm\", " +
                "\"display\": \"United States of America\"}]}]");
        source1.setPublisher(TEST);
        source1.setPurpose(TEST_SOURCE);
        source1.setCopyright(SOURCE_1_COPYRIGHT_TEXT);
        source1.setContentType(EXAMPLE);
        source1.setRevisionDate(date1);
        source1.setCreatedAt(new Timestamp(new Date().getTime()));
    }

    protected void populateSource2(Source source2) {
        source2.setCanonicalUrl(URL_SOURCE_2);
        source2.setMnemonic(SOURCE_2);
        source2.setUri(URI_SOURCE_2);
        source2.setName(SOURCE_2_NAME);
        source2.setFullName(SOURCE_2_FULL_NAME);
        source2.setIsActive(true);
        source2.setDescription(SOURCE_2_DESCRIPTION);
        source2.setContact("[{\"name\": \"Jon Doe 2\", \"telecom\": [{\"use\": \"work\", \"rank\": 1, \"value\": \"jondoe2@gmail.com\", " +
                "\"period\": {\"end\": \"2022-10-29T10:26:15-04:00\", \"start\": \"2021-10-29T10:26:15-04:00\"}, \"system\": \"email\"}]}]");
        source2.setJurisdiction("[{\"coding\": [{\"code\": \"ETH\", \"system\": \"http://unstats.un.org/unsd/methods/m49/m49.htm\", " +
                "\"display\": \"Ethiopia\"}]}]");
        source2.setPublisher("TEST");
        source2.setPurpose(TEST_SOURCE);
        source2.setCopyright(SOURCE_2_COPYRIGHT_TEXT);
        source2.setContentType(EXAMPLE);
        source2.setRevisionDate(date2);
        source1.setCreatedAt(new Timestamp(new Date().getTime()));
    }

    protected Organization newOrganization() {
        Organization organization = new Organization();
        organization.setMnemonic("OCL");
        organization.setId(789L);
        return organization;
    }

    protected UserProfile newUser(String username) {
        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setId(567L);
        AuthtokenToken token = new AuthtokenToken();
        token.setUserProfile(user);
        token.setKey("12345");
        user.setAuthtokenTokens(Collections.singletonList(token));
        return user;
    }

    protected AuthtokenToken newToken(String username) {
        AuthtokenToken token = new AuthtokenToken();
        token.setUserProfile(newUser(username));
        token.setKey("12345");
        return token;
    }

    protected UserProfilesOrganization newUserOrg(String username) {
        UserProfilesOrganization userOrg = new UserProfilesOrganization();
        userOrg.setUserProfile(newUser(username));
        userOrg.setOrganization(newOrganization());
        return userOrg;
    }

    protected KeyHolder newKey() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("id", 45);
        list.add(map);
        return new GeneratedKeyHolder(list);
    }

    protected List<Source> getSources() {
        List<Source> sourceList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Source source = source(123L, V_1_0+i, concept1(), concept2(), concept3());
            populateSource1(source);
            source.setId((long) i);
            source.setMnemonic("source1" + i);
            source.setReleased(true);
            source.setPublisher("OCL");
            sourceList.add(source);
        }
        return sourceList;
    }

}
