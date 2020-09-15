package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the concepts_descriptions database table.
 * @author hp11
 */
@Entity
@Table(name="concepts_descriptions")
@NamedQuery(name="ConceptsDescription.findAll", query="SELECT c FROM ConceptsDescription c")
public class ConceptsDescription extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="CONCEPTS_DESCRIPTIONS_ID_GENERATOR", sequenceName="CONCEPTS_DESCRIPTIONS_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CONCEPTS_DESCRIPTIONS_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	private Concept concept;

	@ManyToOne
	private LocalizedText localizedtext;

	public ConceptsDescription() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public LocalizedText getLocalizedText() {
		return this.localizedtext;
	}

	public void setLocalizedText(LocalizedText localizedText) {
		this.localizedtext = localizedText;
	}

}
