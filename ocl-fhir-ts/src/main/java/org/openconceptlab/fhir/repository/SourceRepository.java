package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Source;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The SourceRepository.
 * @author harpatel1
 */
@Repository
public interface SourceRepository extends BaseOclRepository<Source> {

    List<Source> findByPublicAccessIn(List<String> publicAccess);
    List<Source> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String orgId, List<String> publicAccess);
    List<Source> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String sourceId, String username, List<String> publicAccess);
    List<Source> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Source> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Source> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);

    // versioned
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String version, String orgId, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String sourceId, String version, String username, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonic(String sourceId, String version, String orgId);
    Source findFirstByMnemonicAndVersionAndUserIdUsername(String sourceId, String version, String username);
    Source findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);

    Source findFirstByMnemonicAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(String sourceId, List<String> publicAccess, String orgId);
    Source findFirstByMnemonicAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(String sourceId, List<String> publicAccess, String username);
    Source findFirstByCanonicalUrlAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, List<String> publicAccess);

    Source findFirstByCanonicalUrlAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, String orgId, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, String username, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(String canonicalUrl, String version, String orgId);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsername(String canonicalUrl, String version, String username);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update sources set mnemonic = :id where id = :id", nativeQuery = true)
    void updateMnemonic(@Param("id") Long id);

    @Query(value =
            "select * from sources s1 " +
                    " inner join (select s2.mnemonic as mnemonic ,max(s2.created_at) as created_at from sources s2 group by s2.mnemonic) s3 " +
                    " on s3.mnemonic = s1.mnemonic and s3.created_at = s1.created_at " +
                    " where s1.public_access in :publicAccess " +
                    " order by s1.mnemonic ",
            nativeQuery = true)
    List<Source> findAllLatest(@Param("publicAccess") List<String> publicAccess);
}
