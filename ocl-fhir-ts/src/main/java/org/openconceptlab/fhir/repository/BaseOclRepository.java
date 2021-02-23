package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.BaseOclEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * The BaseOclRepository.
 * @author harpatel1
 */
@NoRepositoryBean
public interface BaseOclRepository<T extends BaseOclEntity> extends JpaRepository<T, Integer> {

}
