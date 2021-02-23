package org.openconceptlab.fhir.util;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ValueSet;

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
	public static final String CONCEPTCLASS = "conceptclass";
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

	public static final String OWNER_REGEX = "^/users/.*|^/orgs/.*";
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

	public static final String ACSN_SYSTEM = "http://hl7.org/fhir/v2/0203";
	public static final String ACSN = "ACSN";
	public static final String OCL_SYSTEM = "http://fhir.openconceptlab.org";

	public static final String CODESYSTEM = CodeSystem.class.getSimpleName();
	public static final String VALUESET = ValueSet.class.getSimpleName();
	public static final String CONCEPTMAP = ConceptMap.class.getSimpleName();

	public static final String EN_LOCALE = "en";
	public static final String DEFINITION = "definition";
	public static final String NA = "N/A";
	public static final String EMPTY_JSON = "{}";
	public static final String FS = "/";
	public static final String TYPE = "type";
	public static final String LOCALE = "locale";
	public static final String LOCALE_PREFERRED = "locale_preferred";
	public static final String CREATED_AT = "created_at";
	public static final String CONCEPT_ID = "concept_id";
	public static final String LOCALIZEDTEXT_ID = "localizedtext_id";
	public static final String PUBLIC_ACCESS = "public_access";
	public static final String IS_ACTIVE = "is_active";
	public static final String EXTRAS = "extras";
	public static final String URI = "uri";
	public static final String MNEMONIC = "mnemonic";
	public static final String RELEASED = "released";
	public static final String RETIRED = "retired";
	public static final String IS_LATEST_VERSION = "is_latest_version";
	public static final String FULL_NAME = "full_name";
	public static final String DEFAULT_LOCALE = "default_locale";
	public static final String CONCEPT_CLASS = "concept_class";
	public static final String COMMENT = "comment";
	public static final String CREATED_BY_ID = "created_by_id";
	public static final String UPDATED_BY_ID = "updated_by_id";
	public static final String PARENT_ID = "parent_id";
	public static final String UPDATED_AT = "updated_at";

	public static final String AUTHORIZATION = "Authorization";
	public static final String VIEW = "View";
	public static final String EDIT = "Edit";
	public static final String NONE = "None";
	public static final String CONCEPTS = "concepts";
	public static final String COLLECTIONS = "collections";
	public static final String SOURCES = "sources";
	public static final String EXPRESSION = "expression";
	public static final String LAST_RESOLVED_AT = "last_resolved_at";
	public static final boolean True = true;
	public static final boolean False = false;
	public static final String CONCEPT_MAP_VERSION = "conceptMapVersion";
	public static final String TARGET_SYSTEM = "targetSystem";
	public static final String TRANSLATE = "$translate";

	public static final String OWNER_URL = "ownerUrl";
}
