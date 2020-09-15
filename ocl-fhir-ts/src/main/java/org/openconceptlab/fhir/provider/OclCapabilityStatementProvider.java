package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * The OclCapabilityStatementProvider.
 * @author hp11
 */
@Component
public class OclCapabilityStatementProvider extends ServerCapabilityStatementProvider {

    @Override
    public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
        CapabilityStatement capabilityStatement =  super.getServerConformance(theRequest, theRequestDetails);
        capabilityStatement.setPublisher("Open Concept Lab");
        return capabilityStatement;
    }
}
