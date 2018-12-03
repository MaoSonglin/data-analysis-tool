package dat.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author MaoSonglin
 * 表与表，字段与字段之前的关联关系
 */
@Entity
@Table(name="table_assoc_info")
public class Association implements Serializable {

	public static final long serialVersionUID = 4583658216833662296L;
	
	public static final int ONE_TO_ONE = 0;
	
	public static final int ONE_TO_MANY = 1;
	
	public static final int MANY_TO_MYAy = 2;
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String name;
	
	private Integer type;
	
	@ManyToOne  @JoinColumn(referencedColumnName="id")
	private VirtualTable pkTable;
	
	@ManyToOne  @JoinColumn(referencedColumnName="id")
	private VirtualTable fkTable;
	
	@ManyToOne  @JoinColumn(referencedColumnName="id")
	private TableColumn pkColumn;
	
	@ManyToOne  @JoinColumn(referencedColumnName="id")
	private TableColumn fkColumn;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	
	

	public VirtualTable getPkTable() {
		return pkTable;
	}

	public void setPkTable(VirtualTable pkTable) {
		this.pkTable = pkTable;
	}

	public TableColumn getFkColumn() {
		return fkColumn;
	}

	public void setFkColumn(TableColumn fkColumn) {
		this.fkColumn = fkColumn;
	}

	public TableColumn getPkColumn() {
		return pkColumn;
	}

	public void setPkColumn(TableColumn pkColumn) {
		this.pkColumn = pkColumn;
	}

	public VirtualTable getFkTable() {
		return fkTable;
	}

	public void setFkTable(VirtualTable fkTable) {
		this.fkTable = fkTable;
	}
	
}
