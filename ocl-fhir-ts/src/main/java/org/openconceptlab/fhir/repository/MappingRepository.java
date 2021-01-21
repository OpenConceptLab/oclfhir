package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.Concept;
import org.openconceptlab.fhir.model.Mapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The MappingRepository.
 * @author harpatel1
 */
@Repository
public interface MappingRepository extends BaseOclRepository<Mapping>{

    @Query(nativeQuery = true, value =
            "select * from mappings m2 where m2.id in \n" +
                    "\t(select mapping_id from \n" +
                    "\t\t(select max(ms.mapping_id) as mapping_id , m1.from_source_url, m1.to_source_url, \n" +
                    "\t\t\tm1.from_concept_code, m1.to_concept_code, m1.map_type from mappings_sources ms \n" +
                    "\t\t\tinner join mappings m1 on m1.id = ms.mapping_id \n" +
                    "\t\t\twhere ms.source_id = :sourceId\n" +
                    "\t\t\tgroup by m1.from_source_url, m1.to_source_url, \n" +
                    "\t\t\tm1.from_concept_code, m1.to_concept_code, m1.map_type) as val \n" +
                    "\t) \n" +
                    "order by m2.from_concept_code asc")
    List<Mapping> findMappings(@Param("sourceId") Long sourceId, Pageable pageable);

}
