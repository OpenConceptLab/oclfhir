package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the localized_texts database table.
 * @author hp11
 */
@Entity
@Table(name="localized_texts")
public class LocalizedText extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="created_at")
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column(name="external_id")
	private String externalId;

	@Column(name="internal_reference_id")
	private String internalReferenceId;

	@Column
	private String locale;

	@Column(name="locale_preferred")
	private Boolean localePreferred;

	@Column
	private String name;

	@Column
	private String type;

	@OneToMany(mappedBy="localizedtext")
	private List<ConceptsDescription> conceptsDescriptions;

	@OneToMany(mappedBy="localizedtext")
	private List<ConceptsName> conceptsNames;

	public LocalizedText() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getInternalReferenceId() {
		return this.internalReferenceId;
	}

	public void setInternalReferenceId(String internalReferenceId) {
		this.internalReferenceId = internalReferenceId;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Boolean getLocalePreferred() {
		return this.localePreferred;
	}

	public void setLocalePreferred(Boolean localePreferred) {
		this.localePreferred = localePreferred;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ConceptsDescription> getConceptsDescriptions() {
		return this.conceptsDescriptions;
	}

	public void setConceptsDescriptions(List<ConceptsDescription> conceptsDescriptions) {
		this.conceptsDescriptions = conceptsDescriptions;
	}

	public ConceptsDescription addConceptsDescription(ConceptsDescription conceptsDescription) {
		getConceptsDescriptions().add(conceptsDescription);
		conceptsDescription.setLocalizedText(this);
		return conceptsDescription;
	}

	public ConceptsDescription removeConceptsDescription(ConceptsDescription conceptsDescription) {
		getConceptsDescriptions().remove(conceptsDescription);
		conceptsDescription.setLocalizedText(null);
		return conceptsDescription;
	}

	public List<ConceptsName> getConceptsNames() {
		return this.conceptsNames;
	}

	public void setConceptsNames(List<ConceptsName> conceptsNames) {
		this.conceptsNames = conceptsNames;
	}

	public ConceptsName addConceptsName(ConceptsName conceptsName) {
		getConceptsNames().add(conceptsName);
		conceptsName.setLocalizedText(this);
		return conceptsName;
	}

	public ConceptsName removeConceptsName(ConceptsName conceptsName) {
		getConceptsNames().remove(conceptsName);
		conceptsName.setLocalizedText(null);
		return conceptsName;
	}

}
