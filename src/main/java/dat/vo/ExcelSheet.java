package dat.vo;

import java.util.List;

public class ExcelSheet {
	private String sheetName;
	
	private String chineseName;
	
	private String desc;
	
	private Integer fieldNameRow;
	
	private Integer firstDataRow;
	
	/**
	 * 日期模式
	 */
	private String datePattern;
	
	/**
	 * 日期分割符
	 */
	private String dateSegmentation;
	
	/**
	 * 时间分隔符
	 */
	private String timeSegmentation;
	
	/**
	 * 日期时间排序
	 */
	private String dateTimeSort;
	
	private List<String> columnNames;
	
	private List<String> fieldNames;
	
	private List<String> fieldChineseNames;
	
	private String tableName;
	
	private String primaryKey;
	
	private List<String> types;

	private List<Integer> lengths;
	
	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public Integer getFieldNameRow() {
		return fieldNameRow;
	}

	public void setFieldNameRow(Integer fieldNameRow) {
		this.fieldNameRow = fieldNameRow;
	}

	public Integer getFirstDataRow() {
		return firstDataRow;
	}

	public void setFirstDataRow(Integer firstDataRow) {
		this.firstDataRow = firstDataRow;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getDateSegmentation() {
		return dateSegmentation;
	}

	public void setDateSegmentation(String dateSegmentation) {
		this.dateSegmentation = dateSegmentation;
	}

	public String getTimeSegmentation() {
		return timeSegmentation;
	}

	public void setTimeSegmentation(String timeSegmentation) {
		this.timeSegmentation = timeSegmentation;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<String> getTypes() {
		return types;
	}

	public List<String> getFieldNames() {
		if(fieldNames == null)
			return columnNames;
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public String getTableName() {
		if(tableName == null)
			return sheetName;
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public String getDateTimeSort() {
		return dateTimeSort;
	}

	public void setDateTimeSort(String dateTimeSort) {
		this.dateTimeSort = dateTimeSort;
	}

	public List<Integer> getLengths() {
		return lengths;
	}

	public void setLengths(List<Integer> lengths) {
		this.lengths = lengths;
	}

	@Override
	public String toString() {
		return "ExcelSheet [sheetName=" + sheetName + ", fieldNameRow="
				+ fieldNameRow + ", firstDataRow=" + firstDataRow
				+ ", datePattern=" + datePattern + ", dateSegmentation="
				+ dateSegmentation + ", timeSegmentation=" + timeSegmentation
				+ ", dateTimeSort=" + dateTimeSort + ", columnNames="
				+ columnNames + ", primaryKey=" + primaryKey + ", types="
				+ types + ", lengths=" + lengths + "]";
	}

	public String getChineseName() {
		if(chineseName == null)
			return sheetName;
		return chineseName;
	}

	public void setChineseName(String chineseName) {
		this.chineseName = chineseName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<String> getFieldChineseNames() {
		if(fieldChineseNames == null)
			return getColumnNames();
		return fieldChineseNames;
	}

	public void setFieldChineseNames(List<String> fieldChineseNames) {
		this.fieldChineseNames = fieldChineseNames;
	}
	

}
