package dat.domain;

public class TableColumnPagingBean extends PagingBean {

	private String sourceName;
	
	private String tableName;
	
	private String columnName;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public String toString() {
		return "TableColumnPagingBean [sourceName=" + sourceName
				+ ", tableName=" + tableName + ", columnName=" + columnName
				+ "]";
	}
	
	
	
}
