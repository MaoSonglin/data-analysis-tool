package dat.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.jboss.logging.Logger;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;


public class OracleSourceMetaData implements MetaDataParser{

	private static Logger logger = Logger.getLogger(OracleSourceMetaData.class);
	Source source;
	
	public OracleSourceMetaData(Source source) {
		super();
		this.source = source;
		try {
			logger.debug("load class of "+source.getDriverClass());
			Class.forName(source.getDriverClass());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean testConnection() {
		try(Connection conn = getConnection()) {
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} 
	}
	
	private Connection getConnection() throws SQLException{
		if(logger.isDebugEnabled()){
			logger.debug(String.format("try to setup connection to url \"%s\" with username '%s' and password '%s'",
					source.getUrl(),source.getUsername(),source.getPassword()));
		}
		Connection conn = DriverManager.getConnection(source.getUrl(), source.getUsername(), source.getPassword());
		Connection connection = (Connection) Proxy.newProxyInstance(
				conn.getClass().getClassLoader(), 
				new Class<?>[]{Connection.class},
				(Object proxy, Method method, Object[] args)-> {
					boolean equals = method.getName().equals("close");
					if(equals){
						logger.debug("closing database connection");
					}
					return method.invoke(conn, args);
				});
		if(logger.isDebugEnabled()){
			logger.debug(String.format("get a database connection instance %s", conn.getClass().getName()));
		}
		return connection;
	}

	@Override
	public List<DataTable> getTables() {
		String sql = "select TABLE_NAME from user_tables";
		try(Connection conn = getConnection()){
			QueryRunner runner = new QueryRunner();
			List<DataTable> tables = runner.query(conn, sql, (rs)->{
				List<DataTable> list = new ArrayList<>();
				while(rs.next()){
					DataTable dataTable = getTable(rs);
					list.add(dataTable);
				}
				return list;
			});
			return tables;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return MetaDataParser.super.getTables();
	}

	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private DataTable getTable(ResultSet rs) throws SQLException {
		String table_name = rs.getString("TABLE_NAME");
		DataTable dataTable = new DataTable();
		dataTable.setName(table_name);
		dataTable.setSource(source);
		dataTable.setAddTime(new Date());
		dataTable.setState(Constant.ACTIVATE_SATE);
		dataTable.generateId();
		logger.debug("read table '"+table_name+"' from database '"+source.getName()+"' successful !");
		return dataTable;
	}

	@Override
	public List<TableColumn> getColumnOfTable(DataTable table) {
		try(Connection conn = getConnection()){
			ResultSet rs = conn.getMetaData().getColumns(table.getName(), null, table.getName(), "%");
			List<TableColumn> list = new LinkedList<>();
			while(rs.next()){
				TableColumn field = getField(rs);
				field.setDataTable(table);
				list.add(field);
			}
			return list;
		} catch(Exception e){
			e.printStackTrace();
		}
		return MetaDataParser.super.getColumnOfTable(table);
	}

	/**
	 * @param rs
	 * @throws SQLException
	 */
	private TableColumn getField(ResultSet rs) throws SQLException {
		TableColumn tableColumn = new TableColumn();
		tableColumn.setColumnName(rs.getString("COLUMN_NAME"));
		tableColumn.setDataType(rs.getInt("DATA_TYPE"));
		tableColumn.setTypeName(rs.getString("TYPE_NAME"));
		tableColumn.setColumnSize(rs.getInt("COLUMN_SIZE"));
		tableColumn.setBufferLength(rs.getInt("BUFFER_LENGTH"));
		tableColumn.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
		tableColumn.setNumPrecRadix(rs.getInt("NUM_PREC_RADIX"));
		tableColumn.setNullable(rs.getBoolean("NULLABLE"));
		tableColumn.setRemarks(rs.getString("REMARKS"));
//		tableColumn.setColumnDef(rs.getString("COLUMN_DEF"));
//		tableColumn.setSqlDataType(rs.getInt("SQL_DATA_TYPE"));
//		tableColumn.setSqlDatetimeSub(rs.getInt("SQL_DATETIME_SUB"));
//		tableColumn.setCharOctetLength(rs.getInt("CHAR_OCTET_LENGTH"));
//		tableColumn.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
		tableColumn.setAddTime(StrUtil.currentTime());
		tableColumn.generateId();
		return tableColumn;
	}

	@Override
	public List<TableColumn> getColumns() {
		logger.debug("get fileds in datasource of "+source.getName()+"");
		String sql = "select TABLE_NAME from user_tables";
		try(Connection conn = getConnection()){
			QueryRunner runner = new QueryRunner();
			return runner.query(conn, sql, (rs)->{
				List<TableColumn> fields = new LinkedList<>();
				while(rs.next()){
					DataTable dataTable = getTable(rs);
					ResultSet columns = conn.getMetaData().getColumns("%", "%", dataTable.getName(), "%");
					while(columns.next()){
						TableColumn field = getField(columns);
						field.setDataTable(dataTable);
						fields.add(field);
					}
					
				}
				return fields;
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return MetaDataParser.super.getColumns();
	}


}
