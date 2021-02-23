package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Concept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The ConceptRepository.
 * @author harpatel1
 */
@Repository
public interface ConceptRepository extends BaseOclRepository<Concept>{

    List<Concept> findByMnemonic(String mnemonic);

    @Query(nativeQuery = true, value = "select * from concepts c2 where c2.id in (" +
            "select concept_id from (\n" +
            "select max(cs.concept_id) as concept_id , c1.mnemonic from concepts_sources cs \n" +
            "inner join concepts c1 on c1.id = cs.concept_id \n" +
            "where cs.source_id = :sourceId\n" +
            "group by c1.mnemonic) as val \n" +
            ") order by c2.mnemonic asc")
    Page<Concept> findConcepts(@Param("sourceId") Long sourceId, Pageable pageable);

    @Query(nativeQuery = true, value = "select count(*) from (select max(cs.concept_id) as concept_id , c1.mnemonic from concepts_sources cs \n" +
            "inner join concepts c1 on c1.id = cs.concept_id \n" +
            "where cs.source_id = :sourceId\n" +
            "group by c1.mnemonic) as val")
    int findConceptCountInSource(@Param("sourceId") Long sourceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update concepts set version = :id where id = :id", nativeQuery = true)
    void updateVersion(@Param("id") Long id);
}
