package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.BaseOclEntity;
import org.openconceptlab.fhir.model.Collection;
import org.openconceptlab.fhir.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * The BaseOclRepository.
 * @author harpatel1
 */
@NoRepositoryBean
public interface BaseOclRepository<T extends BaseOclEntity> extends JpaRepository<T, Integer> {

    List<T> findByPublicAccessIn(List<String> publicAccess);
    List<T> findById(Long id);

}
