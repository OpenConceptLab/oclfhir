package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
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

    /* -------- global --------- */
    // find all latest
    @Query(value = "select * from collections c1 where c1.public_access in :publicAccess and c1.is_latest_version = true order by c1.mnemonic ",
            nativeQuery = true)
    List<Collection> findAllLatest(@Param("publicAccess") List<String> publicAccess);
    // list collection versions
    List<Collection> findByCanonicalUrlAndPublicAccessIn(String canonicalUrl, List<String> publicAccess);
    // single version
    Collection findFirstByCanonicalUrlAndVersionAndPublicAccessIn(String canonicalUrl, String version, List<String> publicAccess);
    // latest version
    Collection findFirstByCanonicalUrlAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, List<String> publicAccess, boolean latestVersion);


    /* -------- organization -------- */
    // find all latest
    List<Collection> findByOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByMnemonic(String org, List<String> publicAccess, boolean latestVersion);
    // list collection versions
    List<Collection> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String orgId, List<String> publicAccess);
    // single version
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(String collectionId, String version, String orgId, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(String canonicalUrl, String version, String orgId, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndOrganizationMnemonic(String canonicalUrl, String version, String orgId);
    Collection findFirstByMnemonicAndVersionAndOrganizationMnemonic(String sourceId, String version, String orgId);
    // latest version
    Collection findFirstByMnemonicAndPublicAccessInAndOrganizationMnemonicAndIsLatestVersionOrderByCreatedAtDesc(String collectionId, List<String> publicAccess, String orgId, boolean latestVersion);
    Collection findFirstByCanonicalUrlAndOrganizationMnemonicAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, String orgId, List<String> publicAccess, boolean latestVersion);


    /* -------- user -------- */
    // find all latest
    List<Collection> findByUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByMnemonic(String username, List<String> publicAccess, boolean latestVersion);
    // list collection versions
    List<Collection> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String collectionId, String username, List<String> publicAccess);
    // single version
    Collection findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(String collectionId, String version, String username, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndUserIdUsernameAndPublicAccessIn(String canonicalUrl, String version, String username, List<String> publicAccess);
    Collection findFirstByCanonicalUrlAndVersionAndUserIdUsername(String canonicalUrl, String version, String username);
    Collection findFirstByMnemonicAndVersionAndUserIdUsername(String sourceId, String version, String username);
    // latest version
    Collection findFirstByMnemonicAndPublicAccessInAndUserIdUsernameAndIsLatestVersionOrderByCreatedAtDesc(String collectionId, List<String> publicAccess, String username, boolean latestVersion);
    Collection findFirstByCanonicalUrlAndUserIdUsernameAndPublicAccessInAndIsLatestVersionOrderByCreatedAtDesc(String canonicalUrl, String username, List<String> publicAccess, boolean latestVersion);

}
