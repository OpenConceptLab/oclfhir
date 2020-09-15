package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the django_content_type database table.
 * @author hp11
 */
@Entity
@Table(name="django_content_type")
@NamedQuery(name="DjangoContentType.findAll", query="SELECT d FROM DjangoContentType d")
public class DjangoContentType extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="DJANGO_CONTENT_TYPE_ID_GENERATOR", sequenceName="DJANGO_CONTENT_TYPE_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DJANGO_CONTENT_TYPE_ID_GENERATOR")
	private Integer id;

	@Column(name="app_label")
	private String appLabel;

	private String model;

	@OneToMany(mappedBy="djangoContentType")
	private List<AuthPermission> authPermissions;

	public DjangoContentType() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAppLabel() {
		return this.appLabel;
	}

	public void setAppLabel(String appLabel) {
		this.appLabel = appLabel;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<AuthPermission> getAuthPermissions() {
		return this.authPermissions;
	}

	public void setAuthPermissions(List<AuthPermission> authPermissions) {
		this.authPermissions = authPermissions;
	}

	public AuthPermission addAuthPermission(AuthPermission authPermission) {
		getAuthPermissions().add(authPermission);
		authPermission.setDjangoContentType(this);
		return authPermission;
	}

	public AuthPermission removeAuthPermission(AuthPermission authPermission) {
		getAuthPermissions().remove(authPermission);
		authPermission.setDjangoContentType(null);
		return authPermission;
	}
}
