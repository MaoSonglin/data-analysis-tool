package dat.domain;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * @author MaoSonglin
 * 表与表，字段与字段之前的关联关系
 */
//@Entity
//@Table(name="assoc_info")
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
	private VirtualTable table;
	
	@OneToOne(targetEntity=DataTable.class) @JoinColumn(referencedColumnName="id")
	private DataTable pkTable;
	
	@OneToOne  @JoinColumn(referencedColumnName="id")
	private TableColumn pkColumn;
	
	@OneToOne  @JoinColumn(referencedColumnName="id")
	private DataTable fkTable;
	
	@OneToOne  @JoinColumn(referencedColumnName="id")
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

	public VirtualTable getTable() {
		return table;
	}

	public void setTable(VirtualTable table) {
		this.table = table;
	}

	public DataTable getPkTable() {
		return pkTable;
	}

	public void setPkTable(DataTable pkTable) {
		this.pkTable = pkTable;
	}

	public TableColumn getPkColumn() {
		return pkColumn;
	}

	public void setPkColumn(TableColumn pkColumn) {
		this.pkColumn = pkColumn;
	}

	public DataTable getFkTable() {
		return fkTable;
	}

	public void setFkTable(DataTable fkTable) {
		this.fkTable = fkTable;
	}

	public TableColumn getFkColumn() {
		return fkColumn;
	}

	public void setFkColumn(TableColumn fkColumn) {
		this.fkColumn = fkColumn;
	}


	
	
}
