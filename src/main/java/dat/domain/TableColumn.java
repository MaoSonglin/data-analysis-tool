package dat.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据列对应的实体类
 */
@Entity
public class TableColumn implements IdGeneratorable{

	@Id
	private String id;
	
	 
	@Column
	private String chinese;
	
	 
	 
	private Boolean isPrimaryKey;
	
	private Boolean isForeginKey;
	
	private Boolean isUnique;
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
	
	private String columnDef;
	
	private Integer sqlDataType;
	
	private Integer sqlDatetimeSub;
	
	private Integer charOctetLength;
	
	private Integer ordinalPosition;
	
	/******************************************************/
	
	@JsonIgnore
	@ManyToOne(targetEntity=DataTable.class,optional=true,fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.REFRESH,CascadeType.MERGE})
	@JoinColumn(name="data_table_id",referencedColumnName="id")
	private DataTable dataTable;
	
	
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

	public String getColumnDef() {
		return columnDef;
	}

	public void setColumnDef(String columnDef) {
		this.columnDef = columnDef;
	}

	public Integer getSqlDataType() {
		return sqlDataType;
	}

	public void setSqlDataType(Integer sqlDataType) {
		this.sqlDataType = sqlDataType;
	}

	public Integer getSqlDatetimeSub() {
		return sqlDatetimeSub;
	}

	public void setSqlDatetimeSub(Integer sqlDatetimeSub) {
		this.sqlDatetimeSub = sqlDatetimeSub;
	}

	public Integer getCharOctetLength() {
		return charOctetLength;
	}

	public void setCharOctetLength(Integer charOctetLength) {
		this.charOctetLength = charOctetLength;
	}

	public Integer getOrdinalPosition() {
		return ordinalPosition;
	}

	public void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	@Override
	public String toString() {
		return "TableColumn [id=" + id + ", chinese=" + chinese
				+ ", isPrimaryKey=" + isPrimaryKey + ", isForeginKey="
				+ isForeginKey + ", isUnique=" + isUnique + ", columnName="
				+ columnName + ", dataType=" + dataType + ", typeName="
				+ typeName + ", columnSize=" + columnSize + ", bufferLength="
				+ bufferLength + ", decimalDigits=" + decimalDigits
				+ ", numPrecRadix=" + numPrecRadix + ", nullable=" + nullable
				+ ", remarks=" + remarks + ", columnDef=" + columnDef
				+ ", sqlDataType=" + sqlDataType + ", sqlDatetimeSub="
				+ sqlDatetimeSub + ", charOctetLength=" + charOctetLength
				+ ", ordinalPosition=" + ordinalPosition + ", dataTable="
				+ dataTable + ", addTime=" + addTime + ", state=" + state + "]";
	}
	
}
