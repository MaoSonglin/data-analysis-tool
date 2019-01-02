package dat.domain;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Deprecated
public class GraphColumn implements Serializable{

	private static final long serialVersionUID = -1920316107905930313L;

	@Id @GeneratedValue
	private Long id;
	
	@ManyToOne(targetEntity=GraphInfo.class,fetch=FetchType.EAGER)
	@JoinColumn(referencedColumnName="id")
	private GraphInfo graph;
	
	@OneToOne(targetEntity=VirtualColumn.class,fetch=FetchType.EAGER)
	private VirtualColumn column;
	
	private String type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GraphInfo getGraph() {
		return graph;
	}

	public void setGraph(GraphInfo graph) {
		this.graph = graph;
	}

	public VirtualColumn getColumn() {
		return column;
	}

	public void setColumn(VirtualColumn column) {
		this.column = column;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
