package dat.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据包中的数据表包含的虚拟字段类
 */ 
@Entity
public class VirtualColumn {
	
	@Id @Column(length=18)
	private String id;
	
	private String name;
	
	private String chinese;
	
	private Integer state;
	
	private String formula;
	
	private String  typeName;
	/**
	 * 该虚拟字段相关联的实际字段
	 */
	@OneToMany(targetEntity=PortionColumn.class,fetch=FetchType.LAZY,cascade={CascadeType.REMOVE,CascadeType.PERSIST})
	@JoinColumn(referencedColumnName="id")
	@JsonIgnore
	private List<PortionColumn> refColumns;

	/**
	 * 包含该虚拟字段的虚拟数据表
	 */
	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
	@JsonIgnore
	private VirtualTable table;
	public String getId() {
		return id;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setId(){
		this.id = StrUtil.generatorId();
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChinese() {
		return chinese;
	}

	

	

	public VirtualTable getTable() {
		return table;
	}

	public void setTable(VirtualTable table) {
		this.table = table;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public List<PortionColumn> getRefColumns() {
		if(refColumns == null)
			refColumns = new ArrayList<>();
		return refColumns;
	}

	public void setRefColumns(List<PortionColumn> refColumns) {
		this.refColumns = refColumns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualColumn other = (VirtualColumn) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	
}
