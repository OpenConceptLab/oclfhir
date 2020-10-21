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
	public static final String VS_COMPOSE_FILTER = "compose.filter";
	public static final String PROPERTY = "property";
	public static final String OP = "op";
	public static final String value = "value";

	public static final String PUBLISHER_REGEX = "^user:.*|^org:.*";
	public static final String ORG_ = "org:";
	public static final String USER_ = "user:";
	public static final String ORG = "org";
	public static final String USER = "user";
	public static final String SEP = ":";
	public static final List<String> publicAccess = Arrays.asList("View", "Edit");
	public static final String OWNER = "owner";
	public static final String ID = "id";
	public static final String _HISTORY = "_history";
	public static final String ALL = "*";

	public static final String SYSTEM_CC = "https://api.openconceptlab.org/orgs/OCL/sources/Classes/concepts";
	public static final String DESC_CC = "Standard list of concept classes.";
	public static final String SYSTEM_DT = "https://api.openconceptlab.org/orgs/OCL/sources/Datatypes/concepts";
	public static final String DESC_DT = "Standard list of concept datatypes.";
	public static final String INACTIVE = "inactive";
	public static final String SYSTEM_HL7_CONCEPT_PROP = "http://hl7.org/fhir/concept-properties";
	public static final String DESC_HL7_CONCEPT_PROP = "True if the concept is not considered active.";
}
