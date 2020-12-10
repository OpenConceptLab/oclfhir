package org.openconceptlab.fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.gson.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.apache.bcel.classfile.Code;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.ConceptsSourceRepository;
import org.openconceptlab.fhir.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;

@Component
public class OclFhirUtil {
    
    @Value("${server.port}")
    private String port;
    private static final Log log = LogFactory.getLog(OclFhirUtil.class);
    private static final FhirContext context;
    private SourceRepository sourceRepository;
    private ConceptRepository conceptRepository;
    private ConceptsSourceRepository conceptsSourceRepository;

    @Autowired
    public OclFhirUtil(SourceRepository sourceRepository, ConceptRepository conceptRepository, ConceptsSourceRepository conceptsSourceRepository) {
        this.sourceRepository = sourceRepository;
        this.conceptRepository = conceptRepository;
        this.conceptsSourceRepository = conceptsSourceRepository;
    }

    public OclFhirUtil(){
        super();
    }

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

    public Source getSourceVersion(String id, String version, List<String> access, String ownerType, String owner) {
        return getSourceVersion(new StringType(id), new StringType(version), access, ownerType, owner);
    }

    public Source getSourceVersion(StringType id, StringType version, List<String> access, String ownerType, String ownerId) {
        final Source source;
        if (!isValid(version)) {
            // get most recent released version
            source = getMostRecentReleasedSourceByOwner(id.getValue(), ownerId, ownerType, access);
        } else {
            // get a given version
            if (ORG.equals(ownerType)) {
                source = sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(
                        id.getValue(), version.getValue(), ownerId, access);
            } else {
                source = sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(
                        id.getValue(), version.getValue(), ownerId, access);
            }
        }
        return source;
    }

    public Source getMostRecentReleasedSourceByOwner(String id, String owner, String ownerType, List<String> access) {
        if (ORG.equals(ownerType)) {
            return sourceRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(
                    id, true, access, owner);
        }
        return sourceRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(
                id, true, access, owner);
    }

    public Source getSourceByOwnerAndUrl(StringType owner, StringType url, StringType version, List<String> access) {
        Source source = null;
        String ownerType = getOwnerType(owner.getValue());
        String value = getOwner(owner.getValue());
        if (!isValid(version)) {
            // get most recent released version
            source = getMostRecentReleasedSourceByOwnerAndUrl(value, ownerType, url, access);
        } else {
            // get a given version
            source = getSourceVersionByOwnerAndUrl(value, ownerType, url, version, access);
        }
        if (source == null)
            throw new ResourceNotFoundException(notFound(CodeSystem.class, url, version));
        return source;
    }

    public Source getMostRecentReleasedSourceByUrl(StringType url, List<String> access) {
        return sourceRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(
                url.getValue(), true, access
        );
    }

