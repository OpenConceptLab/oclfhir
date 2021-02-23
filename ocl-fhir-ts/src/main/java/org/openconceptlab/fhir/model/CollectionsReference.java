package org.openconceptlab.fhir.model;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the collections_references database table.
 * @author harpatel1
 */
@Entity
@Table(name="collections_references")
public class CollectionsReference extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(cascade = CascadeType.ALL)
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
