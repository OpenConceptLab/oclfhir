package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the user_profiles_groups database table.
 * @author hp11
 */
@Entity
@Table(name="user_profiles_groups")
@NamedQuery(name="UserProfilesGroup.findAll", query="SELECT u FROM UserProfilesGroup u")
public class UserProfilesGroup extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="USER_PROFILES_GROUPS_ID_GENERATOR", sequenceName="USER_PROFILES_GROUPS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="USER_PROFILES_GROUPS_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	@JoinColumn(name="group_id")
	private AuthGroup authGroup;

	@ManyToOne
	private UserProfile userprofile;

	public UserProfilesGroup() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public AuthGroup getAuthGroup() {
		return this.authGroup;
	}

	public void setAuthGroup(AuthGroup authGroup) {
		this.authGroup = authGroup;
	}

	public UserProfile getUserProfile() {
		return this.userprofile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userprofile = userProfile;
	}

}
