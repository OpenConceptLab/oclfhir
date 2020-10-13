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
    List<Source> findByMnemonicAndPublicAccessIn(String mnemonic, List<String> publicAccess);
    List<Source> findByMnemonicAndOrganizationMnemonicAndPublicAccessIn(String sourceId, String orgId, List<String> publicAccess);
    List<Source> findByMnemonicAndUserIdUsernameAndPublicAccessIn(String sourceId, String username, List<String> publicAccess);
    List<Source> findByOrganizationMnemonicAndPublicAccessIn(String org, List<String> publicAccess);
    List<Source> findByUserIdUsernameAndPublicAccessIn(String username, List<String> publicAccess);
    List<Source> findByOrganizationMnemonicOrUserIdUsername(String org, String username);
    List<Source> findByExternalIdIs(String externalId);
    List<Source> findByExternalIdAndPublicAccessIn(String externalId, List<String> publicAccess);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update sources set mnemonic = :id where id = :id", nativeQuery = true)
    void updateMnemonic(@Param("id") Long id);
}
