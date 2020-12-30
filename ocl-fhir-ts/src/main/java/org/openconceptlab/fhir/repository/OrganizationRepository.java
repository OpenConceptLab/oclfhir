package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Organization;
import org.springframework.stereotype.Repository;

/**
 * The OrganizationRepository.
 * @author harpatel1
 */
@Repository
public interface OrganizationRepository extends BaseOclRepository<Organization>{

    Organization findByMnemonic(String mnemonic);

}
