package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.ConceptsSource;

import java.util.List;

public interface ConceptsSourceRepository extends BaseOclRepository<ConceptsSource>{

    List<ConceptsSource> findBySourceIdAndConceptIdInOrderByConceptIdDesc(Long sourceId, List<Long> conceptIds);
    List<ConceptsSource> findBySourceId(Long sourceId);
}
