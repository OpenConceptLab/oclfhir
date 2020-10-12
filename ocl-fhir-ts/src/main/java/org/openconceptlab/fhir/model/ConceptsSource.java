package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the concepts_sources database table.
 * @author hp11
 */
@Entity
@Table(name="concepts_sources")
public class ConceptsSource extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Concept concept;

	@ManyToOne
	private Source source;

	public ConceptsSource() {
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

	public Source getSource() {
		return this.source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

}
