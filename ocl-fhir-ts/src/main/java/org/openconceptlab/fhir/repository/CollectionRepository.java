package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Collection;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The CollectionRepository.
 * @author harpatel1
 */
@Repository
public interface CollectionRepository extends BaseOclRepository<Collection>{

    List<Collection> findByMnemonicAndPublicAccessIn(String mnemonic, List<String> publicAccess);
    List<Collection> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String orgId, List<String> publicAccess);
    List<Collection> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String collectionId, String username, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Collection> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonicOrUserIdUsername(String org, String username);
    List<Collection> findByExternalIdIs(String externalId);
    List<Collection> findByExternalIdAndPublicAccessIn(String externalId, List<String> publicAccess);

}
