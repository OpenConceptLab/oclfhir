package org.openconceptlab.fhir.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

/**
 * The CodeSystemResourceProvider.
 * @author hp11
 */
@Component
public class CodeSystemResourceProvider implements IResourceProvider {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return CodeSystem.class;
    }

    /**
     * Simple implementation of the "read" method
     */
    @Read()
    @Transactional
    public CodeSystem read(@IdParam IdType theId) {
        CodeSystem codeSystem = new CodeSystem();
        codeSystem.setIdElement(theId);

        List<Collection> collections = collectionRepository.findAll();
//        UserProfile u = collections.get(0).getCreatedBy();
//        String createdVy = u.getUsername();
//         List<AuthtokenToken> tokens = u.getAuthtokenTokens();

        List<Concept> concepts = conceptRepository.findAll();
        Object o = concepts.get(0).getConceptsDescriptions().get(0).getId();
        Object o2 = concepts.get(0).getConceptsNames().get(0);

        List<Mapping> mappings = mappingRepository.findAll();

        List<Organization> organizations = organizationRepository.findAll();
        Object o3 = organizations.get(0).getUserProfilesOrganizations().get(0).getOrganization();

        List<Source> sources = sourceRepository.findAll();

        List<UserProfile> userProfiles = userRepository.findAll();
        UserProfile u2 = userProfiles.get(0);
        return codeSystem;
    }

}
