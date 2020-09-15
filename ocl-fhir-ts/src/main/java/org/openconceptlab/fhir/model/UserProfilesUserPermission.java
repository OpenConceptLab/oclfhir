package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the user_profiles_user_permissions database table.
 * @author hp11
 */
@Entity
@Table(name="user_profiles_user_permissions")
@NamedQuery(name="UserProfilesUserPermission.findAll", query="SELECT u FROM UserProfilesUserPermission u")
public class UserProfilesUserPermission extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="USER_PROFILES_USER_PERMISSIONS_ID_GENERATOR", sequenceName="USER_PROFILES_USER_PERMISSIONS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="USER_PROFILES_USER_PERMISSIONS_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	@JoinColumn(name="permission_id")
	private AuthPermission authPermission;

	@ManyToOne
	private UserProfile userprofile;

	public UserProfilesUserPermission() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public AuthPermission getAuthPermission() {
		return this.authPermission;
	}

	public void setAuthPermission(AuthPermission authPermission) {
		this.authPermission = authPermission;
	}

	public UserProfile getUserProfile() {
		return this.userprofile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userprofile = userProfile;
	}

}
