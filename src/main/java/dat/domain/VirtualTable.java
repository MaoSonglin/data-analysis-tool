package dat.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据包中的虚拟表实体类对象
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class VirtualTable implements IdGeneratorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3562126732729766303L;

	@Id	
	private String id;
	
	private String name;
	
	private String chinese;
	
	private Integer state = Constant.ACTIVATE_SATE;
	
	/**
	 * 该虚拟表中包含的虚拟字段
	 */
	@OneToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY,mappedBy="table",cascade={CascadeType.REMOVE})
	@JsonIgnore
	private List<VirtualColumn> columns;
	
	@OneToMany(targetEntity=Association.class,fetch=FetchType.LAZY,mappedBy="pkTable")
	private List<Association> foreigns;
	
	/**
	 * 该虚拟表所属的数据包
	 */
	@ManyToMany(targetEntity=WorkPackage.class,fetch=FetchType.LAZY,cascade=CascadeType.MERGE)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnore
	private List<WorkPackage> packages;

	/*@OneToMany(fetch=FetchType.LAZY,cascade=CascadeType.REMOVE,mappedBy="table")
	@JsonIgnore
	private List<Association> assocs;*/
	
	public String getId() {
		return id;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
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

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public List<VirtualColumn> getColumns() {
		if(columns == null)
			columns = new ArrayList<>();
		return columns;
	}

	public void setColumns(List<VirtualColumn> columns) {
		this.columns = columns;
	}

	@Override
	public void generateId() {
		String id = StrUtil.generatorId();
		setId("VT"+id.substring(2));
	}

	public List<WorkPackage> getPackages() {
		if(packages == null)
			packages = new ArrayList<>();
		return packages;
	}

	public void setPackages(List<WorkPackage> packages) {
		this.packages = packages;
	}

	public List<Association> getForeigns() {
		return foreigns;
	}

	public void setForeigns(List<Association> foreigns) {
		this.foreigns = foreigns;
	}

	@Override
	public String toString() {
		return "VirtualTable [id=" + id + ", name=" + name + ", chinese="
				+ chinese + "]";
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
		VirtualTable other = (VirtualTable) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/*public List<Association> getAssocs() {
		return assocs;
	}

	public void setAssocs(List<Association> assocs) {
		this.assocs = assocs;
	}*/
}
