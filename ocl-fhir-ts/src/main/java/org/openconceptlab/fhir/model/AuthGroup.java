package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the auth_group database table.
 * @author harpatel1
 */
@Entity
@Table(name="auth_group")
public class AuthGroup extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String name;

	@OneToMany(mappedBy="authGroup")
	private List<AuthGroupPermission> authGroupPermissions;

	@OneToMany(mappedBy="authGroup")
	private List<UserProfilesGroup> userProfilesGroups;

	public AuthGroup() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
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
		authGroupPermission.setAuthGroup(this);
		return authGroupPermission;
	}

	public AuthGroupPermission removeAuthGroupPermission(AuthGroupPermission authGroupPermission) {
		getAuthGroupPermissions().remove(authGroupPermission);
		authGroupPermission.setAuthGroup(null);
		return authGroupPermission;
	}

	public List<UserProfilesGroup> getUserProfilesGroups() {
		return this.userProfilesGroups;
	}

	public void setUserProfilesGroups(List<UserProfilesGroup> userProfilesGroups) {
		this.userProfilesGroups = userProfilesGroups;
	}

	public UserProfilesGroup addUserProfilesGroup(UserProfilesGroup userProfilesGroup) {
		getUserProfilesGroups().add(userProfilesGroup);
		userProfilesGroup.setAuthGroup(this);
		return userProfilesGroup;
	}

	public UserProfilesGroup removeUserProfilesGroup(UserProfilesGroup userProfilesGroup) {
		getUserProfilesGroups().remove(userProfilesGroup);
		userProfilesGroup.setAuthGroup(null);
		return userProfilesGroup;
	}

}
