package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.LocalizedText;
import org.springframework.stereotype.Repository;

/**
 * The ConceptRepository.
 * @author harpatel1
 */
@Repository
public interface ConceptRepository extends BaseOclRepository<Concept>{
}
