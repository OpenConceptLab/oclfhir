package org.openconceptlab.fhir;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

    @Value("${oclfhir.version}")
    private String oclFhirVersion;

    public String getOclFhirVersion() {
        return oclFhirVersion;
    }
}
