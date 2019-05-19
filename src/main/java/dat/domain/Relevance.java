package dat.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Relevance implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7788994821896669153L;

	@Id
	@GeneratedValue
	private Long id;
	
	private Float strong;
	
	@OneToOne(targetEntity=VirtualColumn.class)
	private VirtualColumn column1;
	
	@OneToOne(targetEntity=VirtualColumn.class)
	private VirtualColumn column2;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VirtualColumn getColumn1() {
		return column1;
	}

	public void setColumn1(VirtualColumn column1) {
		this.column1 = column1;
	}

	public VirtualColumn getColumn2() {
		return column2;
	}

	public void setColumn2(VirtualColumn column2) {
		this.column2 = column2;
	}
	
	public List<VirtualColumn> getColumns(){
		ArrayList<VirtualColumn> arrayList = new ArrayList<>();
		arrayList.add(getColumn1());
		arrayList.add(getColumn2());
		return arrayList;
	}

	public Float getStrong() {
		return strong;
	}

	public void setStrong(Float strong) {
		this.strong = strong;
	}
	
	public VirtualTable getTable1(){
		VirtualColumn virtualColumn = getColumn1();
		if(virtualColumn != null)
		return virtualColumn.getTable();
		return null;
	}
	
	public VirtualTable getTable2(){
		VirtualColumn virtualColumn = getColumn2();
		if(virtualColumn != null)
		return virtualColumn.getTable();
		return null;
	}
	
	
}
