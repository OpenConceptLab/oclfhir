package org.openconceptlab.fhir.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the sources database table.
 * @author harpatel1
 */
@Entity
@Table(name="sources")
public class Source extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "revision_date")
	private Date revisionDate;

	@Column(name="created_at")
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column(name="custom_validation_schema")
	private String customValidationSchema;

	@Column(name="default_locale")
	private String defaultLocale;

	@Column
	private String description;

	@Column(name="external_id")
	private String externalId;

	@Type(type = "jsonb")
	@Column(name = "extras", columnDefinition = "jsonb")
	private String extras;

	@Column(name="full_name")
	private String fullName;

	@Column(name="internal_reference_id")
	private String internalReferenceId = "0";

	@Column(name="is_active")
	private boolean isActive;

	@Column(name="is_latest_version")
	private Boolean isLatestVersion = true;

	@Column
	private String mnemonic;

	@Column
	private String name = "N/A";

	@Column(name="public_access")
	private String publicAccess = "View";

	@Column
	private Boolean released = false;

	@Column
	private Boolean retired;

	@Column(name="source_type")
	private String sourceType;

	@Column(name="updated_at")
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

	@Column
	private String uri;

	@Column
	private String version = "HEAD";

	@Column
	private String website;

	@Column(name = "canonical_url")
	private String canonicalUrl;

	@Column
	private Boolean experimental;

	@Column(name = "case_sensitive")
	private Boolean caseSensitive;

	@Column(name = "collection_reference")
	private String collectionReference;

	@Column(name = "hierarchy_meaning")
	private String hierarchyMeaning;

	@Column
	private Boolean compositional;

	@Column(name = "version_needed")
	private Boolean versionNeeded;

	@OneToMany(mappedBy="parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Concept> concepts;

	@OneToMany(mappedBy="parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Mapping> mappings;

	@OneToMany(mappedBy="source", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ConceptsSource> conceptsSources;

	@OneToMany(mappedBy="source", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<MappingsSource> mappingsSources;

	@ManyToOne
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

	public Source() {
	}

	public Source(Long id) {
		this.id = id;
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(Date revisionDate) {
		this.revisionDate = revisionDate;
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

	public String getInternalReferenceId() {
		return this.internalReferenceId;
	}

	public void setInternalReferenceId(String internalReferenceId) {
		this.internalReferenceId = internalReferenceId;
	}

	public boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(boolean isActive) {
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
		return this.retired != null && this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	public String getSourceType() {
		return this.sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
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

	public List<Concept> getConcepts() {
		if (this.concepts == null)
			this.concepts = new ArrayList<>();
		return this.concepts;
	}

	public String getCanonicalUrl() {
		return this.canonicalUrl;
	}

	public void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	public Concept addConcept(Concept concept) {
		getConcepts().add(concept);
		concept.setParent(this);

		return concept;
	}

	public Concept removeConcept(Concept concept) {
		getConcepts().remove(concept);
		concept.setParent(null);

		return concept;
	}

	public List<Mapping> getMappings() {
		if (this.mappings == null)
			this.mappings = new ArrayList<>();
		return this.mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}

	public Mapping addMapping(Mapping mapping) {
		getMappings().add(mapping);
		mapping.setParent(this);
		return mapping;
	}

	public Mapping removeMapping(Mapping mapping) {
		getMappings().remove(mapping);
		mapping.setParent(null);
		return mapping;
	}

	public List<ConceptsSource> getConceptsSources() {
		if (this.conceptsSources == null)
			this.conceptsSources = new ArrayList<>();
		return this.conceptsSources;
	}

	public void setConceptsSources(List<ConceptsSource> conceptsSources) {
		this.conceptsSources = conceptsSources;
	}

	public ConceptsSource addConceptsSource(ConceptsSource conceptsSource) {
		getConceptsSources().add(conceptsSource);
		return conceptsSource;
	}

	public ConceptsSource removeConceptsSource(ConceptsSource conceptsSource) {
		getConceptsSources().remove(conceptsSource);
		return conceptsSource;
	}

	public List<MappingsSource> getMappingsSources() {
		if (this.mappingsSources == null)
			this.mappingsSources = new ArrayList<>();
		return this.mappingsSources;
	}

	public void setMappingsSources(List<MappingsSource> mappingsSources) {
		this.mappingsSources = mappingsSources;
	}

	public MappingsSource addConceptsSource(MappingsSource mappingsSource) {
		getMappingsSources().add(mappingsSource);
		return mappingsSource;
	}

	public MappingsSource removeConceptsSource(MappingsSource mappingsSource) {
		getMappingsSources().remove(mappingsSource);
		return mappingsSource;
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

	public void setCreatedBy(UserProfile userProfile1) {
		this.createdBy = userProfile1;
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

	public void setUserId(UserProfile userProfile3) {
		this.userId = userProfile3;
	}

	public Boolean isExperimental() {
		return experimental;
	}

	public void setExperimental(boolean experimental) {
		this.experimental = experimental;
	}

	public Boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getCollectionReference() {
		return collectionReference;
	}

	public void setCollectionReference(String collectionReference) {
		this.collectionReference = collectionReference;
	}

	public String getHierarchyMeaning() {
		return hierarchyMeaning;
	}

	public void setHierarchyMeaning(String hierarchyMeaning) {
		this.hierarchyMeaning = hierarchyMeaning;
	}

	public Boolean isCompositional() {
		return compositional;
	}

	public void setCompositional(boolean compositional) {
		this.compositional = compositional;
	}

	public Boolean isVersionNeeded() {
		return versionNeeded;
	}

	public void setVersionNeeded(boolean versionNeeded) {
		this.versionNeeded = versionNeeded;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Source source = (Source) o;
		return new EqualsBuilder().append(id, source.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(id).toHashCode();
	}
}
