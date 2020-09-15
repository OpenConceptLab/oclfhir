package org.openconceptlab.fhir.model;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the organizations database table.
 * @author hp11
 */
@Entity
@Table(name="organizations")
@NamedQuery(name="Organization.findAll", query="SELECT o FROM Organization o")
public class Organization extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="ORGANIZATIONS_ID_GENERATOR", sequenceName="ORGANIZATIONS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ORGANIZATIONS_ID_GENERATOR")
	private Long id;

	@Column
	private String company;

	@Column(name="created_at")
	private Timestamp createdAt;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private String extras;

	@Column(name="internal_reference_id")
	private String internalReferenceId;

	@Column(name="is_active")
	private Boolean isActive;

	@Column
	private String location;

	@Column
	private String mnemonic;

	@Column
	private String name;

	@Column(name="public_access")
	private String publicAccess;

	@Column(name="updated_at")
	private Timestamp updatedAt;

	@Column
	private String uri;

	@Column
	private String website;

	@OneToMany(mappedBy="organization", fetch = FetchType.LAZY)
	private List<Collection> collections;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="created_by_id")
	private UserProfile createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="updated_by_id")
	private UserProfile updatedBy;

	@OneToMany(mappedBy="organization")
	private List<Source> sources;

	@OneToMany(mappedBy="organization")
	private List<UserProfilesOrganization> userProfilesOrganizations;

	public Organization() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
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

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public List<Collection> getCollections() {
		return this.collections;
	}

	public void setCollections(List<Collection> collections) {
		this.collections = collections;
	}

	public Collection addCollection(Collection collection) {
		getCollections().add(collection);
		collection.setOrganization(this);
		return collection;
	}

	public Collection removeCollection(Collection collection) {
		getCollections().remove(collection);
		collection.setOrganization(null);
		return collection;
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

	public List<Source> getSources() {
		return this.sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}

	public Source addSource(Source source) {
		getSources().add(source);
		source.setOrganization(this);
		return source;
	}

	public Source removeSource(Source source) {
		getSources().remove(source);
		source.setOrganization(null);
		return source;
	}

	public List<UserProfilesOrganization> getUserProfilesOrganizations() {
		return this.userProfilesOrganizations;
	}

	public void setUserProfilesOrganizations(List<UserProfilesOrganization> userProfilesOrganizations) {
		this.userProfilesOrganizations = userProfilesOrganizations;
	}

	public UserProfilesOrganization addUserProfilesOrganization(UserProfilesOrganization userProfilesOrganization) {
		getUserProfilesOrganizations().add(userProfilesOrganization);
		userProfilesOrganization.setOrganization(this);
		return userProfilesOrganization;
	}

	public UserProfilesOrganization removeUserProfilesOrganization(UserProfilesOrganization userProfilesOrganization) {
		getUserProfilesOrganizations().remove(userProfilesOrganization);
		userProfilesOrganization.setOrganization(null);
		return userProfilesOrganization;
	}

}
