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

    List<Collection> findByPublicAccessIn(List<String> publicAccess);
    List<Collection> findByMnemonicAndPublicAccessIn(String mnemonic, List<String> publicAccess);
    List<Collection> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String orgId, List<String> publicAccess);
    List<Collection> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String collectionId, String username, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Collection> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonicOrUserIdUsername(String org, String username);
    List<Collection> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);

    // versioned
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String version, String orgId, List<String> publicAccess);
    Collection findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String collectionId, String version, String username, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);

    Collection findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(String collectionId, Boolean released, List<String> publicAccess,
                                                                                                      String orgId);
    Collection findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(String collectionId, Boolean released, List<String> publicAccess,
                                                                                                String username);
    Collection findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, Boolean released, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, boolean released,
                                                                                                          String orgId, List<String> publicAccess);

    Collection findFirstByCanonicalUrlAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, boolean released,
                                                                                                    String username, List<String> publicAccess);

    Collection findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);

    Collection findFirstByMnemonicAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String collectionId, boolean released,
                                                                                                      String orgId, List<String> publicAccess);

    Collection findFirstByMnemonicAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String collectionId, boolean released, String username, List<String> publicAccess);

}
