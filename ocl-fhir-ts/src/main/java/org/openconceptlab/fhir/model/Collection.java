package org.openconceptlab.fhir.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the collections database table.
 * @author harpatel1
 */
@Entity
@Table(name="collections")
public class Collection extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Type(type = "jsonb")
	@Column(name="identifier", columnDefinition = "jsonb")
	private String identifier;

	@Column(name = "publisher")
	private String publisher;

	@Type(type = "jsonb")
	@Column(name="contact", columnDefinition = "jsonb")
	private String contact;

	@Type(type = "jsonb")
	@Column(name="jurisdiction", columnDefinition = "jsonb")
	private String jurisdiction;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "copyright")
	private String copyright;

	@Column(name = "immutable")
	private Boolean immutable;

	@Column(name = "revision_date")
	private Date revisionDate;

	@Column(name="collection_type")
	private String collectionType = "N/A";

	@Column(name="created_at")
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column(name="custom_resources_linked_source")
	private String customResourcesLinkedSource = "N/A";

	@Column(name="custom_validation_schema")
	private String customValidationSchema;

	@Column(name="default_locale")
	private String defaultLocale;

	@Column
	private String description;

	@Column(name="external_id")
	private String externalId;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private String extras;

	@Column(name="full_name")
	private String fullName;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_latest_version")
	private Boolean isLatestVersion;

	@Column
	private String mnemonic;

	@Column
	private String name = "N/A";

	@Column(name="preferred_source")
	private String preferredSource = "N/A";

	@Column(name="public_access")
	private String publicAccess;

	@Column
	private Boolean released;

	@Column(name="repository_type")
	private String repositoryType = "Collection";

	@Column
	private Boolean retired;

	@Column(name="updated_at")
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

	@Column
	private String uri;

	@Column
	private String version;

	@Column
	private String website;

	@Column(name = "canonical_url")
	private String canonicalUrl;

	@Column
	private Boolean experimental;

	@Column(name = "locked_date")
	private Timestamp lockedDate;

	@ManyToOne(fetch = FetchType.LAZY)
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="created_by_id")
	private UserProfile createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="updated_by_id")
	private UserProfile updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id")
	private UserProfile userId;

	@OneToMany(mappedBy="collection", cascade = CascadeType.ALL)
	private List<CollectionsConcept> collectionsConcepts;

	@OneToMany(mappedBy="collection", cascade = CascadeType.ALL)
	private List<CollectionsMapping> collectionsMappings;

	@OneToMany(mappedBy="collection", cascade = CascadeType.ALL)
	private List<CollectionsReference> collectionsreferences;

	@Column
	private String text;

	@Type(type = "jsonb")
	@Column(name="meta", columnDefinition = "jsonb")
	private String meta;

	public Collection() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getJurisdiction() {
		return jurisdiction;
	}

	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public Boolean getImmutable() {
		return this.immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(Date revisionDate) {
		this.revisionDate = revisionDate;
	}

	public String getCollectionType() {
		return this.collectionType;
	}

	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getCustomResourcesLinkedSource() {
		return this.customResourcesLinkedSource;
	}

	public void setCustomResourcesLinkedSource(String customResourcesLinkedSource) {
		this.customResourcesLinkedSource = customResourcesLinkedSource;
	}

	public String getCustomValidationSchema() {
		return this.customValidationSchema;
	}

	public void setCustomValidationSchema(String customValidationSchema) {
		this.customValidationSchema = customValidationSchema;
	}

	public String getDefaultLocale() {
		return this.defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getIsActive() {
		return this.isActive != null && this.isActive;
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

	public String getMnemonic() {
		return this.mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPreferredSource() {
		return this.preferredSource;
	}

	public void setPreferredSource(String preferredSource) {
		this.preferredSource = preferredSource;
	}

	public String getPublicAccess() {
		return this.publicAccess;
	}

	public void setPublicAccess(String publicAccess) {
		this.publicAccess = publicAccess;
	}

	public Boolean getReleased() {
		return this.released !=null && this.released;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public String getRepositoryType() {
		return this.repositoryType;
	}

	public void setRepositoryType(String repositoryType) {
		this.repositoryType = repositoryType;
	}

	public Boolean getRetired() {
		return this.retired != null && this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
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

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCanonicalUrl() {
		return canonicalUrl;
	}

	public void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public UserProfile getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(UserProfile createdBy) {
		this.createdBy = createdBy;
	}

	public UserProfile getUpdatedBy() {
		return this.updatedBy;
	}

	public void setUpdatedBy(UserProfile userProfile2) {
		this.updatedBy = userProfile2;
	}

	public UserProfile getUserId() {
		return this.userId;
	}

	public void setUserId(UserProfile userId) {
		this.userId = userId;
	}

	public List<CollectionsConcept> getCollectionsConcepts() {
		return this.collectionsConcepts;
	}

	public void setCollectionsConcepts(List<CollectionsConcept> collectionsConcepts) {
		this.collectionsConcepts = collectionsConcepts;
	}

	public CollectionsConcept addCollectionsConcept(CollectionsConcept collectionsConcept) {
		getCollectionsConcepts().add(collectionsConcept);
		collectionsConcept.setCollection(this);
		return collectionsConcept;
	}

	public CollectionsConcept removeCollectionsConcept(CollectionsConcept collectionsConcept) {
		getCollectionsConcepts().remove(collectionsConcept);
		collectionsConcept.setCollection(null);
		return collectionsConcept;
	}

	public List<CollectionsMapping> getCollectionsMappings() {
		return this.collectionsMappings;
	}

	public void setCollectionsMappings(List<CollectionsMapping> collectionsMappings) {
		this.collectionsMappings = collectionsMappings;
	}

	public CollectionsMapping addCollectionsMapping(CollectionsMapping collectionsMapping) {
		getCollectionsMappings().add(collectionsMapping);
		collectionsMapping.setCollection(this);
		return collectionsMapping;
	}

	public CollectionsMapping removeCollectionsMapping(CollectionsMapping collectionsMapping) {
		getCollectionsMappings().remove(collectionsMapping);
		collectionsMapping.setCollection(null);
		return collectionsMapping;
	}

	public List<CollectionsReference> getCollectionsReferences() {
		return this.collectionsreferences;
	}

	public void setCollectionsReferences(List<CollectionsReference> collectionsReferences) {
		this.collectionsreferences = collectionsReferences;
	}

	public CollectionsReference addCollectionsReference(CollectionsReference collectionsReference) {
		getCollectionsReferences().add(collectionsReference);
		collectionsReference.setCollection(this);
		return collectionsReference;
	}

	public CollectionsReference removeCollectionsReference(CollectionsReference collectionsReference) {
		getCollectionsReferences().remove(collectionsReference);
		collectionsReference.setCollection(null);
		return collectionsReference;
	}

	public Boolean isExperimental() {
		return experimental;
	}

	public void setExperimental(boolean experimental) {
		this.experimental = experimental;
	}

	public Timestamp getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(Timestamp lockedDate) {
		this.lockedDate = lockedDate;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}
}