    private Source getMostRecentReleasedSourceByOwnerAndUrl(String owner, String ownerType, StringType url, List<String> access) {
        if (ORG.equals(ownerType))
            return sourceRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    url.getValue(), true, owner, access
            );
        return sourceRepository.findFirstByCanonicalUrlAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(
                url.getValue(), true, owner, access
        );
    }

    private Source getSourceVersionByOwnerAndUrl(String owner, String ownerType, StringType url, StringType version, List<String> access) {
        if (ORG.equals(ownerType))
            return sourceRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
        return sourceRepository.findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(url.getValue(), version.getValue(), owner, access);
    }

    public IGenericClient getClient() {
        return context.newRestfulGenericClient(serverBase);
    }

    public String getResourceAsString(Resource resource) {
        return parser.encodeResourceToString(resource);
    }

    public static IBaseResource getResource(String resource) {
        return getFhirContext().newJsonParser().parseResource(resource);
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

    public static boolean isValid(final CodeType code) {
        return code != null && StringUtils.isNotBlank(code.getCode());
    }

    public static boolean isTrue(final BooleanType booleanType) {
        return isValid(booleanType) && booleanType.getValue();
    }

    public static boolean isValid(final PrimitiveType type) {
        return type != null && !type.isEmpty();
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

    public static Optional<Identifier> getIdentifier(String value) {
        if (!isValid(value))
            return Optional.empty();
        Identifier identifier = new Identifier();
        identifier.setSystem("http://fhir.openconceptlab.org");
        identifier.setValue(value.replace("sources", "CodeSystem").replace("collections", "ValueSet"));
        identifier.getType().setText("Accession ID");
        identifier.getType().getCodingFirstRep().setSystem("http://hl7.org/fhir/v2/0203").setCode("ACSN").setDisplay("Accession ID");
        return isValid(value) ? Optional.of(identifier) : Optional.empty();
    }

    public static void addConceptDesignation(Concept concept, CodeSystem.ConceptDefinitionComponent definitionComponent) {
        concept.getConceptsNames().parallelStream().forEach(name -> {
            CodeSystem.ConceptDefinitionDesignationComponent designation = new CodeSystem.ConceptDefinitionDesignationComponent();
            LocalizedText lt = name.getLocalizedText();
            if(lt != null) {
                designation.setLanguage(lt.getLocale());
                if (isValid(lt.getType()))
                    designation.getUse().setCode(lt.getType());
                designation.setValue(lt.getName());
                definitionComponent.getDesignation().add(designation);
            }
        });
    }

    public static void addStatus(MetadataResource resource, boolean active, boolean retired, boolean released) {
        if(active || released) {
            resource.setStatus(Enumerations.PublicationStatus.ACTIVE);
        } else if(retired) {
            resource.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            resource.setStatus(Enumerations.PublicationStatus.UNKNOWN);
        }
    }

    public String getDefinition(List<LocalizedText> definitions, String defaultLocale) {
        if (definitions == null || definitions.isEmpty()) return "";
        if (definitions.size() > 1) {

            // match with dict default locale
            Stream<LocalizedText> dlMatch = definitions.stream().filter(d -> defaultLocale.equals(d.getLocale()));
            Optional<LocalizedText> dlPreferred = getPreferred(definitions.stream().filter(d -> defaultLocale.equals(d.getLocale())));
            if (dlPreferred.isPresent()) return dlPreferred.get().getName();
            Optional<LocalizedText> dlNonPreferred = getNonPreferred(definitions.stream().filter(d -> defaultLocale.equals(d.getLocale())));
            if (dlNonPreferred.isPresent()) return dlNonPreferred.get().getName();

            // match with dict supported locales
            Stream<LocalizedText> slMatch = definitions.stream().filter(d -> defaultLocale.contains(d.getLocale()));
            Optional<LocalizedText> slPreferred = getPreferred(definitions.stream().filter(d -> defaultLocale.contains(d.getLocale())));
            if (slPreferred.isPresent()) return slPreferred.get().getName();
            Optional<LocalizedText> slNonPreferred = getNonPreferred(definitions.stream().filter(d -> defaultLocale.contains(d.getLocale())));
            if (slNonPreferred.isPresent()) return slNonPreferred.get().getName();

            // Any locale preferred
            Optional<LocalizedText> anyPreferred = getPreferred(definitions.stream());
            if (anyPreferred.isPresent()) return anyPreferred.get().getName();
        }
        return definitions.get(0).getName();
    }

    private Optional<LocalizedText> getPreferred(Stream<LocalizedText> texts) {
        return texts.filter(LocalizedText::getLocalePreferred).findFirst();
    }

    private Optional<LocalizedText> getNonPreferred(Stream<LocalizedText> texts) {
        return texts.filter(f -> !f.getLocalePreferred()).findFirst();
    }

    public Optional<Concept> getSourceConcept(Source source, String conceptId, String conceptVersion) {
        List<ConceptsSource> cs = conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(
                source.getId(),
                conceptRepository.findByMnemonic(conceptId).stream().map(Concept::getId).collect(Collectors.toList())
        );
        return cs
                .stream().map(ConceptsSource::getConcept)
                .filter(c -> !isValid(conceptVersion) || conceptVersion.equals(c.getVersion()))
                .findFirst();
    }

    public List<LocalizedText> getNames(Concept concept) {
        return concept.getConceptsNames().stream().map(ConceptsName::getLocalizedText).collect(Collectors.toList());
    }

    public boolean validateDisplay(List<LocalizedText> names, final StringType display, final CodeType displayLanguage) {
        return names.parallelStream()
                .filter(name -> name.getName().equals(display.getValue()))
                .anyMatch(name -> !isValid(displayLanguage) || name.getLocale().equals(displayLanguage.getCode()));
    }

    public Optional<String> getDisplayForLanguage(List<LocalizedText> names, String displayLanguage) {
        return names.stream()
                .sorted(Comparator.comparing(LocalizedText::getLocalePreferred, Comparator.reverseOrder()))
                .filter(name -> !isValid(displayLanguage) || name.getLocale().equals(displayLanguage))
                .map(LocalizedText::getName)
                .findFirst();
    }

    public static String notFound(Class<? extends MetadataResource> cls, StringType url, StringType version) {
        return String.format("Resource of type %s with URL %s%s is not known",
                cls.getSimpleName(),
                url,
                isValid(version) ? " and version "+version : "");
    }

    public static String notFound(Class<? extends MetadataResource> cls, StringType owner, StringType id, StringType version) {
        return String.format("Resource of type %s with owner %s and ID %s/%s is not known",
                cls.getSimpleName(),
                getOwner(owner.getValue()),
                id,
                isValid(version) ? VERSION + "/" + version : "");
    }

    public static boolean isVersionAll(StringType version) {
        return isValid(version) && version.getValue().equals("*");
    }

    public static ResponseEntity<String> badRequest() {
        return ResponseEntity.badRequest().body("{\"exception\":\"Could not process the request.\"}");
    }

    public static ResponseEntity<String> notFound(int status, String body) {
        return ResponseEntity.status(status).body(body);
    }

    public static StringType newStringType(UriType type) {
        StringType stringType = new StringType();
        if (type != null) stringType.setValue(type.getValue());
        return stringType;
    }

    public static StringType newStringType(CodeType type) {
        StringType stringType = new StringType();
        if (type != null) stringType.setValue(type.getCode());
        return stringType;
    }

    public static StringType newStringType(String value) {
        return new StringType(value);
    }

    public static UriType newUri(String url) {
        return new UriType(url);
    }

    public static BooleanType newBoolean(Boolean value) {
        return new BooleanType(value);
    }

    public static IntegerType newInteger(Integer value) {
        return new IntegerType(value);
    }

    public static String newString(CodeType type) {
        return newStringType(type).getValue();
    }

    public static String getCode(CodeType type) {
        return isValid(type) ? type.getCode() : EMPTY;
    }

    public static int getPage(StringType page) {
        if (isValid(page) && !StringUtils.isNumeric(page.getValue())) {
            throw new InvalidRequestException("Page value must be positive numeric value.");
        }
        return page == null || page.getValue().matches("0|1") ? 0 : Integer.parseInt(page.getValue()) - 1;
    }

    public static <T extends MetadataResource> void addJsonFields(T resource, String identifier, String contact, String jurisdiction) {
        JsonObject object = new JsonObject();
        object.addProperty(RESOURCE_TYPE, resource.getClass().getSimpleName());
        addJsonProperty(object, resource.getClass().getSimpleName(), IDENTIFIER, identifier);
        addJsonProperty(object, resource.getClass().getSimpleName(), CONTACT, contact);
        addJsonProperty(object, resource.getClass().getSimpleName(), JURISDICTION, jurisdiction);

        if (resource instanceof CodeSystem) {
            CodeSystem cs = (CodeSystem) getFhirContext().newJsonParser().parseResource(gson.toJson(object));
            ((CodeSystem) resource).setIdentifier(cs.getIdentifier());
            resource.setContact(cs.getContact());
            resource.setJurisdiction(cs.getJurisdiction());
        } else if (resource instanceof ValueSet) {
            ValueSet vs = (ValueSet) getFhirContext().newJsonParser().parseResource(gson.toJson(object));
            ((ValueSet) resource).setIdentifier(vs.getIdentifier());
            resource.setContact(vs.getContact());
            resource.setJurisdiction(vs.getJurisdiction());
        }
    }

    private static void addJsonProperty(JsonObject object, String resourceType, String property, String value) {
        if (isValid(value)) {
            try {
                JsonArray array = jsonArray(value);
                object.add(property, array);
            } catch (Exception e) {
                log.warn(String.format("Error parsing {}.{} ", resourceType, property) + e.getMessage(), e);
            }
        }
    }

    public static JsonArray jsonArray(String value) {
        JsonElement e = jsonParser.parse(value);
        JsonArray ar = new JsonArray();
        if (! (e instanceof JsonArray)) {
            ar.add(e);
        } else {
            ar = e.getAsJsonArray();
        }
        return ar;
    }
}
