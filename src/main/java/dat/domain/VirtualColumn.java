package dat.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据包中的数据表包含的虚拟字段类
 */ 
@Entity
public class VirtualColumn implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8263945867735514210L;

	@Id 
	private String id;
	
	private String name;
	
	private String chinese;
	
	private Integer state;
	
	private String formula;
	
	private String  typeName;
	
	/**
	 * 字段备注
	 */
	private String remask;
	
	@OneToOne(targetEntity=Reference.class,fetch=FetchType.LAZY,mappedBy="foreignColumn")
	@JsonIgnore
	private Reference reference;
	
	@OneToMany(targetEntity=Reference.class,fetch=FetchType.LAZY,mappedBy="referencedColumn")
	@JsonIgnore
	private List<Reference> referencedBy;
	
//	@OneToOne(fetch=FetchType.EAGER,targetEntity=VirtualColumn.class)
//	@JoinColumn(referencedColumnName="id")
//	private VirtualColumn relation;
//	
	/**
	 * 该虚拟字段相关联的实际字段
	 */
	@ManyToMany(targetEntity=TableColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnore
	private List<TableColumn> refColumns;

	/**
	 * 包含该虚拟字段的虚拟数据表
	 */
	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.REFRESH/*,CascadeType.MERGE*/})
	@JoinColumn(referencedColumnName="id")
//	@JsonIgnore
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
		String id = StrUtil.generatorId();
		setId("VC"+id.substring(2));
	}
	public String getName() {
		return name;
	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public List<Reference> getReferencedBy() {
		return referencedBy;
	}

	public void setReferencedBy(List<Reference> referencedBy) {
		this.referencedBy = referencedBy;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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

	public List<TableColumn> getRefColumns() {
		if(refColumns == null)
			refColumns = new ArrayList<>();
		return refColumns;
	}

	public void setRefColumns(List<TableColumn> refColumns) {
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

	@Override
	public String toString() {
		return "VirtualColumn [id=" + id + ", name=" + name + ", chinese="
				+ chinese + ", state=" + state + ", formula=" + formula
				+ ", typeName=" + typeName + ", remask=" + remask + ", table="
				+ table + "]";
	}

	public String getRemask() {
		return remask;
	}

	public void setRemask(String remask) {
		this.remask = remask;
	}

	
	public String getRela(){
		Reference reference = getReference();
		if(reference != null){
			VirtualTable virtualTable = reference.getReferencedTable();
			String chinese = virtualTable.getChinese();
			String tableName = chinese == null ? virtualTable.getName() : chinese;
			VirtualColumn virtualColumn = reference.getReferencedColumn();
			chinese = virtualColumn.getChinese();
			String columnName = chinese == null ? virtualColumn.getName() : chinese;
			return tableName+"."+columnName;
		}
		return null;
	}
}
