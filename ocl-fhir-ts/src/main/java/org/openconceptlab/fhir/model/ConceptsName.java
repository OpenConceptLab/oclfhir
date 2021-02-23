package org.openconceptlab.fhir.model;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the concepts_names database table.
 * @author harpatel1
 */
@Entity
@Table(name="concepts_names")
public class ConceptsName extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Concept concept;

	@ManyToOne(cascade = CascadeType.ALL)
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
