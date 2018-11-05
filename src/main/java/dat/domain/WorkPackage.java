package dat.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 业务数据包对应的实体类对象，保存和修改该实体对象的实例会自动保存和修改该
 * 实例对象关联的虚拟数据表，但是删除该对象的实例对象不会进行级联删除
 */
@Entity
public class WorkPackage {
	
	@Id @Column(length=18)
	private String id;
	
	private String name;
	
	private Integer state = Constant.ACTIVATE_SATE;
	
	@ManyToMany(targetEntity=VirtualTable.class,mappedBy="packages",fetch=FetchType.LAZY)
	@JsonIgnore
	private List<VirtualTable> tables;

	public String getId() {
		return id;
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

	@Override
	public String toString() {
		return "WorkPackage [id=" + id + ", name=" + name + ", state=" + state
				+ "]";
	}


	
}
