package org.openconceptlab.fhir.model;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the collections_concepts database table.
 * @author harpatel1
 */
@Entity
@Table(name="collections_concepts")
public class CollectionsConcept extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Collection collection;

	@ManyToOne
	private Concept concept;

	public CollectionsConcept() {
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

	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

}
