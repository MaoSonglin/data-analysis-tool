package dat.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据包中的虚拟表实体类对象
 */
@Entity
public class VirtualTable implements IdGeneratorable{

	@Id	@Column(length=18)
	private String id;
	
	private String name;
	
	private String chinese;
	
	/**
	 * 该虚拟表中包含的虚拟字段
	 */
	@OneToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY,mappedBy="table",cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@JsonIgnore
	private List<VirtualColumn> columns;
	
	/**
	 * 该虚拟表所属的数据包
	 */
	@ManyToMany(targetEntity=WorkPackage.class,fetch=FetchType.LAZY)
	@JsonIgnore
	private List<WorkPackage> packages;

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
		this.id = StrUtil.generatorId();
	}

	public List<WorkPackage> getPackages() {
		return packages;
	}

	public void setPackages(List<WorkPackage> packages) {
		this.packages = packages;
	}
	
	
}
