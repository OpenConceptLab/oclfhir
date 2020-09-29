package org.openconceptlab.fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

@Component
public class OclFhirUtil {

    @Value("${server.port}")
    private String port;

    private static FhirContext context = FhirContext.forR4();
    private String serverBase = "";
    IParser parser = context.newJsonParser();

    @PostConstruct
    private void init() {
        serverBase = String.format("http://localhost:%s/fhir",port);
    }

    public static FhirContext getFhirContext() {
        return context;
    }

    public static <T extends Resource> Bundle getBundle(List<T> resource, String fhirBase, String requestPath) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resource.size());
        resource.stream().forEach(r -> {
            Bundle.BundleEntryComponent component = new Bundle.BundleEntryComponent();
            component.setResource(r);
            component.setFullUrl(getCompleteUrl(fhirBase, requestPath, r.getId()));
            bundle.addEntry(component);
        });
        return bundle;
    }

    private static String getCompleteUrl(final String fhirBase, final String requestPath, final String id) {
        return fhirBase + "/" + Paths.get(requestPath, id).toString();
    }

    public IGenericClient getClient() {
        return context.newRestfulGenericClient(serverBase);
    }

    public String getResource(Resource resource) {
        return parser.encodeResourceToString(resource);
    }

    public OperationOutcome getNotFoundOutcome(IdType id) {
        OperationOutcome o = new OperationOutcome();
        o.getIssueFirstRep()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(OperationOutcome.IssueType.NOTFOUND)
                .setDiagnostics("Resource " + id + " does not exist.");
        return o;
    }
}
