package org.openconceptlab.fhir.provider;

import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.openconceptlab.fhir.base.OclFhirTest;
import org.openconceptlab.fhir.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestValueSetResourceProvider extends OclFhirTest {

    @Before
    public void setUpBefore() {
        MockitoAnnotations.initMocks(this);
        source1 = source(123L, "v1.0", concept1(), concept2(), concept3());
        source2 = source(234L, "v2.0", concept1(), concept2(), concept3(), concept4());
        source3 = source(345L, "v3.0", concept1(), concept2(), concept3(), concept4());
        cs11 = conceptsSource(concept1(), source1);
        cs21 = conceptsSource(concept1(), source2);
        cs22 = conceptsSource(concept2(), source2);
        cs23 = conceptsSource(concept3(), source2);
        cs24 = conceptsSource(concept4(), source2);
        cs31 = conceptsSource(concept1(), source3);
        cs32 = conceptsSource(concept2(), source3);
        cs33 = conceptsSource(concept3(), source3);
        cs34 = conceptsSource(concept4(), source3);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testValidateCode_url_null() {
        validateCode(null, V_11_1, CS_URL, V_21_1, AD, null, null, null, OWNER_VAL);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testValidateCode_code_null() {
        validateCode(VS_URL, V_11_1, CS_URL, V_21_1, null, null, null, null, OWNER_VAL);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testValidateCode_system_null() {
        validateCode(VS_URL, V_11_1, null, V_21_1, AD, null, null, null, OWNER_VAL);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testValidateCode_code_coding() {
        validateCode(VS_URL, V_11_1, CS_URL, null, AD, null, null,
                new Coding("ABC", "ABC", "ABC"), OWNER_VAL);
    }

    @Test
    public void testValidateCode_systemversion_unmatch() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_1, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_systemversion_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, null, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_systemversion_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, EN, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, ES, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_unmatch1() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, ES, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_unmatch2() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, EN, null, OWNER_VAL));
    }

    @Test
    public void testExpand() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24), null, 0, 50, "");
        assertEquals(5, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v2.0", AD, ALLERGIC_DISORDER);
        assertContains(vs, 1, CS_URL, "v2.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 2, CS_URL, "v2.0", TM, TUMOR_DISORDER);
        assertContains(vs, 3, CS_URL, "v2.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
        assertContains(vs, 4, CS_URL, "v1.0", AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testExpand_partial() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs24, cs22, cs23), null, 2, 50, "");
        assertEquals(3, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v2.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 1, CS_URL, "v2.0", TM, TUMOR_DISORDER);
        assertContains(vs, 2, CS_URL, "v2.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
    }

    @Test
    public void testExpand_count_0() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Collections.singletonList(cs21), null,0, 0, "");
        assertEquals(5, vs.getExpansion().getTotal());
        assertEquals(0, vs.getExpansion().getContains().size());
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testExpand_count_negative() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        runExpand(references, Collections.singletonList(cs11), Collections.singletonList(cs21), null, 0, -2, "");
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testExpand_missing_url() {
        ValueSetResourceProvider provider = valueSetProvider();
        provider.valueSetExpand(null, null, new IntegerType(0), new IntegerType(10),
                null, null, null, null, null, null, null, newString(OWNER_VAL));
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testExpand_unknown_systemversion() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                null, 0, 50, CS_URL+"|unk");
    }

    @Test
    public void testExpand_known_systemversion() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                Arrays.asList(cs31, cs32, cs33, cs34), 0, 50, CS_URL + "|v3.0");
        assertEquals(4, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v3.0", AD, ALLERGIC_DISORDER);
        assertContains(vs, 1, CS_URL, "v3.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 2, CS_URL, "v3.0", TM, TUMOR_DISORDER);
        assertContains(vs, 3, CS_URL, "v3.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
    }

    @Test
    public void testExpand_known_systemversion_no_head_in_expression() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                Arrays.asList(cs31, cs32, cs33, cs34), 0, 50, CS_URL + "|v3.0");
        assertEquals(0, vs.getExpansion().getContains().size());
    }

    private void assertContains(ValueSet valueSet, int index, String system, String version, String code, String display) {
        assertEquals(system, valueSet.getExpansion().getContains().get(index).getSystem());
        assertEquals(version, valueSet.getExpansion().getContains().get(index).getVersion());
        assertEquals(code, valueSet.getExpansion().getContains().get(index).getCode());
        assertEquals(display, valueSet.getExpansion().getContains().get(index).getDisplay());
    }

    public ValueSet runExpand(List<CollectionsReference> references, List<ConceptsSource> list1, List<ConceptsSource> list2,
                              List<ConceptsSource> list3, Integer offset, Integer count, String systemVersion) {
        // set up
        ValueSetResourceProvider provider = valueSetProvider();
        Collection collection = collection(references);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source2).thenReturn(source1);
        OngoingStubbing<List<ConceptsSource>> stub1 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()));
        for (ConceptsSource cs1 : list1) {
            stub1 = stub1.thenReturn(Collections.singletonList(cs1));
        }
        OngoingStubbing<List<ConceptsSource>> stub2 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(234L), anyList()));
        for (ConceptsSource cs2 : list2) {
            stub2 = stub2.thenReturn(Collections.singletonList(cs2));
        }
        if (isValid(systemVersion) & !systemVersion.contains("unk")) {
            when(sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(anyString(), anyString(), anyList())).thenReturn(source3);
            OngoingStubbing<List<ConceptsSource>> stub3 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(345L), anyList()));
            if (list3 != null) {
                for (ConceptsSource cs3 : list3) {
                    stub3 = stub3.thenReturn(Collections.singletonList(cs3));
                }
            }
        }
        when(collectionRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyString(), anyList())).thenReturn(collection);

        return provider.valueSetExpand(newUrl(VS_URL), null, new IntegerType(offset), new IntegerType(count),
                null, null, null, null, null, Sets.newHashSet(new CanonicalType(systemVersion)), null, newString(OWNER_VAL));
    }


    public Parameters validateCode(String url, String version, String system, String systemVersion, String code,
                                       String display, String language, Coding coding, String owner) {
        // set up
        ValueSetResourceProvider provider = valueSetProvider();
        Concept concept1 = concept1();
        Concept concept2 = concept2();

        Source source = source(123L, systemVersion, concept1, concept2);

        Collection collection = collection(newReferences(
                "/orgs/OCL/sources/"+CS+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/"+V_21_2+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/"+V_21_1+"/concepts/"+TM+"/123/"
        ));

        ConceptsSource cs1 = conceptsSource(concept1, source);
        ConceptsSource cs2 = conceptsSource(concept2, source);

        // mocks
        if (StringUtils.isNotBlank(version)) {
            when(collectionRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(),
                    anyString(), anyString(), anyList())).thenReturn(collection);
        } else {
            when(collectionRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    anyString(), anyBoolean(), anyString(), anyList())).thenReturn(collection);
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            when(sourceRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(),
                    anyString(), anyString(), anyList())).thenReturn(source);
        } else {
            when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    anyString(), anyBoolean(), anyString(), anyList())).thenReturn(source);
        }

        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList()))
                .thenReturn(Arrays.asList(cs1, cs2));
        when(conceptRepository.findByMnemonic(anyString())).thenReturn(Arrays.asList(concept1, concept2));

        // call to test method
        Parameters output = provider.valueSetValidateCode(newUrl(url), newString(version), newCode(code), newUrl(system), newString(systemVersion),
                newString(display), newCode(language), coding, newString(owner));

        // verify
        if (StringUtils.isNotBlank(version)) {
            verify(collectionRepository, times(1))
                    .findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList());
        } else {
            verify(collectionRepository, times(1))
                    .findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyString(), anyList());
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            verify(sourceRepository, times(1))
                    .findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList());
        } else {
            verify(sourceRepository, times(1))
                    .findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyString(), anyList());
        }
        return output;
    }
}
