package org.openconceptlab.fhir.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the authtoken_token database table.
 * @author harpatel1
 */
@Entity
@Table(name="authtoken_token")
public class AuthtokenToken extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String key;

	@Column
	private Timestamp created;

	@ManyToOne
	@JoinColumn(name="user_id")
	private UserProfile userProfile;

	public AuthtokenToken() {
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public UserProfile getUserProfile() {
		return this.userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

}
