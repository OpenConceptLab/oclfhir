package org.openconceptlab.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirAuthorizationInterceptor;
import org.openconceptlab.fhir.interceptor.OclFhirLoggingInterceptor;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ConceptMapResourceProvider;
import org.openconceptlab.fhir.provider.OclCapabilityStatementProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * The OclFhirRestfulServer.
 * @author harpatel1
 */
@Component
@WebServlet(urlPatterns = "/fhir/*", loadOnStartup = 1, displayName = "OCL FHIR Server", asyncSupported = true)
public class OclFhirRestfulServer extends RestfulServer {

	@Autowired
	ApplicationProperties properties;

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

//	@Value("${ocl.servlet.baseurl}")
//	private String baseUrl;

	@Override
	protected void initialize() throws ServletException {
		// Create a context for the appropriate version
		setFhirContext(FhirContext.forR4());

		// setServerAddressStrategy(new HardcodedServerAddressStrategy(baseUrl));

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

		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader("x-fhir-starter");
		config.addAllowedHeader("Origin");
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("X-Requested-With");
		config.addAllowedHeader("Content-Type");
		config.addAllowedHeader("Access-Control-Request-Method");
		config.addAllowedHeader("Access-Control-Request-Headers");
		config.addAllowedOrigin("*");

		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));

		// Create the interceptor and register it
		CorsInterceptor interceptor = new CorsInterceptor(config);
		registerInterceptor(interceptor);

	}

	@Override
	public void addHeadersToResponse(HttpServletResponse theHttpResponse) {
		theHttpResponse.addHeader("x-ocl-fhir-version", properties.getOclFhirVersion());
	}
}
