package org.openconceptlab.fhir.util;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Arrays;
import java.util.Collections;
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
	public static final String TEXT = "text";
	public static final String META = "meta";

	public static final String OWNER_REGEX = "^/users/.*|^/orgs/.*";
	public static final String ORG_ = "org:";
	public static final String USER_ = "user:";
	public static final String ORG = "org";
	public static final String USER = "user";
	public static final String ORGS = "orgs";
	public static final String USERS = "users";
	public static final String SEP = ":";
	public static final List<String> publicAccess = Collections.singletonList("View");
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
	public static final String MAPPINGS = "mappings";
	public static final String EXPRESSION = "expression";
	public static final String LAST_RESOLVED_AT = "last_resolved_at";
	public static final boolean True = true;
	public static final boolean False = false;
	public static final String CONCEPT_MAP_VERSION = "conceptMapVersion";
	public static final String TARGET_SYSTEM = "targetSystem";
	public static final String TRANSLATE = "$translate";

	public static final String OWNER_URL = "ownerUrl";
	public static final String FROM_SOURCE_URL = "from_source_url";
	public static final String FROM_SOURCE_VERSION = "from_source_version";
	public static final String FROM_SOURCE_ID = "from_source_id";
	public static final String FROM_CONCEPT_CODE = "from_concept_code";
	public static final String FROM_CONCEPT_NAME = "from_concept_name";
	public static final String FROM_CONCEPT_ID = "from_concept_id";
	public static final String TO_SOURCE_URL = "to_source_url";
	public static final String TO_SOURCE_VERSION = "to_source_version";
	public static final String TO_SOURCE_ID = "to_source_id";
	public static final String TO_CONCEPT_CODE = "to_concept_code";
	public static final String TO_CONCEPT_NAME = "to_concept_name";
	public static final String TO_CONCEPT_ID = "to_concept_id";
	public static final String MAP_TYPE = "map_type";
	public static final String RESOURCE_ID = "resourceId";

	// swagger documentation constants
	public static final String CODE_SYSTEM_GLOBAL_NAMESPACE = "ⓖ CodeSystem";
	public static final String VALUE_SET_GLOBAL_NAMESPACE = "ⓖ ValueSet";
	public static final String CONCEPT_MAP_GLOBAL_NAMESPACE = "ⓖ ConceptMap";

	public static final String CODE_SYSTEM_ORGANIZATION_NAMESPACE = "ⓞ CodeSystem";
	public static final String VALUE_SET_ORGANIZATION_NAMESPACE = "ⓞ ValueSet";
	public static final String CONCEPT_MAP_ORGANIZATION_NAMESPACE = "ⓞ ConceptMap";

	public static final String CODE_SYSTEM_USER_NAMESPACE = "ⓤ CodeSystem";
	public static final String VALUE_SET_USER_NAMESPACE = "ⓤ ValueSet";
	public static final String CONCEPT_MAP_USER_NAMESPACE = "ⓤ ConceptMap";

	public static final String THE_FHIR_PARAMETERS_OBJECT = "the FHIR Parameters object";
	public static final String DOCUMENTATION_CONTROLLER_CALLED = "Documentation controller called.";
	public static final String THE_CODESYSTEM_URL = "the codesystem url";
	public static final String THE_CODESYSTEM_VERSION = "the codesystem version";
	public static final String THE_CONCEPTMAP_URL = "the conceptmap url";
	public static final String THE_CONCEPTMAP_VERSION = "the conceptmap version";
	public static final String THE_CONCEPT_CODE = "the concept code";
	public static final String THE_VALUESET_URL = "the valueset url";
	public static final String THE_VALUESET_VERSION = "the valueset version";
	public static final String THE_DISPLAY_ASSOCIATED_WITH_THE_CODE = "the display associated with the code";
	public static final String THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY = "the language to be used for description when validating the display property";
	public static final String VALUESET_EXPAND_FILTER_TEXT = "The case-sensitive code filter to be used to control codes included in " +
			"valueSet expansion. If multiple filters are needed then each code filter should be separated by double underscore \"\\_\\_\", " +
			"for example - EMR__HRP__KP (EMR or HRP or KP). If the filter itself includes \"\\_\", then the filter should be surrounded in double quotes. " +
			"For example, if user wants to filter on \"HRH\\_\" then the multi filter string should be EMR__\"HRH_\"__KP (EMR or HRH_ or KP) .";
	public static final String STARTING_INDEX_IF_SUBSET_IS_DESIRED = "starting index if subset is desired";
	public static final String NUMBER_OF_CODES_TO_BE_RETURNED = "number of codes to be returned";
	public static final String INCLUDE_CONCEPT_DESIGNATIONS = "include concept designations";
	public static final String INCLUDE_VALUESET_DEFINITION = "include valueset definition";
	public static final String ONLY_INCLUDE_ACTIVE_CONCEPTS = "only include active concepts";
	public static final String THE_LANGUAGE_TO_BE_USED_FOR_VALUE_SET_EXPANSION_CONTAINS_DISPLAY = "the language to be used for ValueSet.expansion.contains.display";
	public static final String CANONICAL_REFERENCE_TO_CODESYSTEM_VERSION_TO_BE_EXCLUDED = "canonical reference to codesystem/version to be excluded";
	public static final String CANONICAL_REFERENCE_TO_CODESYSTEM_VERSION_TO_BE_USED_IF_NOT_EXISTS_IN_VALUESET = "canonical reference to codesystem/version to be used if not exists in valueset";
	public static final String THE_LANGUAGE_TO_BE_USED_FOR_DISPLAY_PARAMETER = "the language to be used for display parameter";
	public static final String THE_SOURCE_CODESYSTEM_URL = "the source codesystem url";
	public static final String THE_SOURCE_CODESYSTEM_VERSION = "the source codesystem version";
	public static final String THE_CONCEPT_CODE_TO_BE_TRANSLATED = "the concept code to be translated";
	public static final String THE_TARGET_CODESYSTEM_URL = "the target codesystem url";
	public static final String GET_SEARCH_CODE_SYSTEMS = "Get/Search CodeSystems";
	public static final String GET_SEARCH_VALUE_SETS = "Get/Search ValueSets";
	public static final String GET_SEARCH_CONCEPT_MAPS = "Get/Search ConceptMaps";

	public static final String PERFORM_LOOKUP_BY_URL = "Perform $lookup by url";
	public static final String PERFORM_VALIDATE_CODE_BY_URL = "Perform $validate-code by url";
	public static final String PERFORM_EXPAND_BY_URL = "Perform $expand by url";
	public static final String PERFORM_TRANSLATE_BY_URL = "Perform $translate by url";

	public static final String PERFORM_LOOKUP_BY_ID = "Perform $lookup by id and/or version";
	public static final String PERFORM_VALIDATE_CODE_BY_ID = "Perform $validate-code by id and/or version";
	public static final String PERFORM_EXPAND_BY_ID = "Perform $expand by id and/or version";
	public static final String PERFORM_TRANSLATE_BY_ID = "Perform $translate by id and/or version";

	public static final String CREATE_CODESYSTEM = "create codesystem";
	public static final String UPDATE_CODESYSTEM_VERSION = "update codesystem version";
	public static final String DELETE_CODESYSTEM_VERSION = "delete codesystem version";
	public static final String DELETE_CONCEPT_FROM_CODESYSTEM_VERSION = "delete concept from codesystem version";
	public static final String GET_CODESYSTEM_BY_ORGANIZATION_AND_ID = "get codesystem by organization and id";
	public static final String GET_CODESYSTEM_BY_USER_AND_ID = "get codesystem by user and id";
	public static final String GET_SEARCH_CODESYSTEM_VERSIONS = "Get/Search codesystem versions";
	public static final String SEARCH_CODESYSTEMS_FOR_ORGANIZATION = "Search codesystems for organization";
	public static final String SEARCH_CODESYSTEMS_FOR_USER = "Search codesystems for user";
	public static final String THE_CODESYSTEM_ID = "the codesystem id";
	public static final String THE_ORGANIZATION_ID = "the organization id";
	public static final String THE_USERNAME = "the username";
	public static final String THE_CODESYSTEM_JSON_RESOURCE = "the codesystem json resource";

	public static final String CREATE_VALUESET = "create valueset";
	public static final String UPDATE_VALUESET_VERSION = "update valueset version";
	public static final String DELETE_VALUESET_VERSION = "delete valueset version";
	public static final String DELETE_CONCEPT_FROM_VALUESET_VERSION = "delete concept from valueset version";
	public static final String GET_VALUESET_BY_ORGANIZATION_AND_ID = "get valueset by organization and id";
	public static final String GET_VALUESET_BY_USER_AND_ID = "get valueset by user and id";
	public static final String GET_SEARCH_VALUESET_VERSIONS = "Get/Search valueset versions";
	public static final String SEARCH_VALUESETS_FOR_ORGANIZATION = "Search valuesets for organization";
	public static final String SEARCH_VALUESETS_FOR_USER = "Search valuesets for user";
	public static final String THE_VALUESET_ID = "the valueset id";
	public static final String THE_VALUESET_JSON_RESOURCE = "the valueset json resource";

	public static final String CREATE_CONCEPTMAP = "create conceptmap";
	public static final String UPDATE_CONCEPTMAP_VERSION = "update conceptmap version";
	public static final String DELETE_CONCEPTMAP_VERSION = "delete conceptmap version";
	public static final String DELETE_MAPPING_FROM_CONCEPTMAP_VERSION = "delete mapping from conceptmap version";
	public static final String GET_CONCEPTMAP_BY_ORGANIZATION_AND_ID = "get conceptmap by organization and id";
	public static final String GET_CONCEPTMAP_BY_USER_AND_ID = "get conceptmap by user and id";
	public static final String GET_SEARCH_CONCEPTMAP_VERSIONS = "Get/Search conceptmap versions";
	public static final String SEARCH_CONCEPTMAPS_FOR_ORGANIZATION = "Search conceptmaps for organization";
	public static final String SEARCH_CONCEPTMAPS_FOR_USER = "Search conceptmaps for user";
	public static final String THE_CONCEPTMAP_ID = "the conceptmap id";
	public static final String THE_CONCEPTMAP_JSON_RESOURCE = "the conceptmap json resource";

	public static final String CODESYSTEM_LOOKUP_REQ_BODY_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_LOOKUP_REQ_BODY_ID_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_LOOKUP_REQ_BODY_ID_VERSION_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"code\" : \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"systemVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"systemVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"systemVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"display\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\",\n" +
					"                \"display\":\"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_EXPAND_REQ_BODY_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"offset\",\n" +
					"            \"valueInteger\":0\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"count\",\n" +
					"            \"valueInteger\":100\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDesignations\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDefinition\",\n" +
					"            \"valueBoolean\": false\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"activeOnly\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"exclude-system\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system-version\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_EXPAND_REQ_BODY_ID_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"valueSetVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"offset\",\n" +
					"            \"valueInteger\":0\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"count\",\n" +
					"            \"valueInteger\":100\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDesignations\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDefinition\",\n" +
					"            \"valueBoolean\": false\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"activeOnly\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"exclude-system\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system-version\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String VALUESET_EXPAND_REQ_BODY_ID_VERSION_EXAMPLE =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"offset\",\n" +
					"            \"valueInteger\":0\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"count\",\n" +
					"            \"valueInteger\":100\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDesignations\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"includeDefinition\",\n" +
					"            \"valueBoolean\": false\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"activeOnly\",\n" +
					"            \"valueBoolean\": true\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"displayLanguage\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"exclude-system\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system-version\",\n" +
					"            \"valueCanonical\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"conceptMapVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_ID_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"conceptMapVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_ID_VERSION_EXAMPLE1 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"system\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"code\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"version\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"url\",\n" +
					"            \"valueUri\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"conceptMapVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_ID_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"conceptMapVersion\",\n" +
					"            \"valueString\":\"\"\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

	public static final String CONCEPTMAP_TRANSLATE_REQ_BODY_ID_VERSION_EXAMPLE2 =
			"{\n" +
					"    \"resourceType\":\"Parameters\",\n" +
					"    \"parameter\": [\n" +
					"        {\n" +
					"            \"name\":\"coding\",\n" +
					"            \"valueCoding\": {\n" +
					"                \"system\" : \"\",\n" +
					"                \"code\" : \"\",\n" +
					"                \"version\": \"\"\n" +
					"            }\n" +
					"        },\n" +
					"        {\n" +
					"            \"name\":\"targetSystem\",\n" +
					"            \"valueCode\":\"\"\n" +
					"        }\n" +
					"    ]\n" +
					"}";

}


