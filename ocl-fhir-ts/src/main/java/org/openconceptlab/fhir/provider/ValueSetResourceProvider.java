package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

/**
 * The ValueSetResourceProvider.
 * @author hp11
 */
@Component
public class ValueSetResourceProvider implements IResourceProvider {
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ValueSet.class;
    }
}
