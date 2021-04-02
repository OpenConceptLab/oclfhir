package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The CollectionRepository.
 * @author harpatel1
 */
@Repository
public interface CollectionRepository extends BaseOclRepository<Collection>{

    List<Collection> findByPublicAccessIn(List<String> publicAccess);
    List<Collection> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String orgId, List<String> publicAccess);
    List<Collection> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String collectionId, String username, List<String> publicAccess);
    List<Collection> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Collection> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Collection> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);

    // versioned
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String version, String orgId, List<String> publicAccess);
    Collection findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String collectionId, String version, String username, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);

    Collection findFirstByMnemonicAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(String collectionId, List<String> publicAccess, String orgId);
    Collection findFirstByMnemonicAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(String collectionId, List<String> publicAccess, String username);
    Collection findFirstByCanonicalUrlAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, String orgId, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, String username, List<String> publicAccess);

    Collection findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(String canonicalUrl, String version, String orgId);
    Collection findFirstByCanonicalUrlAndVersionAndUserIdUsername(String canonicalUrl, String version, String username);
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonic(String sourceId, String version, String orgId);
    Collection findFirstByMnemonicAndVersionAndUserIdUsername(String sourceId, String version, String username);

    @Query(value =
            "select * from collections s1 " +
                    " inner join (select s2.mnemonic as mnemonic ,max(s2.created_at) as created_at from collections s2 group by s2.mnemonic) s3 " +
                    " on s3.mnemonic = s1.mnemonic and s3.created_at = s1.created_at " +
                    " where s1.public_access in :publicAccess " +
                    " order by s1.mnemonic ",
            nativeQuery = true)
    List<Collection> findAllLatest(@Param("publicAccess") List<String> publicAccess);

}
