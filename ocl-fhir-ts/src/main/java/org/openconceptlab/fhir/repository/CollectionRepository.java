package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The CollectionRepository.
 * @author harpatel1
 */
@Repository
public interface CollectionRepository extends BaseOclRepository<Collection>{

    List<Collection> findByMnemonicAndPublicAccessIn(String mnemonic, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonic(String org);
    List<Collection> findByUserIdUsername(String username);
    List<Collection> findByOrganizationMnemonicOrUserIdUsername(String org, String username);

}
