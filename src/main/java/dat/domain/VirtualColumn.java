package dat.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import dat.util.StrUtil;
import lombok.Data;

/**
 * @author MaoSonglin
 * 数据包中的数据表包含的虚拟字段类<br>
 * 关联属性包括：<br>
 * refColumns<br>
 * table<br>
 * graphs
 */ 
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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

	@JsonIgnore
	@ManyToMany(targetEntity=GraphInfo.class,fetch=FetchType.LAZY)
	private List<GraphInfo> graphs;
	
	 
	
	public void setId(){
		String id = StrUtil.generatorId();
		setId("VC"+id.substring(2));
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

	 

	
	public String getRela(){
		return null;
	}
}
