package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the collections_references database table.
 * @author hp11
 */
@Entity
@Table(name="collections_references")
@NamedQuery(name="CollectionsReference.findAll", query="SELECT c FROM CollectionsReference c")
public class CollectionsReference extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="COLLECTIONS_REFERENCES_ID_GENERATOR", sequenceName="COLLECTIONS_REFERENCES_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="COLLECTIONS_REFERENCES_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	private CollectionReference collectionreference;

	@ManyToOne
	private Collection collection;

	public CollectionsReference() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CollectionReference getCollectionReference() {
		return this.collectionreference;
	}

	public void setCollectionReference(CollectionReference collectionReference) {
		this.collectionreference = collectionReference;
	}

	public Collection getCollection() {
		return this.collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

}
