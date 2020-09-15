package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the collections_mappings database table.
 * @author hp11
 */
@Entity
@Table(name="collections_mappings")
@NamedQuery(name="CollectionsMapping.findAll", query="SELECT c FROM CollectionsMapping c")
public class CollectionsMapping extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="COLLECTIONS_MAPPINGS_ID_GENERATOR", sequenceName="COLLECTIONS_MAPPINGS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="COLLECTIONS_MAPPINGS_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	private Collection collection;

	@ManyToOne
	private Mapping mapping;

	public CollectionsMapping() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Collection getCollection() {
		return this.collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public Mapping getMapping() {
		return this.mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

}
