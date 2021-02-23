package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openconceptlab.fhir.base.OclFhirTest;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.openconceptlab.fhir.provider.OclCapabilityStatementProvider.*;

public class TestOclCapabilityStatementProvider extends OclFhirTest {

    @Before
    public void setUpBefore() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCapabilityStatementProvider() {
        OclCapabilityStatementProvider provider = Mockito.spy(new OclCapabilityStatementProvider());
        doReturn(new CapabilityStatement()).when(provider).getSuperServerConformance(any(HttpServletRequest.class), any(RequestDetails.class));
        CapabilityStatement statement = provider.getServerConformance(servletRequest, requestDetails);
        assertEquals(OPEN_CONCEPT_LAB, statement.getPublisher());
        assertEquals(OPEN_CONCEPT_LAB_FHIR_CAPABILITY_STATEMENT, statement.getTitle());
        assertEquals(OPEN_CONCEPT_LAB_FHIR_API, statement.getImplementation().getDescription());
        assertEquals(Enumerations.FHIRVersion._4_0_1, statement.getFhirVersion());

        assertEquals(CodeSystem.class.getSimpleName(), statement.getRest().get(0).getResourceFirstRep().getType());
        assertEquals(CODE_SYSTEM_PROFILE, statement.getRest().get(0).getResourceFirstRep().getProfile());
        assertEquals("lookup", statement.getRest().get(0).getOperation().get(0).getName());
        assertEquals("validate-code", statement.getRest().get(0).getOperation().get(1).getName());

        assertEquals(ValueSet.class.getSimpleName(), statement.getRest().get(1).getResourceFirstRep().getType());
        assertEquals(VALUESET_PROFILE, statement.getRest().get(1).getResourceFirstRep().getProfile());
        assertEquals("validate-code", statement.getRest().get(1).getOperation().get(0).getName());
        assertEquals("expand", statement.getRest().get(1).getOperation().get(1).getName());
    }

}
