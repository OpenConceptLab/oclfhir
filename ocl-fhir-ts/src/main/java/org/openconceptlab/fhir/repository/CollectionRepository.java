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

    // versioned
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String version, String orgId, List<String> publicAccess);
    Collection findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String collectionId, String version, String username, List<String> publicAccess);
    Collection findFirstByExternalIdAndVersionAndPublicAccessIn(String externalId, String version, List<String> publicAccess);

    Collection findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(String collectionId, Boolean released, List<String> publicAccess,
                                                                                                      String orgId);
    Collection findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(String collectionId, Boolean released, List<String> publicAccess,
                                                                                                String username);
    Collection findFirstByExternalIdAndReleasedAndPublicAccessInOrderByCreatedAtDesc(String externalId, Boolean released, List<String> publicAccess);

}
