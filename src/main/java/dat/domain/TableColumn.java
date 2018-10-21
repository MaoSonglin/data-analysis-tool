package dat.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据列对应的实体类
 */
@Entity
public class TableColumn implements IdGeneratorable{

	@Id
	@Column(length=18)
	private String id;
	
	@Column(length=32)
	private String name;
	
	@Column(length=64)
	private String chinese;
	
	@Column(length=10)
	private String jdbcType;
	
	private Integer length;
	
	private Boolean canBeNull;
	
	private Boolean isPrimaryKey;
	
	private Boolean isForeginKey;
	
	private Boolean isUnique;
	
	@Column(length=255)
	private String comment;
	
	@ManyToOne(targetEntity=DataTable.class,optional=true)
	@JoinColumn(name="data_table_id",referencedColumnName="id")
	private DataTable dataTable;
	
	@OneToOne(targetEntity=TableColumn.class)
	private TableColumn referenceColumn;
	
	public void generateId() {
		id = StrUtil.generatorId();
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

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Boolean getCanBeNull() {
		return canBeNull;
	}

	public void setCanBeNull(Boolean canBeNull) {
		this.canBeNull = canBeNull;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public Boolean getIsForeginKey() {
		return isForeginKey;
	}

	public void setIsForeginKey(Boolean isForeginKey) {
		this.isForeginKey = isForeginKey;
	}

	public Boolean getIsUnique() {
		return isUnique;
	}

	public void setIsUnique(Boolean isUnique) {
		this.isUnique = isUnique;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}

	public TableColumn getReferenceColumn() {
		return referenceColumn;
	}

	public void setReferenceColumn(TableColumn referenceColumn) {
		this.referenceColumn = referenceColumn;
	}

	@Override
	public String toString() {
		return "TableColumn [id=" + id + ", name=" + name + ", chinese="
				+ chinese + ", jdbcType=" + jdbcType + ", length=" + length
				+ ", canBeNull=" + canBeNull + ", isPrimaryKey=" + isPrimaryKey
				+ ", isForeginKey=" + isForeginKey + ", isUnique=" + isUnique
				+ ", comment=" + comment + "]";
	}

	
}
