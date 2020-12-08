package org.openconceptlab.fhir.util;

import java.util.Arrays;
import java.util.List;

public final class OclFhirConstants {
	
	public static final String HEAD = "HEAD";
	public static final String FILTERS = "filters";
	public static final String CODE = "code";
	public static final String IDENTIFIERS = "identifiers";	
	public static final String DESC = "description";
	public static final String OPERATOR = "operator";
	public static final String VALUE = "value";
	public static final String SYSTEM = "system";
	public static final String VERSION = "version";
	public static final String USE = "use";
	public static final String CONCEPT_CLASS = "conceptclass";
	public static final String DATATYPE = "datatype";
	public static final String PURPOSE = "purpose";
	public static final String COPYRIGHT = "copyright";
	public static final String VS_COMPOSE_INACTIVE = "compose.inactive";
	public static final String VS_COMPOSE_INCLUDE = "compose.include";
	public static final String PROPERTY = "property";
	public static final String OP = "op";
	public static final String value = "value";
	public static final String DISPLAY = "display";
	public static final String NAME = "name";
	public static final String LANGUAGE = "language";
	public static final String DISP_LANG = "displayLanguage";
	public static final String LOOKUP = "$lookup";
	public static final String URL = "url";
	public static final String VALIDATE_CODE = "$validate-code";
	public static final String EXPAND = "$expand";
	public static final String DESIGNATION = "designation";
	public static final String RESULT = "result";
	public static final String MESSAGE = "message";
	public static final String CODING = "coding";
	public static final String SYSTEM_VERSION = "systemVersion";
	public static final String VALUESET_VERSION = "valueSetVersion";
	public static final String OFFSET = "offset";
	public static final String COUNT = "count";
	public static final String INCLUDE_DESIGNATIONS = "includeDesignations";
	public static final String INCLUDE_DEFINITION = "includeDefinition";
	public static final String ACTIVE_ONLY = "activeOnly";
	public static final String DISPLAY_LANGUAGE = "displayLanguage";
	public static final String EXCLUDE_SYSTEM = "exclude-system";
	public static final String SYSTEMVERSION = "system-version";
	public static final String FILTER = "filter";
	public static final String PAGE = "page";
	public static final String RESOURCE_TYPE = "resourceType";
	public static final String IDENTIFIER = "identifier";
	public static final String CONTACT = "contact";
	public static final String JURISDICTION = "jurisdiction";

	public static final String PUBLISHER_REGEX = "^user:.*|^org:.*";
	public static final String ORG_ = "org:";
	public static final String USER_ = "user:";
	public static final String ORG = "org";
	public static final String USER = "user";
	public static final String ORGS = "orgs";
	public static final String USERS = "users";
	public static final String SEP = ":";
	public static final List<String> publicAccess = Arrays.asList("View", "Edit");
	public static final String OWNER = "owner";
	public static final String ID = "id";
	public static final String ALL = "*";
	public static final String EMPTY = "";

	public static final String SYSTEM_CC = "https://api.openconceptlab.org/orgs/OCL/sources/Classes/concepts";
	public static final String DESC_CC = "Standard list of concept classes.";
	public static final String SYSTEM_DT = "https://api.openconceptlab.org/orgs/OCL/sources/Datatypes/concepts";
	public static final String DESC_DT = "Standard list of concept datatypes.";
	public static final String INACTIVE = "inactive";
	public static final String SYSTEM_HL7_CONCEPT_PROP = "http://hl7.org/fhir/concept-properties";
	public static final String DESC_HL7_CONCEPT_PROP = "True if the concept is not considered active.";
}
