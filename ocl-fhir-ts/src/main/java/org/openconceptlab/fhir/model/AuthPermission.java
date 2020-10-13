package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the auth_permission database table.
 * @author harpatel1
 */
@Entity
@Table(name="auth_permission")
public class AuthPermission extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String codename;

	@Column
	private String name;

	@ManyToOne
	@JoinColumn(name="content_type_id")
	private DjangoContentType djangoContentType;

	@OneToMany(mappedBy="authPermission")
	private List<AuthGroupPermission> authGroupPermissions;

	@OneToMany(mappedBy="authPermission")
	private List<UserProfilesUserPermission> userProfilesUserPermissions;

	public AuthPermission() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodename() {
		return this.codename;
	}

	public void setCodename(String codename) {
		this.codename = codename;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AuthGroupPermission> getAuthGroupPermissions() {
		return this.authGroupPermissions;
	}

	public void setAuthGroupPermissions(List<AuthGroupPermission> authGroupPermissions) {
		this.authGroupPermissions = authGroupPermissions;
	}

	public AuthGroupPermission addAuthGroupPermission(AuthGroupPermission authGroupPermission) {
		getAuthGroupPermissions().add(authGroupPermission);
		authGroupPermission.setAuthPermission(this);
		return authGroupPermission;
	}

	public AuthGroupPermission removeAuthGroupPermission(AuthGroupPermission authGroupPermission) {
		getAuthGroupPermissions().remove(authGroupPermission);
		authGroupPermission.setAuthPermission(null);
		return authGroupPermission;
	}

	public DjangoContentType getDjangoContentType() {
		return this.djangoContentType;
	}

	public void setDjangoContentType(DjangoContentType djangoContentType) {
		this.djangoContentType = djangoContentType;
	}

	public List<UserProfilesUserPermission> getUserProfilesUserPermissions() {
		return this.userProfilesUserPermissions;
	}

	public void setUserProfilesUserPermissions(List<UserProfilesUserPermission> userProfilesUserPermissions) {
		this.userProfilesUserPermissions = userProfilesUserPermissions;
	}

	public UserProfilesUserPermission addUserProfilesUserPermission(UserProfilesUserPermission userProfilesUserPermission) {
		getUserProfilesUserPermissions().add(userProfilesUserPermission);
		userProfilesUserPermission.setAuthPermission(this);
		return userProfilesUserPermission;
	}

	public UserProfilesUserPermission removeUserProfilesUserPermission(UserProfilesUserPermission userProfilesUserPermission) {
		getUserProfilesUserPermissions().remove(userProfilesUserPermission);
		userProfilesUserPermission.setAuthPermission(null);
		return userProfilesUserPermission;
	}

}
