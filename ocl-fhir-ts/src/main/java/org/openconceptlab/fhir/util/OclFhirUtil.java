package org.openconceptlab.fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.google.gson.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;

@Component
public class OclFhirUtil {

    public static final String PUBLISHER_REGEX = "^user:.*|^org:.*";
	public static final String ORG_ = "org:";
    public static final String USER_ = "user:";
    public static final String ORG = "org";
    public static final String USER = "user";
    public static final String SEP = ":";
    public static List<String> publicAccess = Arrays.asList("View", "Edit");
    public static final String OWNER = "owner";
    public static final String ID = "id";
    
    @Value("${server.port}")
    private String port;

    private static FhirContext context;

    static {
        context = FhirContext.forR4();
    }

    private String serverBase = "";
    IParser parser = context.newJsonParser();
    public static JsonParser jsonParser = new JsonParser();
    public static Gson gson = new Gson();
    public static final List<String> allowedFilterOperators = Arrays.asList(CodeSystem.FilterOperator.ISA.toCode(),
            CodeSystem.FilterOperator.ISNOTA.toCode(), CodeSystem.FilterOperator.IN.toCode(),
            CodeSystem.FilterOperator.NOTIN.toCode());

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
            //component.setFullUrl(getCompleteUrl(fhirBase, requestPath, r.getId()));
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

    public static OperationOutcome getError(OperationOutcome.IssueType errorType, final String error) {
        OperationOutcome o = new OperationOutcome();
        o.getIssueFirstRep()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(errorType)
                .setDiagnostics(error);
        return o;
    }

    public static OperationOutcome getError(OperationOutcome.IssueType errorType) {
        OperationOutcome o = new OperationOutcome();
        o.getIssueFirstRep()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(errorType);
        return o;
    }

    public static boolean isValid(final String value) {
        return StringUtils.isNotBlank(value);
    }

    public static boolean isValid(final StringType value) {
        return value != null && StringUtils.isNotBlank(value.getValue());
    }
    
    public static JsonObject parseExtras(String extras) {
        return jsonParser.parse(extras).getAsJsonObject();
    }

    public static List<Identifier> getIdentifiers(JsonArray jsonArray) {
        List<Identifier> identifiers = new ArrayList<>();
        if (jsonArray != null && jsonArray.isJsonArray()) {
            for(JsonElement je : jsonArray) {
                JsonObject identifier = je.getAsJsonObject();
                if(identifier.get(SYSTEM) != null && identifier.get(VALUE) != null) {
                    Identifier i = new Identifier();
                    i.setSystem(identifier.get(SYSTEM).getAsString());
                    i.setValue(identifier.get(VALUE).getAsString());
                    JsonElement use = identifier.get(USE);
                    if(use != null && Identifier.IdentifierUse.fromCode(use.getAsString()) != null)
                        i.setUse(Identifier.IdentifierUse.fromCode(use.getAsString()));
                    identifiers.add(i);
                }
            }
        }
        return identifiers;
    }

    public static boolean isValidElement(JsonElement element) {
        return element != null && element.getAsString() != null;
    }

    public static void validatePublisher(String publisher) {
        if (!isValidPublisher(publisher)) {
            throw new InvalidRequestException("", getError(OperationOutcome.IssueType.INVALID,
                    String.format("Invalid publisher '%s' provided. Correct format is 'user:<username>' or 'org:<organizationId>'", publisher)));
        }
    }

    public static boolean isValidPublisher(final String publisher) {
        return isValid(publisher)
                && publisher.matches(PUBLISHER_REGEX)
                && publisher.split(SEP).length >= 2;
    }

    public static String getOwnerType(String owner) {
        return owner.split(SEP)[0];
    }

    public static String getOwner(String owner) {
        String[] arr = owner.split(SEP);
        return String.join(SEP, ArrayUtils.subarray(arr, 1, arr.length));
    }

    public static <T extends MetadataResource> String toFhirString(final T resource) {
        String fhirString = getFhirContext().newJsonParser().encodeResourceToString(resource);
        JsonObject object = new JsonObject();
        object.add("__fhir", jsonParser.parse(fhirString));
        return gson.toJson(object);
    }

}
