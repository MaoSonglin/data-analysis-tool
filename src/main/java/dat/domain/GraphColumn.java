package dat.domain;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Data;

@Deprecated
@Data
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
	
}
