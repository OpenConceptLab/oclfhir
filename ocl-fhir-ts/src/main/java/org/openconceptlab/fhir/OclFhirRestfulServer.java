package org.openconceptlab.fhir;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirAuthorizationInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirLoggingInterceptor;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.OclCapabilityStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The OclFhirRestfulServer.
 * @author hp11
 */
@Component
@WebServlet(urlPatterns = "/fhir/*", loadOnStartup = 1)
public class OclFhirRestfulServer extends RestfulServer {

	@Autowired
	private CodeSystemResourceProvider codeSystemResourceProvider;

	@Autowired
	private OclCapabilityStatementProvider oclCapabilityStatementProvider;

	@Autowired
	OclFhirAuthorizationInterceptor oclFhirAuthorizationInterceptor;

	@Autowired
	OclFhirLoggingInterceptor oclFhirLoggingInterceptor;

	@Override
	protected void initialize() throws ServletException {
		// Create a context for the appropriate version
		setFhirContext(FhirContext.forR4());
		
		// Register resource providers
		registerProvider(codeSystemResourceProvider);

		// Register capability statement provider
		setServerConformanceProvider(oclCapabilityStatementProvider);
		
		// Register interceptors
		registerInterceptor(new ResponseHighlighterInterceptor());
		registerInterceptor(oclFhirAuthorizationInterceptor);
		registerInterceptor(oclFhirLoggingInterceptor);
	}
}
