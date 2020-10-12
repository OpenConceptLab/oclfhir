package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the collection_references database table.
 * @author hp11
 */
@Entity
@Table(name="collection_references")
public class CollectionReference extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="created_at")
	private Timestamp createdAt;

	@Column
	private String expression;

	@Column(name="internal_reference_id")
	private String internalReferenceId;

	@Column(name="last_resolved_at")
	private Timestamp lastResolvedAt;

	@Column(name="updated_at")
	private Timestamp updatedAt;

	@OneToMany(mappedBy="collectionreference")
	private List<CollectionsReference> collectionsReferences;

	public CollectionReference() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getInternalReferenceId() {
		return this.internalReferenceId;
	}

	public void setInternalReferenceId(String internalReferenceId) {
		this.internalReferenceId = internalReferenceId;
	}

	public Timestamp getLastResolvedAt() {
		return this.lastResolvedAt;
	}

	public void setLastResolvedAt(Timestamp lastResolvedAt) {
		this.lastResolvedAt = lastResolvedAt;
	}

	public Timestamp getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<CollectionsReference> getCollectionsReferences() {
		return this.collectionsReferences;
	}

	public void setCollectionsReferences(List<CollectionsReference> collectionsReferences) {
		this.collectionsReferences = collectionsReferences;
	}

	public CollectionsReference addCollectionsReference(CollectionsReference collectionsReference) {
		getCollectionsReferences().add(collectionsReference);
		collectionsReference.setCollectionReference(this);
		return collectionsReference;
	}

	public CollectionsReference removeCollectionsReference(CollectionsReference collectionsReference) {
		getCollectionsReferences().remove(collectionsReference);
		collectionsReference.setCollectionReference(null);
		return collectionsReference;
	}

}
