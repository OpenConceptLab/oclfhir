package org.openconceptlab.fhir.base;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.mockito.Mock;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.converter.ValueSetConverter;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.model.Organization;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.repository.CollectionRepository;
import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.ConceptsSourceRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.openconceptlab.fhir.util.OclFhirUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    protected Source source1;
    protected Source source2;
    protected Source source3;
    protected ConceptsSource cs11;
    protected ConceptsSource cs21;
    protected ConceptsSource cs22;
    protected ConceptsSource cs23;
    protected ConceptsSource cs24;
    protected ConceptsSource cs31;
    protected ConceptsSource cs32;
    protected ConceptsSource cs33;
    protected ConceptsSource cs34;

    @Mock
    protected SourceRepository sourceRepository;

    @Mock
    protected CollectionRepository collectionRepository;

    @Mock
    protected ConceptRepository conceptRepository;

    @Mock
    protected ConceptsSourceRepository conceptsSourceRepository;

    @Mock
    protected UserProfile oclUser;

    @Mock
    protected RequestDetails requestDetails;

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
        OclFhirUtil oclFhirUtil = new OclFhirUtil(sourceRepository, conceptRepository, conceptsSourceRepository);
        ValueSetConverter converter = new ValueSetConverter(oclFhirUtil, conceptsSourceRepository, conceptRepository, sourceRepository);
        ValueSetResourceProvider provider = spy(new ValueSetResourceProvider(collectionRepository, converter, oclFhirUtil));
        return provider;
    }

    public CodeSystemResourceProvider codeSystemProvider() {
        OclFhirUtil oclFhirUtil = new OclFhirUtil(sourceRepository, conceptRepository, conceptsSourceRepository);
        CodeSystemConverter converter = new CodeSystemConverter(sourceRepository, conceptRepository, oclFhirUtil, oclUser, conceptsSourceRepository);
        CodeSystemResourceProvider provider = new CodeSystemResourceProvider(sourceRepository, converter, oclFhirUtil);
        return provider;
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
        source.setMnemonic("diagnosis-cs");
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

    public void setActive(Concept concept) {
        concept.setIsActive(true);
    }

    public void setReleased(Source source) {
        source.setReleased(true);
    }

    public void setRetired(Source source) {
        source.setRetired(true);
    }

    public void setReleased(Collection collection) {
        collection.setReleased(true);
    }

    public void setRetired(Collection collection) {
        collection.setRetired(true);
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

}
