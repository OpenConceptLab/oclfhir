package org.openconceptlab.fhir.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the mappings database table.
 * @author harpatel1
 */
@Entity
@Table(name="mappings")
@TypeDefs({
		@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Mapping extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column
	private String comment;

	@Column(name="created_at")
	private Timestamp createdAt;

	@Column(name="custom_validation_schema")
	private String customValidationSchema;

	@Column(name="external_id")
	private String externalId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private String extras;

	@Column(name="internal_reference_id")
	private String internalReferenceId;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_latest_version")
	private Boolean isLatestVersion;

	@Column(name="map_type")
	private String mapType;

	@Column(name="public_access")
	private String publicAccess;

	@Column
	private Boolean released;

	@Column
	private Boolean retired;

	@Column(name="to_concept_code")
	private String toConceptCode;

	@Column(name="to_concept_name")
	private String toConceptName;

	@Column(name="updated_at")
	private Timestamp updatedAt;

	@Column
	private String uri;

	@Column
	private String version;

	@OneToMany(mappedBy="mapping")
	private List<CollectionsMapping> collectionsMappings;

	@ManyToOne
	@JoinColumn(name="from_concept_id")
	private Concept fromConcept;

	@ManyToOne
	@JoinColumn(name="to_concept_id")
	private Concept toConcept;

	@ManyToOne
	@JoinColumn(name="versioned_object_id")
	private Mapping versionedObject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private Source parent;

	@ManyToOne
	@JoinColumn(name="to_source_id")
	private Source toSource;

	@ManyToOne
	@JoinColumn(name="created_by_id")
	private UserProfile createdBy;

	@ManyToOne
	@JoinColumn(name="updated_by_id")
	private UserProfile updatedBy;

	@OneToMany(mappedBy="mapping", fetch = FetchType.LAZY)
	private List<MappingsSource> mappingsSources;

	public Mapping() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getCustomValidationSchema() {
		return this.customValidationSchema;
	}

	public void setCustomValidationSchema(String customValidationSchema) {
		this.customValidationSchema = customValidationSchema;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExtras() {
		return this.extras;
	}

	public void setExtras(String extras) {
		this.extras = extras;
	}

	public String getInternalReferenceId() {
		return this.internalReferenceId;
	}

	public void setInternalReferenceId(String internalReferenceId) {
		this.internalReferenceId = internalReferenceId;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsLatestVersion() {
		return this.isLatestVersion;
	}

	public void setIsLatestVersion(Boolean isLatestVersion) {
		this.isLatestVersion = isLatestVersion;
	}

	public String getMapType() {
		return this.mapType;
	}

	public void setMapType(String mapType) {
		this.mapType = mapType;
	}

	public String getPublicAccess() {
		return this.publicAccess;
	}

	public void setPublicAccess(String publicAccess) {
		this.publicAccess = publicAccess;
	}

	public Boolean getReleased() {
		return this.released;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public Boolean getRetired() {
		return this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	public String getToConceptCode() {
		return this.toConceptCode;
	}

	public void setToConceptCode(String toConceptCode) {
		this.toConceptCode = toConceptCode;
	}

	public String getToConceptName() {
		return this.toConceptName;
	}

	public void setToConceptName(String toConceptName) {
		this.toConceptName = toConceptName;
	}

	public Timestamp getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<CollectionsMapping> getCollectionsMappings() {
		return this.collectionsMappings;
	}

	public void setCollectionsMappings(List<CollectionsMapping> collectionsMappings) {
		this.collectionsMappings = collectionsMappings;
	}

	public CollectionsMapping addCollectionsMapping(CollectionsMapping collectionsMapping) {
		getCollectionsMappings().add(collectionsMapping);
		collectionsMapping.setMapping(this);
		return collectionsMapping;
	}

	public CollectionsMapping removeCollectionsMapping(CollectionsMapping collectionsMapping) {
		getCollectionsMappings().remove(collectionsMapping);
		collectionsMapping.setMapping(null);
		return collectionsMapping;
	}

	public Concept getFromConcept() {
		return this.fromConcept;
	}

	public void setFromConcept(Concept concept1) {
		this.fromConcept = concept1;
	}

	public Concept getToConcept() {
		return this.toConcept;
	}

	public void setToConcept(Concept concept2) {
		this.toConcept = concept2;
	}

	public Mapping getVersionedObject() {
		return this.versionedObject;
	}

	public void setVersionedObject(Mapping mapping) {
		this.versionedObject = mapping;
	}

	public Source getParent() {
		return this.parent;
	}

	public void setParent(Source source1) {
		this.parent = source1;
	}

	public Source getToSource() {
		return this.toSource;
	}

	public void setToSource(Source source2) {
		this.toSource = source2;
	}

	public UserProfile getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(UserProfile userProfile1) {
		this.createdBy = userProfile1;
	}

	public UserProfile getUpdatedBy() {
		return this.updatedBy;
	}

	public void setUpdatedBy(UserProfile userProfile2) {
		this.updatedBy = userProfile2;
	}

	public List<MappingsSource> getMappingsSources() {
		return this.mappingsSources;
	}

	public void setMappingsSources(List<MappingsSource> mappingsSources) {
		this.mappingsSources = mappingsSources;
	}

	public MappingsSource addMappingsSource(MappingsSource mappingsSource) {
		getMappingsSources().add(mappingsSource);
		mappingsSource.setMapping(this);
		return mappingsSource;
	}

	public MappingsSource removeMappingsSource(MappingsSource mappingsSource) {
		getMappingsSources().remove(mappingsSource);
		mappingsSource.setMapping(null);
		return mappingsSource;
	}

}
