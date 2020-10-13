package org.openconceptlab.fhir.model;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the user_profiles database table.
 * @author harpatel1
 */
@Entity
@Table(name="user_profiles")
public class UserProfile extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column
	private String company;

	@Column(name="created_at")
	private Timestamp createdAt;

	@Column(name="date_joined")
	private Timestamp dateJoined;

	@Column
	private String email;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private String extras;

	@Column(name="first_name")
	private String firstName;

	@Column(name="internal_reference_id")
	private String internalReferenceId;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_staff")
	private Boolean isStaff;

	@Column(name="is_superuser")
	private Boolean isSuperuser;

	@Column(name="last_login")
	private Timestamp lastLogin;

	@Column(name="last_name")
	private String lastName;

	@Column
	private String location;

	@Column
	private String password;

	@Column(name="preferred_locale")
	private String preferredLocale;

	@Column(name="public_access")
	private String publicAccess;

	@Column(name="updated_at")
	private Timestamp updatedAt;

	@Column
	private String uri;

	@Column
	private String username;

	@OneToMany(mappedBy="userProfile")
	private List<AuthtokenToken> authtokenTokens;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="created_by_id")
	private UserProfile createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="updated_by_id")
	private UserProfile updatedBy;

	@OneToMany(mappedBy="userprofile")
	private List<UserProfilesGroup> userProfilesGroups;

	@OneToMany(mappedBy="userprofile")
	private List<UserProfilesOrganization> userProfilesOrganizations;

	@OneToMany(mappedBy="userprofile")
	private List<UserProfilesUserPermission> userProfilesUserPermissions;

	public UserProfile() {
	}

	public UserProfile(Long id) {
		this.id = id;
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

	public Timestamp getDateJoined() {
		return this.dateJoined;
	}

	public void setDateJoined(Timestamp dateJoined) {
		this.dateJoined = dateJoined;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getExtras() {
		return this.extras;
	}

	public void setExtras(String extras) {
		this.extras = extras;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
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

	public Boolean getIsStaff() {
		return this.isStaff;
	}

	public void setIsStaff(Boolean isStaff) {
		this.isStaff = isStaff;
	}

	public Boolean getIsSuperuser() {
		return this.isSuperuser;
	}

	public void setIsSuperuser(Boolean isSuperuser) {
		this.isSuperuser = isSuperuser;
	}

	public Timestamp getLastLogin() {
		return this.lastLogin;
	}

	public void setLastLogin(Timestamp lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPreferredLocale() {
		return this.preferredLocale;
	}

	public void setPreferredLocale(String preferredLocale) {
		this.preferredLocale = preferredLocale;
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

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<AuthtokenToken> getAuthtokenTokens() {
		return this.authtokenTokens;
	}

	public void setAuthtokenTokens(List<AuthtokenToken> authtokenTokens) {
		this.authtokenTokens = authtokenTokens;
	}

	public AuthtokenToken addAuthtokenToken(AuthtokenToken authtokenToken) {
		getAuthtokenTokens().add(authtokenToken);
		authtokenToken.setUserProfile(this);
		return authtokenToken;
	}

	public AuthtokenToken removeAuthtokenToken(AuthtokenToken authtokenToken) {
		getAuthtokenTokens().remove(authtokenToken);
		authtokenToken.setUserProfile(null);
		return authtokenToken;
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

	public List<UserProfilesGroup> getUserProfilesGroups() {
		return this.userProfilesGroups;
	}

	public void setUserProfilesGroups(List<UserProfilesGroup> userProfilesGroups) {
		this.userProfilesGroups = userProfilesGroups;
	}

	public UserProfilesGroup addUserProfilesGroup(UserProfilesGroup userProfilesGroup) {
		getUserProfilesGroups().add(userProfilesGroup);
		userProfilesGroup.setUserProfile(this);
		return userProfilesGroup;
	}

	public UserProfilesGroup removeUserProfilesGroup(UserProfilesGroup userProfilesGroup) {
		getUserProfilesGroups().remove(userProfilesGroup);
		userProfilesGroup.setUserProfile(null);
		return userProfilesGroup;
	}

	public List<UserProfilesOrganization> getUserProfilesOrganizations() {
		return this.userProfilesOrganizations;
	}

	public void setUserProfilesOrganizations(List<UserProfilesOrganization> userProfilesOrganizations) {
		this.userProfilesOrganizations = userProfilesOrganizations;
	}

	public UserProfilesOrganization addUserProfilesOrganization(UserProfilesOrganization userProfilesOrganization) {
		getUserProfilesOrganizations().add(userProfilesOrganization);
		userProfilesOrganization.setUserProfile(this);
		return userProfilesOrganization;
	}

	public UserProfilesOrganization removeUserProfilesOrganization(UserProfilesOrganization userProfilesOrganization) {
		getUserProfilesOrganizations().remove(userProfilesOrganization);
		userProfilesOrganization.setUserProfile(null);
		return userProfilesOrganization;
	}

	public List<UserProfilesUserPermission> getUserProfilesUserPermissions() {
		return this.userProfilesUserPermissions;
	}

	public void setUserProfilesUserPermissions(List<UserProfilesUserPermission> userProfilesUserPermissions) {
		this.userProfilesUserPermissions = userProfilesUserPermissions;
	}

	public UserProfilesUserPermission addUserProfilesUserPermission(UserProfilesUserPermission userProfilesUserPermission) {
		getUserProfilesUserPermissions().add(userProfilesUserPermission);
		userProfilesUserPermission.setUserProfile(this);
		return userProfilesUserPermission;
	}

	public UserProfilesUserPermission removeUserProfilesUserPermission(UserProfilesUserPermission userProfilesUserPermission) {
		getUserProfilesUserPermissions().remove(userProfilesUserPermission);
		userProfilesUserPermission.setUserProfile(null);
		return userProfilesUserPermission;
	}

}
