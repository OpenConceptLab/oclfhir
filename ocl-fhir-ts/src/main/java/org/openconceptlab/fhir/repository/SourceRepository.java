package org.openconceptlab.fhir.repository;

import org.hl7.fhir.r4.model.Base;
import org.openconceptlab.fhir.model.Source;
import org.springframework.stereotype.Repository;

/**
 * The SourceRepository.
 * @author hp11
 */
@Repository
public interface SourceRepository extends BaseOclRepository<Source> {
}
