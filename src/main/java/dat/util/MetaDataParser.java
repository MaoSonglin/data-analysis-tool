package dat.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.mysql.jdbc.Statement;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;

/**
 * @author MaoSonglin
 * 数据源元数据解析器，可以获取数据源中的数据表信息，字段信息等。
 */
public interface MetaDataParser {
	public static class SourceMetaDataException extends RuntimeException{
		private static final long serialVersionUID = -543575228175945068L;
		public SourceMetaDataException(String msg){
			super(msg);
		}
	}
	/**
	 * 根据数据源获取不同的数据源解析器
	 * @param source	数据源
	 * @return			数据源解析器
	 */
	static MetaDataParser getSourceMetaData(Source source){
		String databaseName = source.getDatabaseName();
		// MySQL、SQL Server、SQLite类型数据源
		if(Constant.MYSOL.equalsIgnoreCase(databaseName)
				||Constant.SQL_SERVER.equalsIgnoreCase(databaseName)
				||Constant.SQLITE.equalsIgnoreCase(databaseName)){
			return new MySQLSourceMetaData(source);
		}
		// oracle数据源使用
		if(Constant.ORACLE.equals(databaseName)){
			return new OracleSourceMetaData(source);
		}
		throw new SourceMetaDataException("unexpected datasource type of database name "+databaseName);
	}
	/**
	 * 测试数据源连接是否正确
	 * @return 如果可以顺利的连接到数据源返回true，否则返回false
	 */
	default boolean testConnection(){
		return false;
	}
	
	/**
	 * 获取数据源中包含的数据表信息
	 * @return
	 */
	default List<DataTable> getTables(){
		return null;
	}
	
	/**
	 * 获取数据中指定数据表下的数据字段
	 * @param table
	 * @return
	 */
	default List<TableColumn> getColumnOfTable(DataTable table){
		return null;
	}
	
	/**
	 * 获取数据源下的所有字段信息，字段中包含数据表信息的关联对象
	 * @return
	 */
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

class MySQLSourceMetaData implements MetaDataParser{
	private static Logger logger = Logger.getLogger(MySQLSourceMetaData.class);
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
		logger.debug("test database is connectable");
        try {
			conn =  getConn();
			logger.debug("database connected successful!");
			return true;
		} catch (Exception e) {
			logger.debug("database connect fail with exception "+e.getMessage());
			return false;
		} finally {
			close(conn,null,null);
		}
	}

	private Connection getConn(){
		try {
			logger.debug("load database driver class "+driverClass);
			Class.forName(driverClass);
			logger.debug("get database connection from url '"+url+"' with username '"+username+"' and password '"+password+"'");
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
			logger.debug("get tables in database ....");
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
		logger.debug("read table '"+table_name+"' from database '"+mysqlSource.getName()+"' successful !");
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
			logger.debug("reading fields from table '"+name+"' in datasource '"+mysqlSource.getName()+"'");
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getColumns(conn.getCatalog(), null, name, null);
			while(rs.next()){
				TableColumn tableColumn = doResult(rs);
				logger.debug("get a field '"+tableColumn.getColumnName()+"' from table '"+name+" successful ");
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
		ResultSet rs2 = null;
		try {
			conn = getConn();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(conn.getCatalog(), "%", null, new String[]{"TABLE","VIEW"});
			Map<String,DataTable> map = new HashMap<>();
			while(rs.next()){
				DataTable table = getTable(rs);
				String name = table.getName();
				logger.debug("read a table \""+name+"\" successful !");
				map.put(name, table);
			}
			
			rs2 = metaData.getColumns(conn.getCatalog(), "%", "%", "%");
			while(rs2.next()){
				String tableName = rs2.getString("TABLE_NAME");
				TableColumn tableColumn = doResult(rs2);
				logger.debug("read a field \""+tableColumn.getColumnName()+"\" from table \""+tableName+"\" successful !");
				DataTable dataTable = map.get(tableName);
				tableColumn.setDataTable(dataTable);
				tableColumn.generateId();
				if(dataTable!=null)
					list.add(tableColumn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			close(null,null,rs2);
			close(conn,null,rs);
		}
		return list;
	}
	
	
}
