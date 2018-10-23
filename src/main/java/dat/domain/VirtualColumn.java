package dat.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;

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
	
	/**
	 * 该虚拟字段相关联的实际字段
	 */
	@ManyToMany(targetEntity=TableColumn.class)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnore
	private List<TableColumn> refColumns;

	/**
	 * 包含该虚拟字段的虚拟数据表
	 */
	@ManyToMany(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
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

	public String getChinese() {
		return chinese;
	}

	

	public List<VirtualTable> getTables() {
		return tables;
	}

	public void setTables(List<VirtualTable> tables) {
		this.tables = tables;
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
		return refColumns;
	}

	public void setRefColumns(List<TableColumn> refColumns) {
		this.refColumns = refColumns;
	}
	
	
}
