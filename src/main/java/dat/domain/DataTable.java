package dat.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据表实体类，该实体类对应某个数据源中实际存在的数据表，表中的数据字段与该实体类中的属性一一对应
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTable implements IdGeneratorable,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4532397303906592014L;

	@Id
	private String id;
	
	private String name;
	
	private String chinese;
	
	@ManyToOne(targetEntity=Source.class,fetch=FetchType.LAZY)
	@JoinColumn(name="data_source_id",referencedColumnName="id")
	@JsonIgnore
	private Source source;
	
	@OneToMany(targetEntity=TableColumn.class,fetch=FetchType.LAZY,mappedBy="dataTable")
	@JsonIgnore
	private List<TableColumn> columns;
	
	
	private Date addTime;
	
	@Column(name="`desc`")
	private String desc;
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	private Integer state = Constant.ACTIVATE_SATE;

	@Override
	public String toString() {
		return "DataTable [id=" + id + ", name=" + name + ", chinese="
				+ chinese + ", source=" + source + ", addTime=" + addTime
				+ ", state=" + state + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getCommit() {
		return desc;
	}

	public void setCommit(String commit) {
		this.desc = commit;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
	
	
	public void generateId(){
		String id = StrUtil.generatorId();
		setId("TB"+id.substring(2));
	}

	public List<TableColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<TableColumn> columns) {
		for (TableColumn tableColumn : columns) {
			tableColumn.setDataTable(this);
		}
		this.columns = columns;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if(!getClass().isAssignableFrom(obj.getClass()))
			return false;
		DataTable other = (DataTable) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
}