package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the concepts_names database table.
 * @author hp11
 */
@Entity
@Table(name="concepts_names")
@NamedQuery(name="ConceptsName.findAll", query="SELECT c FROM ConceptsName c")
public class ConceptsName extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="CONCEPTS_NAMES_ID_GENERATOR", sequenceName="CONCEPTS_NAMES_ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CONCEPTS_NAMES_ID_GENERATOR")
	private Integer id;

	@ManyToOne
	private Concept concept;

	@ManyToOne
	private LocalizedText localizedtext;

	public ConceptsName() {
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
