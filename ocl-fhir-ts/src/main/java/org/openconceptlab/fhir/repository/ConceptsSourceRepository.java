package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.ConceptsSource;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConceptsSourceRepository extends BaseOclRepository<ConceptsSource>{

    List<ConceptsSource> findBySourceIdAndConceptIdInOrderByConceptIdDesc(Long sourceId, List<Long> conceptIds);
    List<ConceptsSource> findBySourceIdOrderByConceptMnemonicAsc(Long sourceId, Pageable pageable);
}
