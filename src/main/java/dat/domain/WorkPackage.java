package dat.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 业务数据包对应的实体类对象，保存和修改该实体对象的实例会自动保存和修改该
 * 实例对象关联的虚拟数据表，但是删除该对象的实例对象不会进行级联删除
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class WorkPackage {
	
	@Id 
	private String id;
	
	private String name;
	
	private Boolean modify = true;
	
	private Integer state = Constant.ACTIVATE_SATE;
	
	@ManyToMany(targetEntity=VirtualTable.class,mappedBy="packages",fetch=FetchType.LAZY)
	@JsonIgnore
	private List<VirtualTable> tables;
	
	@OneToMany(targetEntity=ReportInfo.class,mappedBy="pkg",fetch=FetchType.LAZY)
	@JsonIgnore
	private List<ReportInfo> reports;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setId(){
		String id = StrUtil.generatorId();
		setId("PKG"+id.substring(2));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public List<VirtualTable> getTables() {
		if(tables == null)
			tables = new ArrayList<>();
		return tables;
	}

	public void setTables(List<VirtualTable> tables) {
		this.tables = tables;
	}

	public Boolean getModify() {
		return modify;
	}

	public void setModify(Boolean modify) {
		this.modify = modify;
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
		WorkPackage other = (WorkPackage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public List<ReportInfo> getReports() {
		return reports;
	}

	public void setReports(List<ReportInfo> reports) {
		this.reports = reports;
	}

	@Override
	public String toString() {
		return "WorkPackage [id=" + id + ", name=" + name + ", state=" + state
				+ "]";
	}


	
}
