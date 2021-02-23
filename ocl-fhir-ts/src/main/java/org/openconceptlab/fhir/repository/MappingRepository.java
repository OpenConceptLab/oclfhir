package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The MappingRepository.
 * @author harpatel1
 */
@Repository
public interface MappingRepository extends BaseOclRepository<Mapping>{

    @Query(nativeQuery = true, value =
            "select * from mappings m2 where m2.id in " +
                    " (select mapping_id from " +
                    " (select max(ms.mapping_id) as mapping_id , m1.from_source_url, m1.to_source_url, " +
                    " m1.from_concept_code, m1.to_concept_code, m1.map_type from mappings_sources ms " +
                    " inner join mappings m1 on m1.id = ms.mapping_id " +
                    " where ms.source_id = :sourceId " +
                    " group by m1.from_source_url, m1.to_source_url, " +
                    " m1.from_concept_code, m1.to_concept_code, m1.map_type) as val " +
                    " ) " +
                    " order by m2.from_concept_code asc ")
    Page<Mapping> findMappings(@Param("sourceId") Long sourceId, Pageable pageable);

    @Query(nativeQuery = true, value =
            "select * from mappings m2 where m2.id in " +
                    " (select mapping_id from " +
                    " (select max(ms.mapping_id) as mapping_id , m1.from_source_url, m1.to_source_url, " +
                    " m1.from_concept_code, m1.to_concept_code, m1.map_type from mappings_sources ms " +
                    " inner join mappings m1 on m1.id = ms.mapping_id " +
                    " where ms.source_id = :sourceId " +
                    " group by m1.from_source_url, m1.to_source_url, " +
                    " m1.from_concept_code, m1.to_concept_code, m1.map_type) as val " +
                    " ) and m2.from_concept_code = :fromCode and (m2.from_source_url = :fromUrlLocal or m2.from_source_url = :fromUrlCanonical) ")
    List<Mapping> findMappingsForCode(@Param("sourceId") Long sourceId,
                                      @Param("fromUrlLocal") String fromUrlLocal,
                                      @Param("fromUrlCanonical") String fromUrlCanonical,
                                      @Param("fromCode") String fromCode);
}
