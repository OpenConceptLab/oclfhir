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

    /* -------- global --------- */
    // find all latest
    @Query(value = "select * from sources s1 where s1.public_access in :publicAccess and s1.is_latest_version = true order by s1.mnemonic ",
            nativeQuery = true)
    List<Source> findAllLatest(@Param("publicAccess") List<String> publicAccess);
    // list source versions
    List<Source> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);
    // version
    Source findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);
    // latest
    Source findFirstByCanonicalUrlAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, List<String> publicAccess, boolean latestVersion);


     /* -------- organization -------- */
    // find all latest
    List<Source> findByOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByMnemonic(String org, List<String> publicAccess, boolean latestVersion);
    // list source versions
    List<Source> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String orgId, List<String> publicAccess);
    // single version
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String version, String orgId, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndOrganizationMnemonic(String sourceId, String version, String orgId);
    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(String canonicalUrl, String version, String orgId);
    // latest version
    Source findFirstByMnemonicAndPublicAccessInAndOrganizationMnemonicAndIsLatestVersionOrderByCreatedAtDesc(String sourceId, List<String> publicAccess, String orgId, boolean latestVersion);
    Source findFirstByCanonicalUrlAndOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, String orgId, List<String> publicAccess, boolean latestVersion);


     /* -------- user -------- */
    // find all latest
    List<Source> findByUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByMnemonic(String username, List<String> publicAccess, boolean latestVersion);
    // list source versions
    List<Source> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String sourceId, String username, List<String> publicAccess);

    // single version
    Source findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String sourceId, String version, String username, List<String> publicAccess);
    Source findFirstByMnemonicAndVersionAndUserIdUsername(String sourceId, String version, String username);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);
    Source findFirstByCanonicalUrlAndVersionAndUserIdUsername(String canonicalUrl, String version, String username);
    // latest version
    Source findFirstByMnemonicAndPublicAccessInAndUserIdUsernameAndIsLatestVersionOrderByCreatedAtDesc(String sourceId, List<String> publicAccess, String username, boolean latestVersion);
    Source findFirstByCanonicalUrlAndUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, String username, List<String> publicAccess, boolean latestVersion);


    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update sources set mnemonic = :id where id = :id", nativeQuery = true)
    void updateMnemonic(@Param("id") Long id);

}
