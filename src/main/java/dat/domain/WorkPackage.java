package dat.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.StrUtil;

@Entity
public class WorkPackage {
	
	@Id @Column(length=18)
	private String id;
	
	private String name;
	
	private Integer state;
	
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
		return tables;
	}

	public void setTables(List<VirtualTable> tables) {
		this.tables = tables;
	}


	
}
