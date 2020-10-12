package org.openconceptlab.fhir.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the mappings_sources database table.
 * @author hp11
 */
@Entity
@Table(name="mappings_sources")
public class MappingsSource extends BaseOclEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Mapping mapping;

	@ManyToOne
	private Source source;

	public MappingsSource() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Mapping getMapping() {
		return this.mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public Source getSource() {
		return this.source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

}
