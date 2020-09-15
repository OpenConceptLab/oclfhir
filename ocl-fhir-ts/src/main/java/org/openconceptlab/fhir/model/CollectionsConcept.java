package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the collections_concepts database table.
 * @author hp11
 */
@Entity
@Table(name="collections_concepts")
@NamedQuery(name="CollectionsConcept.findAll", query="SELECT c FROM CollectionsConcept c")
public class CollectionsConcept extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="COLLECTIONS_CONCEPTS_ID_GENERATOR", sequenceName="COLLECTIONS_CONCEPTS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="COLLECTIONS_CONCEPTS_ID_GENERATOR")
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
