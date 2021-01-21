package org.openconceptlab.fhir.repository;

import org.hl7.fhir.r4.model.Base;
import org.openconceptlab.fhir.model.BaseOclEntity;
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
    List<Source> findByMnemonicAndPublicAccessIn(String mnemonic, List<String> publicAccess);
    List<Source> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String orgId, List<String> publicAccess);
    List<Source> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String sourceId, String username, List<String> publicAccess);
    List<Source> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Source> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Source> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);
    List<Source> findByCanonicalUrl(String canonicalUrl);

    // versioned
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String version, String orgId, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String sourceId, String version, String username, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonic(String sourceId, String version, String orgId);
    Source findFirstByMnemonicAndVersionAndUserIdUsername(String sourceId, String version, String username);

    Source findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);

    Source findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(String sourceId, Boolean released, List<String> publicAccess,
                                                                                                      String orgId);
    Source findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(String sourceId, Boolean released, List<String> publicAccess,
                                                                                                String username);
    Source findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, Boolean released, List<String> publicAccess);

    Source findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, boolean released,
                                                                                                              String orgId, List<String> publicAccess);

    Source findFirstByCanonicalUrlAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String canonicalUrl, boolean released,
                                                                                                          String username, List<String> publicAccess);

    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);

    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(String canonicalUrl, String version, String orgId);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsername(String canonicalUrl, String version, String username);

    Source findFirstByMnemonicAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(String sourceId, boolean released,
                                                                                                          String orgId, List<String> publicAccess);

    Source findFirstByMnemonicAndReleasedAndUserIdUsernameAndPublicAccessInOrderByCreatedAtDesc(String sourceId, boolean released,
                                                                                                    String username, List<String> publicAccess);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update sources set mnemonic = :id where id = :id", nativeQuery = true)
    void updateMnemonic(@Param("id") Long id);

    @Query(value =
            "select * from sources s1 " +
                    " inner join (select s2.mnemonic as mnemonic ,max(s2.created_at) as created_at from sources s2 where s2.released = true group by s2.mnemonic) s3 " +
                    " on s3.mnemonic = s1.mnemonic and s3.created_at = s1.created_at " +
                    " where s1.public_access in :publicAccess " +
                    " order by s1.mnemonic ",
            nativeQuery = true)
    List<Source> findAllMostRecentReleased(@Param("publicAccess") List<String> publicAccess);
}
