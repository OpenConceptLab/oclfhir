package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ConceptMap;
import org.springframework.stereotype.Component;

/**
 * The ConceptMapResourceProvider.
 * @author harpatel1
 */
@Component
public class ConceptMapResourceProvider implements IResourceProvider {

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return ConceptMap.class;
    }

}
