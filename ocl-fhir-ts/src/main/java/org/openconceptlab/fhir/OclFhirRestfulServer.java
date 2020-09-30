package org.openconceptlab.fhir;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ServeMediaResourceRawInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.openconceptlab.fhir.interceptor.OclFhirAuthorizationInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirLoggingInterceptor;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.OclCapabilityStatementProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	private ValueSetResourceProvider valueSetResourceProvider;

	@Autowired
	private OclCapabilityStatementProvider oclCapabilityStatementProvider;

	@Autowired
	OclFhirAuthorizationInterceptor oclFhirAuthorizationInterceptor;

	@Autowired
	OclFhirLoggingInterceptor oclFhirLoggingInterceptor;

	@Value("${ocl.servlet.baseurl}")
	private String baseUrl;

	@Override
	protected void initialize() throws ServletException {
		// Create a context for the appropriate version
		setFhirContext(FhirContext.forR4());

		setServerAddressStrategy(new HardcodedServerAddressStrategy(baseUrl));

		// Register resource providers
		registerProvider(codeSystemResourceProvider);
		registerProvider(valueSetResourceProvider);

		// Register capability statement provider
		setServerConformanceProvider(oclCapabilityStatementProvider);
		
		// Register interceptors
		registerInterceptor(new ResponseHighlighterInterceptor());
		registerInterceptor(oclFhirAuthorizationInterceptor);
		registerInterceptor(oclFhirLoggingInterceptor);
	}

}
