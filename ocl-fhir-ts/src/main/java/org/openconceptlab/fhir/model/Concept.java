package org.openconceptlab.fhir.model;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.hibernate.annotations.Type;
import org.openconceptlab.fhir.util.OclFhirUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the concepts database table.
 * @author harpatel1
 */
@Entity
@Table(name="concepts")
public class Concept extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String comment;

	@Column(name="concept_class")
	private String conceptClass = "N/A";

	@Column(name="created_at")
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column
	private String datatype = "N/A";

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
	private Boolean isLatestVersion = false;

	@Column
	private String mnemonic;

	@Column
	private String name;

	@Column(name="public_access")
	private String publicAccess;

	@Column
	private Boolean released = false;

	@Column
	private Boolean retired;

	@Type(type = "jsonb")
	@Column(name="supported_locales", columnDefinition = "jsonb")
	private String supportedLocales;

	@Column(name="updated_at")
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());;

	@Column
	private String uri;

	@Column
	private String version;

	@Column
	private String website;

	@ManyToOne
	@JoinColumn(name="versioned_object_id")
	private Concept versionedObject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private Source parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="created_by_id")
	private UserProfile createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="updated_by_id")
	private UserProfile updatedBy;

//	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy="concept", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ConceptsDescription> conceptsDescriptions = new ArrayList<>();

//	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy="concept", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ConceptsName> conceptsNames = new ArrayList<>();

	@OneToMany(mappedBy="concept", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ConceptsSource> conceptsSources = new ArrayList<>();

	public List<ConceptsSource> getConceptsSources() {
		return conceptsSources;
	}

	public void setConceptsSources(List<ConceptsSource> conceptsSources) {
		this.conceptsSources = conceptsSources;
	}

	public Concept() {
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

	public String getConceptClass() {
		return this.conceptClass;
	}

	public void setConceptClass(String conceptClass) {
		this.conceptClass = conceptClass;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getDatatype() {
		return this.datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
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

	public String getMnemonic() {
		return OclFhirUtil.decodeValue(this.mnemonic);
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
		return this.released != null ? this.released : false;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public Boolean getRetired() {
		if (this.retired == null) {
			this.retired = false;
		}
		return this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	public String getSupportedLocales() {
		return this.supportedLocales;
	}

	public void setSupportedLocales(String supportedLocales) {
		this.supportedLocales = supportedLocales;
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

	public Concept getVersionedObject() {
		return this.versionedObject;
	}

	public void setVersionedObject(Concept concept) {
		this.versionedObject = concept;
	}

	public Source getParent() {
		return this.parent;
	}

	public void setParent(Source source) {
		this.parent = source;
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

	public void setUpdatedBy(UserProfile updatedBy) {
		this.updatedBy = updatedBy;
	}

	public List<ConceptsDescription> getConceptsDescriptions() {
		if (this.conceptsDescriptions == null)
			this.conceptsDescriptions = new ArrayList<>();
		return this.conceptsDescriptions;
	}

	public void setConceptsDescriptions(List<ConceptsDescription> conceptsDescriptions) {
		this.conceptsDescriptions = conceptsDescriptions;
	}

	public ConceptsDescription addConceptsDescription(ConceptsDescription conceptsDescription) {
		getConceptsDescriptions().add(conceptsDescription);
		conceptsDescription.setConcept(this);
		return conceptsDescription;
	}

	public ConceptsDescription removeConceptsDescription(ConceptsDescription conceptsDescription) {
		getConceptsDescriptions().remove(conceptsDescription);
		conceptsDescription.setConcept(null);
		return conceptsDescription;
	}

	public List<ConceptsName> getConceptsNames() {
		if (this.conceptsNames == null)
			this.conceptsNames = new ArrayList<>();
		return this.conceptsNames;
	}

	public void setConceptsNames(List<ConceptsName> conceptsNames) {
		this.conceptsNames = conceptsNames;
	}

	public ConceptsName addConceptsName(ConceptsName conceptsName) {
		getConceptsNames().add(conceptsName);
		conceptsName.setConcept(this);
		return conceptsName;
	}

	public ConceptsName removeConceptsName(ConceptsName conceptsName) {
		getConceptsNames().remove(conceptsName);
		conceptsName.setConcept(null);
		return conceptsName;
	}
}
