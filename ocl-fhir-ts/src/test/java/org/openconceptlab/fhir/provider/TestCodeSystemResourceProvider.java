package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openconceptlab.fhir.base.OclFhirTest;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Collection;
import org.springframework.data.domain.PageRequest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class TestCodeSystemResourceProvider extends OclFhirTest {

    @Before
    public void setUpBefore() {
        MockitoAnnotations.initMocks(this);
        source1 = source(123L, V_1_0, concept1(), concept2(), concept3());
        source2 = source(234L, "v2.0", concept1(), concept2(), concept3(), concept4());
        source3 = source(345L, "v3.0", concept1(), concept2(), concept3(), concept4());
        populateSource1(source1);
        populateSource2(source2);
    }

    @After
    public void after() {
        source1 = null;
        source2 = null;
        source3 = null;
        cs11 = null;
        cs21 = null;
        cs22 = null;
        cs23 = null;
        cs24 = null;
        cs31 = null;
        cs32 = null;
        cs33 = null;
        cs34 = null;
    }

    private static final Validator ACCESSOR_VALIDATOR = ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new SetterTester())
            .build();

    public static void validateAccessors(final Class<?> clazz) {
        ACCESSOR_VALIDATOR.validate(PojoClassFactory.getPojoClass(clazz));
    }

    @Test
    public void testSearchCodeSystem_1_return_1() {
        source1.setReleased(true);
        source2.setReleased(false);
        source3.setReleased(false);
        when(sourceRepository.findAllMostRecentReleased(anyList())).thenReturn(Arrays.asList(source1));
        when(conceptRepository.findConceptCountInSource(anyLong())).thenReturn(1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(1, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
    }

    @Test
    public void testSearchCodeSystem_2_return_2() {
        source1.setReleased(true);
        source2.setReleased(true);
        source3.setReleased(false);
        when(sourceRepository.findAllMostRecentReleased(anyList())).thenReturn(Arrays.asList(source1, source2));
        when(conceptRepository.findConceptCountInSource(anyLong())).thenReturn(1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(2, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertEquals(0, ((CodeSystem) bundle.getEntry().get(1).getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(1).getResource(), URL_SOURCE_2, SOURCE_2_NAME, SOURCE_2_FULL_NAME,
                "Jon Doe 2", "jondoe2@gmail.com", "ETH", TEST_SOURCE, SOURCE_2_COPYRIGHT_TEXT, EXAMPLE);
    }

    @Test
    public void testSearchCodeSystem_contentType_empty() {
        source1.setIsLatestVersion(true);
        source1.setContentType("");
        when(sourceRepository.findAllMostRecentReleased(anyList())).thenReturn(Arrays.asList(source1));
        when(conceptRepository.findConceptCountInSource(anyLong())).thenReturn(1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(1, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, "?");
    }

    @Test
    public void testSearchCodeSystem_contentType_external() {
        source1.setIsLatestVersion(true);
        source1.setContentType("TEST");
        when(sourceRepository.findAllMostRecentReleased(anyList())).thenReturn(Collections.singletonList(source1));
        when(conceptRepository.findConceptCountInSource(anyLong())).thenReturn(1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(1, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, "?");
    }

    @Test
    public void testSearchCodeSystem_contentType_null() {
        source1.setIsLatestVersion(true);
        source1.setContentType(null);
        when(sourceRepository.findAllMostRecentReleased(anyList())).thenReturn(Collections.singletonList(source1));
        when(conceptRepository.findConceptCountInSource(anyLong())).thenReturn(1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(1, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, "?");
    }

    @Test
    public void testSearchCodeSystem_return_empty() {
        source1.setIsLatestVersion(false);
        source2.setIsLatestVersion(false);
        source3.setIsLatestVersion(false);
        when(sourceRepository.findByPublicAccessIn(anyList())).thenReturn(Arrays.asList(source1, source2, source3));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystem_head_return_empty() {
        source1.setIsLatestVersion(true);
        source1.setVersion("HEAD");
        source2.setIsLatestVersion(false);
        source3.setIsLatestVersion(false);
        when(sourceRepository.findByPublicAccessIn(anyList())).thenReturn(Arrays.asList(source1, source2, source3));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystems(requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystemByUrl_version_empty_return_most_recent() {
        source1.setDefaultLocale(EN);
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList()))
                .thenReturn(source1);
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByUrl(newString(URL_SOURCE_1), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertEquals(1, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        CodeSystem codeSystem = (CodeSystem) bundle.getEntryFirstRep().getResource();
        Assert.assertFalse(codeSystem.getConcept().isEmpty());
    }

    @Test
    public void testSearchCodeSystemByUrl_valid_version() {
        source1.setDefaultLocale(EN);
        source1.setVersion(V_1_0);
        when(sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(anyString(), anyString(), anyList()))
                .thenReturn(source1);
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByUrl(newString(URL_SOURCE_1), newString(V_1_0), null, requestDetails);
        assertEquals(1, bundle.getTotal());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertEquals(1, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        CodeSystem codeSystem = (CodeSystem) bundle.getEntryFirstRep().getResource();
        Assert.assertFalse(codeSystem.getConcept().isEmpty());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testSearchCodeSystemByUrl_not_found() {
        source1.setDefaultLocale(EN);
        source1.setVersion(V_1_0);
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.searchCodeSystemByUrl(newString(URL_SOURCE_1), newString(V_1_0), null, requestDetails);
    }

    @Test
    public void testSearchCodeSystemByUrl_version_head_return_empty() {
        source1.setDefaultLocale(EN);
        source1.setVersion("HEAD");
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList()))
                .thenReturn(source1);
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByUrl(newString(URL_SOURCE_1), null, null, requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystemByUrl_version_all_return_all_no_concept() {
        source1.setDefaultLocale(EN);
        source1.setVersion(V_1_0);
        Source source1v2 = source(123L, "v2.0", concept1(), concept2(), concept3());
        source1v2.setCanonicalUrl(URL_SOURCE_1);
        source1v2.setDefaultLocale(EN);

        when(sourceRepository.findByCanonicalUrlAndPublicAccessIn(anyString(), anyList()))
                .thenReturn(Arrays.asList(source1, source1v2));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByUrl(newString(URL_SOURCE_1), newString("*"), null, requestDetails);
        assertEquals(2, bundle.getTotal());
        assertEquals(URL_SOURCE_1, ((CodeSystem)bundle.getEntry().get(0).getResource()).getUrl());
        assertEquals("v2.0", ((CodeSystem)bundle.getEntry().get(0).getResource()).getVersion());
        assertEquals(URL_SOURCE_1, ((CodeSystem)bundle.getEntry().get(1).getResource()).getUrl());
        assertEquals(V_1_0, ((CodeSystem)bundle.getEntry().get(1).getResource()).getVersion());
    }

    @Test
    public void testSearchCodeSystemByOwner() {
        source1.setReleased(true);
        source2.setReleased(true);
        when(sourceRepository.findByOrganizationMnemonicAndPublicAccessIn(anyString(), anyList())).thenReturn(Arrays.asList(source1, source2));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwner(newString("org:OCL"), requestDetails);
        assertEquals(2, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertEquals(0, ((CodeSystem) bundle.getEntry().get(1).getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(1).getResource(), URL_SOURCE_2, SOURCE_2_NAME, SOURCE_2_FULL_NAME,
                "Jon Doe 2", "jondoe2@gmail.com", "ETH", TEST_SOURCE, SOURCE_2_COPYRIGHT_TEXT, EXAMPLE);
    }

    @Test
    public void testSearchCodeSystemByOwner_user() {
        source1.setReleased(true);
        source2.setReleased(true);
        when(sourceRepository.findByUserIdUsernameAndPublicAccessIn(anyString(), anyList())).thenReturn(Arrays.asList(source1, source2));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwner(newString("user:test"), requestDetails);
        assertEquals(2, bundle.getTotal());
        assertEquals(0, ((CodeSystem) bundle.getEntryFirstRep().getResource()).getConcept().size());
        assertEquals(0, ((CodeSystem) bundle.getEntry().get(1).getResource()).getConcept().size());
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(0).getResource(), URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertBaseCodeSystem((CodeSystem) bundle.getEntry().get(1).getResource(), URL_SOURCE_2, SOURCE_2_NAME, SOURCE_2_FULL_NAME,
                "Jon Doe 2", "jondoe2@gmail.com", "ETH", TEST_SOURCE, SOURCE_2_COPYRIGHT_TEXT, EXAMPLE);
    }

    @Test
    public void testSearchCodeSystemByOwner_owner_empty() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwner(newString(""), requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystemByOwner_owner_null() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwner(null, requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testSearchCodeSystemByOwnerAndId_not_found() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwnerAndId(newString("org:OCL"), newString("123"), null, null, requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystemByOwnerAndId_version_empty_return_most_recent() {
        source1.setIsLatestVersion(true);
        when(sourceRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList(), anyString())).thenReturn(source1);
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwnerAndId(newString("org:OCL"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        CodeSystem codeSystem = (CodeSystem) bundle.getEntry().get(0).getResource();
        assertEquals(1, codeSystem.getConcept().size());
        assertBaseCodeSystem(codeSystem, URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertEquals(1, codeSystem.getConcept().size());
    }

    @Test
    public void testSearchCodeSystemByOwnerAndId_version_head_return_empty() {
        source1.setIsLatestVersion(true);
        source1.setVersion("HEAD");
        when(sourceRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList(), anyString())).thenReturn(source1);
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwnerAndId(newString("org:OCL"), newString("123"), null, null, requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchCodeSystemByOwnerAndId_version_all_return_all_no_concept() {
        source1.setIsLatestVersion(true);
        when(sourceRepository.findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(
                anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(source1));
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwnerAndId(newString("org:OCL"), newString("123"), newString("*"), null, requestDetails);
        assertEquals(1, bundle.getTotal());
        CodeSystem codeSystem = (CodeSystem) bundle.getEntry().get(0).getResource();
        assertBaseCodeSystem(codeSystem, URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertEquals(0, codeSystem.getConcept().size());
    }

    @Test
    public void testSearchCodeSystemByOwnerAndId_version_all_return_all_no_concept_user() {
        source1.setIsLatestVersion(true);
        when(sourceRepository.findByMnemonicAndUserIdUsernameAndPublicAccessIn(
                anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(source1));
        when(conceptRepository.findConcepts(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(cs11.getConcept()));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Bundle bundle = provider.searchCodeSystemByOwnerAndId(newString("user:test"), newString("123"), newString("*"), null, requestDetails);
        assertEquals(1, bundle.getTotal());
        CodeSystem codeSystem = (CodeSystem) bundle.getEntry().get(0).getResource();
        assertBaseCodeSystem(codeSystem, URL_SOURCE_1, SOURCE_1_NAME, SOURCE_1_FULL_NAME,
                "Jon Doe 1", "jondoe1@gmail.com", "USA", TEST_SOURCE, SOURCE_1_COPYRIGHT_TEXT, EXAMPLE);
        assertEquals(0, codeSystem.getConcept().size());
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemLookup_code_null() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemLookUp(null, newUrl(URL_SOURCE_1), null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemLookup_code_empty() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemLookUp(newCode(""), newUrl(URL_SOURCE_1), null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemLookup_url_null() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemLookUp(newCode(AD), null, null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemLookup_url_empty() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemLookUp(newCode(AD), newUrl(""), null, null, null);
    }

    @Test
    public void testCodeSystemLookup() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemLookUp(newCode(AD), newUrl(URL_SOURCE_1), null, null, null);
        assertEquals(SOURCE_1_NAME, parameters.getParameter("name").toString());
        assertEquals(V_1_0, parameters.getParameter("version").toString());
        assertEquals(ALLERGIC_DISORDER, parameters.getParameter("display").toString());
    }

    @Test
    public void testCodeSystemLookup_displayLanguage() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemLookUp(newCode(AD), newUrl(URL_SOURCE_1), null, newCode(ES), null);
        assertEquals(SOURCE_1_NAME, parameters.getParameter("name").toString());
        assertEquals(V_1_0, parameters.getParameter("version").toString());
        assertEquals(TRASTORNO_ALERGICO, parameters.getParameter("display").toString());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testCodeSystemLookup_invalid_code() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemLookUp(newCode("BLOB"), newUrl(URL_SOURCE_1), null, null, null);
    }

    @Test
    public void testCodeSystemLookup_displayLanguage_not_exist() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemLookUp(newCode(AD), newUrl(URL_SOURCE_1), null, newCode("fr"), null);
        assertEquals(SOURCE_1_NAME, parameters.getParameter("name").toString());
        assertEquals(V_1_0, parameters.getParameter("version").toString());
        assertNull(parameters.getParameter("display"));
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemValidateCode_url_null() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemValidateCode(null, newCode(AD), null, null, null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemValidateCode_url_empty() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemValidateCode(newUrl(""), newCode(AD), null, null, null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemValidateCode_code_null() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), null, null, null, null, null, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void testCodeSystemValidateCode_code_empty() {
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(""), null, null, null, null, null);
    }

    @Test
    public void testCodeSystemValidateCode() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(AD), null, null, null, null, null);
        assertTrue(parameters);
    }

    @Test
    public void testCodeSystemValidateCode_display_valid() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(AD), null, newString(ALLERGIC_DISORDER), null, null, null);
        assertTrue(parameters);
    }

    @Test
    public void testCodeSystemValidateCode_display_invalid() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(AD), null, newString("ABC"), null, null, null);
        assertFalse(parameters);
    }

    @Test
    public void testCodeSystemValidateCode_displayLanguage_valid() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Parameters parameters = provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(AD), null, newString(ALLERGIC_DISORDER), newCode(EN), null, null);
        assertTrue(parameters);
    }

    @Test
    public void testCodeSystemValidateCode_displayLanguage_invalid() {
        when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        provider.getResourceType();
        Parameters parameters = provider.codeSystemValidateCode(newUrl(URL_SOURCE_1), newCode(AD), null, newString(ALLERGIC_DISORDER), newCode(ES), null, null);
        assertFalse(parameters);
    }

    @Test
    public void testCodeSystemValidateCode_coding() {
        when(sourceRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(
                anyString(), anyString(), anyString(), anyList())).thenReturn(source1);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()))
                .thenReturn(Collections.singletonList(cs11));
        CodeSystemResourceProvider provider = codeSystemProvider();
        Coding coding = new Coding();
        coding.setSystem(URI_SOURCE_1);
        coding.setCode(AD);
        coding.setDisplay(ALLERGIC_DISORDER);
        coding.setVersion(V_1_0);
        Parameters parameters = provider.codeSystemValidateCode(null, null, null, null, null, coding, newString("user:test"));
        assertTrue(parameters);
    }

    @Test
    public void testDTO() {
        validateAccessors(AuthGroup.class);
        validateAccessors(AuthGroupPermission.class);
        validateAccessors(AuthPermission.class);
        validateAccessors(AuthtokenToken.class);
        validateAccessors(BaseOclEntity.class);
        validateAccessors(Collection.class);
        validateAccessors(CollectionReference.class);
        validateAccessors(CollectionsConcept.class);
        validateAccessors(CollectionsMapping.class);
        validateAccessors(CollectionsConcept.class);
        validateAccessors(Concept.class);
        validateAccessors(ConceptsDescription.class);
        validateAccessors(ConceptsName.class);
        validateAccessors(ConceptsSource.class);
        validateAccessors(DjangoContentType.class);
        validateAccessors(LocalizedText.class);
        validateAccessors(Mapping.class);
        validateAccessors(MappingsSource.class);
        validateAccessors(Organization.class);
        validateAccessors(Source.class);
        validateAccessors(UserProfile.class);
        validateAccessors(UserProfilesGroup.class);
        validateAccessors(UserProfilesOrganization.class);
        validateAccessors(UserProfilesUserPermission.class);
    }

    private void assertConcept(CodeSystem.ConceptDefinitionComponent c, String code, String display, String language1, String name1,
                               String language2, String name2, String prop1, String val1, String prop2, String val2,
                               String prop3, String val3) {
        assertEquals(code, c.getCode());
        assertEquals(display, c.getDisplay());
        assertEquals(language1, c.getDesignation().get(0).getLanguage());
        assertEquals(name1, c.getDesignation().get(0).getValue());
        if (c.getDesignation().size() > 1) {
            assertEquals(language2, c.getDesignation().get(1).getLanguage());
            assertEquals(name2, c.getDesignation().get(1).getValue());
        }
        assertEquals(prop1, c.getProperty().get(0).getCode());
        assertEquals(val1, c.getProperty().get(0).getValue().toString());
        assertEquals(prop2, c.getProperty().get(1).getCode());
        assertEquals(val2, c.getProperty().get(1).getValue().toString());
        assertEquals(prop3, c.getProperty().get(2).getCode());
        assertEquals(val3, c.getProperty().get(2).getValueBooleanType().asStringValue());
    }

    private void assertBaseCodeSystem(CodeSystem system, String url, String name, String fullName,
                                      String contactName, String contactEmail, String jurisdictionCode, String purpose,
                                      String copyright, String contentType) {
        assertEquals(url, system.getUrl());
        assertEquals(name, system.getName());
        assertEquals(fullName, system.getTitle());
        assertEquals(contactName, system.getContactFirstRep().getName());
        assertEquals(contactEmail, system.getContactFirstRep().getTelecomFirstRep().getValue());
        assertEquals(jurisdictionCode, system.getJurisdictionFirstRep().getCodingFirstRep().getCode());
        assertEquals(purpose, system.getPurpose());
        assertEquals(copyright, system.getCopyright());
        assertEquals(contentType, system.getContent().toCode());
    }
}
