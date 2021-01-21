package org.openconceptlab.fhir;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirAuthorizationInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirLoggingInterceptor;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ConceptMapResourceProvider;
import org.openconceptlab.fhir.provider.OclCapabilityStatementProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The OclFhirRestfulServer.
 * @author harpatel1
 */
@Component
@WebServlet(urlPatterns = "/fhir/*", loadOnStartup = 1)
public class OclFhirRestfulServer extends RestfulServer {

	private CodeSystemResourceProvider codeSystemResourceProvider;
	private ValueSetResourceProvider valueSetResourceProvider;
	private ConceptMapResourceProvider conceptMapResourceProvider;
	private OclCapabilityStatementProvider oclCapabilityStatementProvider;
	private OclFhirAuthorizationInterceptor oclFhirAuthorizationInterceptor;
	private OclFhirLoggingInterceptor oclFhirLoggingInterceptor;

	@Autowired
	public OclFhirRestfulServer(CodeSystemResourceProvider codeSystemResourceProvider,
								ValueSetResourceProvider valueSetResourceProvider,
								ConceptMapResourceProvider conceptMapResourceProvider,
								OclCapabilityStatementProvider oclCapabilityStatementProvider,
								OclFhirAuthorizationInterceptor oclFhirAuthorizationInterceptor,
								OclFhirLoggingInterceptor oclFhirLoggingInterceptor) {
		this.codeSystemResourceProvider = codeSystemResourceProvider;
		this.valueSetResourceProvider = valueSetResourceProvider;
		this.conceptMapResourceProvider = conceptMapResourceProvider;
		this.oclCapabilityStatementProvider = oclCapabilityStatementProvider;
		this.oclFhirAuthorizationInterceptor = oclFhirAuthorizationInterceptor;
		this.oclFhirLoggingInterceptor = oclFhirLoggingInterceptor;
	}

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
		registerProvider(conceptMapResourceProvider);

		// Register capability statement provider
		setServerConformanceProvider(oclCapabilityStatementProvider);
		
		// Register interceptors
		registerInterceptor(new ResponseHighlighterInterceptor());
		registerInterceptor(oclFhirAuthorizationInterceptor);
		registerInterceptor(oclFhirLoggingInterceptor);
	}

}
