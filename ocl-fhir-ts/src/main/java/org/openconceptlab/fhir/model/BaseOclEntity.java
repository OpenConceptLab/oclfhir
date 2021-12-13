package org.openconceptlab.fhir.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.MappedSuperclass;

/**
 * Base OCL entity.
 * @author harpatel1
 */
@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonType.class)
})
public class BaseOclEntity {
}
