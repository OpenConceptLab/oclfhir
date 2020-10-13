package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the concepts_descriptions database table.
 * @author harpatel1
 */
@Entity
@Table(name="concepts_descriptions")
public class ConceptsDescription extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
