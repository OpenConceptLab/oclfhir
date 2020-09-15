package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the authtoken_token database table.
 * @author hp11
 */
@Entity
@Table(name="authtoken_token")
@NamedQuery(name="AuthtokenToken.findAll", query="SELECT a FROM AuthtokenToken a")
public class AuthtokenToken extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="AUTHTOKEN_TOKEN_KEY_GENERATOR", sequenceName="AUTHTOKEN_TOKEN_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="AUTHTOKEN_TOKEN_KEY_GENERATOR")
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
