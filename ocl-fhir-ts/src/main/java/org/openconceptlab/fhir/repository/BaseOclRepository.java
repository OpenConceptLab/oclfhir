package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.BaseOclEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * The BaseOclRepository.
 * @author hp11
 */
@NoRepositoryBean
public interface BaseOclRepository<T extends BaseOclEntity> extends JpaRepository<T, Integer> {
}
