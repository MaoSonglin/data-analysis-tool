package dat.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据列对应的实体类
 */
@Entity
public class TableColumn implements IdGeneratorable{

	private static Logger logger = LoggerFactory.getLogger(TableColumn.class);
	
	private static final long serialVersionUID = 5234868668420594840L;

	@Id
	private String id;
	 
	@Column
	private String chinese;
	
	
	/******************************************************/
	private String columnName;
	
	private Integer dataType;
	
	private String typeName;
	
	private Integer columnSize;
	
	private Integer bufferLength;
	
	private Integer decimalDigits;
	
	private Integer numPrecRadix;
	
	private Boolean nullable;
	
	private String remarks;
	
	/**
	 * 当前字段作为外键，参考其他表的信息
	 */
	@OneToOne(targetEntity=ForeignKey.class,mappedBy="foreignColumn",fetch=FetchType.EAGER)
	@JsonIgnore
	private ForeignKey foreignKey;
	
	/**
	 * 引用当前字段的外键
	 */
	@OneToMany(targetEntity=ForeignKey.class,mappedBy="primaryColumn",fetch=FetchType.EAGER)
	@JsonIgnore
	private List<ForeignKey> quote;
	
	/*private Integer sqlDataType;
	
	private Integer sqlDatetimeSub;
	
	private Integer charOctetLength;
	
	private Integer ordinalPosition;*/
	
	/******************************************************/
	
	@JsonIgnore
	@ManyToOne(targetEntity=DataTable.class,optional=true,fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.REFRESH,CascadeType.MERGE})
	@JoinColumn(name="data_table_id",referencedColumnName="id")
	private DataTable dataTable;
	
	
	/*@JsonIgnore
	@ManyToMany(targetEntity=TableColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(name="assoc_column_id",referencedColumnName="id")
	private List<TableColumn> assoc;*/
	
	private String addTime;
	/**
	 * 删除状态
	 */
	private Integer state = Constant.ACTIVATE_SATE;
	
	
	

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public void generateId() {
		String id = StrUtil.generatorId();
		setId("FD"+id.substring(2));
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	 
	public String getChinese() {
		return chinese;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}

//	public TableColumn getReferenceColumn() {
//		return referenceColumn;
//	}
//
//	public void setReferenceColumn(TableColumn referenceColumn) {
//		this.referenceColumn = referenceColumn;
//	}


	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Integer getDataType() {
		return dataType;
	}

	public void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}

	public Integer getBufferLength() {
		return bufferLength;
	}

	public void setBufferLength(Integer bufferLength) {
		this.bufferLength = bufferLength;
	}

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public Integer getNumPrecRadix() {
		return numPrecRadix;
	}

	public void setNumPrecRadix(Integer numPrecRadix) {
		this.numPrecRadix = numPrecRadix;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	
	public ForeignKey getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(ForeignKey foreignKey) {
		logger.debug("设置该字段的外键");
		this.foreignKey = foreignKey;
	}

	/**
	 * 获取引用该字段的外键
	 * @return
	 */
	public List<ForeignKey> getQuote() {
		return quote;
	}

	/**
	 * 设置引用该字段的外键信息
	 * @param quote
	 */
	public void setQuote(List<ForeignKey> quote) {
		logger.debug("设置引用该字段的外键");
		this.quote = quote;
	}

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
		TableColumn other = (TableColumn) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TableColumn [id=" + id + ", chinese=" + chinese
				+ ", columnName=" + columnName + ", dataType=" + dataType
				+ ", typeName=" + typeName + ", columnSize=" + columnSize
				+ ", bufferLength=" + bufferLength + ", decimalDigits="
				+ decimalDigits + ", numPrecRadix=" + numPrecRadix
				+ ", nullable=" + nullable + ", remarks=" + remarks
				+ ", addTime=" + addTime + ", state=" + state + "]";
	}


}
