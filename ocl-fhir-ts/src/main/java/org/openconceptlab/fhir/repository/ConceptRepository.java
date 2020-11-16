package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.LocalizedText;
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

    @Query(nativeQuery = true, value = "select * from concepts c where C.id in (select concept_id from concepts_sources cs where cs.source_id = :id)")
    List<Concept> findConceptsForSource(@Param("id") Long id);

}
