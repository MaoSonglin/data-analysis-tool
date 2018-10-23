package dat.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.Statement;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;

public interface SourceMetaData {
	
	static SourceMetaData getSourceMetaData(Source source){
		String databaseName = source.getDatabaseName();
		if(Constant.MYSOL.equalsIgnoreCase(databaseName)
				||Constant.SQL_SERVER.equalsIgnoreCase(databaseName)
				||Constant.ORACLE.equalsIgnoreCase(databaseName)
				||Constant.SQLITE.equalsIgnoreCase(databaseName)){
			return new MySQLSourceMetaData(source);
		}
		return null;
	}
	default boolean testConnection(){
		return false;
	}
	default List<DataTable> getTables(){
		return null;
	}
	
	default List<TableColumn> getColumnOfTable(DataTable table){
		return null;
	}
	
	default List<TableColumn> getColumns(){
		return null;
	}
	
	enum SourceType{
		MySQL,
		SQLServer,
		Oracle,
		DB2,
		Sybase,
		Access,
		SQLite,
		Derby,
		Postgre,
		Informix,
		ODBC,
		Hive,
		MongoDB,
		Excel,
		XML
	}
	static public enum ColumnAttrName {
		TABLE_CAT,
		TABLE_SCHEM,
		TABLE_NAME,
		COLUMN_NAME,
		DATA_TYPE,
		TYPE_NAME,
		COLUMN_SIZE,
		BUFFER_LENGTH,
		DECIMAL_DIGITS,
		NUM_PREC_RADIX,
		NULLABLE,
		REMARKS,
		COLUMN_DEF,
		SQL_DATA_TYPE,
		SQL_DATETIME_SUB,
		CHAR_OCTET_LENGTH,
		ORDINAL_POSITION,
		IS_NULLABLE,
		SCOPE_CATLOG,
		SCOPE_SCHEMA,
		SCOPE_TABLE,
		SOURCE_DATA_TYPE,
		IS_AUTOINCREMENT,
		IS_GENERATEDCOLUMN
	}
}

class MySQLSourceMetaData implements SourceMetaData{
	
	private Source mysqlSource;
	private String driverClass;
	private String url;
	private String username;
	private String password;

	
	public MySQLSourceMetaData(){
		
	}

	public MySQLSourceMetaData(Source source) {
		setSource(source);
	}

	public Source getSource() {
		return mysqlSource;
	}

	public void setSource(Source mysqlSource) {
		this.mysqlSource = mysqlSource;
		driverClass = mysqlSource.getDriverClass();
		url = mysqlSource.getUrl();
		username = mysqlSource.getUsername();
		password = mysqlSource.getPassword();
	}
	
	
	@Override
	public boolean testConnection() {
		Connection conn = null;
        try {
			conn =  getConn();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			close(conn,null,null);
		}
	}

	private Connection getConn(){
		try {
			Class.forName(driverClass);
			return DriverManager.getConnection(url,username,password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void close(Connection conn, Statement st, ResultSet rs) {
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rs = null;
			}
		}
		if(st != null){
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				st = null;
			}
		}
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				conn = null;
			}
		}
	}
	
	@Override
	public List<DataTable> getTables() {
		List<DataTable> datatables = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		try{
			conn = getConn();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(null, "%", null, new String[]{"TABLE","VIEW"});
			while(rs.next()){
				DataTable dataTable = getTable(rs);
				datatables.add(dataTable);
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			close(conn,null,rs);
		}
		return datatables;
	}

	private DataTable getTable(ResultSet rs) throws SQLException {
		String table_name = rs.getString("table_name");
		DataTable dataTable = new DataTable();
		dataTable.setName(table_name);
		dataTable.setSource(mysqlSource);
		dataTable.setAddTime(new Date());
		dataTable.generateId();
		dataTable.setState(Constant.ACTIVATE_SATE);
		return dataTable;
	}

	@Override
	public List<TableColumn> getColumnOfTable(DataTable table) {
		List<TableColumn> list = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = getConn();
			String name = table.getName();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getColumns(conn.getCatalog(), "%", name, "%");
			while(rs.next()){
				TableColumn tableColumn = doResult(rs);
				tableColumn.setDataTable(table);
				tableColumn.generateId();
				list.add(tableColumn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			close(conn,null,rs);
		}
		return list;
	}

	private TableColumn doResult(ResultSet columns) throws SQLException {
		TableColumn tableColumn = new TableColumn();
		tableColumn.setColumnName(columns.getString("COLUMN_NAME"));
		tableColumn.setDataType(columns.getInt("DATA_TYPE"));
		tableColumn.setTypeName(columns.getString("TYPE_NAME"));
		tableColumn.setColumnSize(columns.getInt("COLUMN_SIZE"));
		tableColumn.setBufferLength(columns.getInt("BUFFER_LENGTH"));
		tableColumn.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
		tableColumn.setNumPrecRadix(columns.getInt("NUM_PREC_RADIX"));
		tableColumn.setNullable(columns.getBoolean("NULLABLE"));
		tableColumn.setRemarks(columns.getString("REMARKS"));
		tableColumn.setColumnDef(columns.getString("COLUMN_DEF"));
		tableColumn.setSqlDataType(columns.getInt("SQL_DATA_TYPE"));
		tableColumn.setSqlDatetimeSub(columns.getInt("SQL_DATETIME_SUB"));
		tableColumn.setCharOctetLength(columns.getInt("CHAR_OCTET_LENGTH"));
		tableColumn.setOrdinalPosition(columns.getInt("ORDINAL_POSITION"));
		tableColumn.setAddTime(StrUtil.currentTime());
		return tableColumn;
	}

	@Override
	public List<TableColumn> getColumns() {
		List<TableColumn> list = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = getConn();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(conn.getCatalog(), "%", null, new String[]{"TABLE","VIEW"});
			while(rs.next()){
				DataTable table = getTable(rs);
				String name = table.getName();
				ResultSet columns = metaData.getColumns(conn.getCatalog(), "%", name, "%");
				while(columns.next()){
					TableColumn tableColumn = doResult(columns);
					tableColumn.setDataTable(table);
					tableColumn.generateId();
					list.add(tableColumn);
				}
				columns.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			close(conn,null,rs);
		}
		return list;
	}
	
	
}