package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the user_profiles_organizations database table.
 * @author hp11
 */
@Entity
@Table(name="user_profiles_organizations")
public class UserProfilesOrganization extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Organization organization;

	@ManyToOne
	private UserProfile userprofile;

	public UserProfilesOrganization() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public UserProfile getUserProfile() {
		return this.userprofile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userprofile = userProfile;
	}

}
