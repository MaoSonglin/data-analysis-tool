package dat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.jboss.logging.Logger;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;

public class TableHandleImpl implements TableHandle {
	private static Logger logger = Logger.getLogger(TableHandleImpl.class);
	private String driverClassName;
	
	private String url;
	
	private String password;
	
	private String username;
	
	private String tableName;
	
	private String databaseName;
	
	public TableHandleImpl() {
		super();
	}

	public TableHandleImpl(DataTable table) {
		Source source = table.getSource();
		driverClassName= source.getDriverClass();
		url = source.getUrl();
		username = source.getUsername();
		password = source.getPassword();
		tableName = table.getName();
		databaseName = source.getDatabaseName();
		init();
	}

	@Override
	public Map<String, List<String>> select(List<TableColumn> columns,
			int offset, int limit) {
		List<String> list = new ArrayList<>(columns.size());
		for (TableColumn tableColumn : columns) {
			list.add(tableColumn.getColumnName());
		}
		return selectByColumnNames(list,offset,limit);
	}

	@Override
	public Map<String, List<String>> selectByColumnNames(
			List<String> columnNames, int offset, int limit) {
		StringBuffer sb = new StringBuffer();
		for (String columnName : columnNames) {
			sb.append(columnName).append(',');
		}
		if(sb.length()>0)
			sb.deleteCharAt(sb.length()-1);
		sb.insert(0, "select ");
		sb.append(" from ").append(tableName);
		Dialect dialect = DialectUtil.getDialect(databaseName);
		LimitHandler handler = dialect.getLimitHandler();
		String sql = null;
		RowSelection selection = null;
		if(handler.supportsLimit()){
			 selection = new RowSelection();
			selection.setFirstRow(offset);
			selection.setMaxRows(limit);
			sql = handler.processSql(sb.toString(), selection );
		}else{
			sql = sb.toString();
		}
		logger.debug(sql);
		try(Connection conn = DriverManager.getConnection(url, username, password);
			PreparedStatement ps =	conn.prepareStatement(sql);	){
			if(handler.supportsLimit()){
				handler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
			}
			Map<String,List<String>> map = new HashMap<>();
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					for (String columnLabel : columnNames) {
						List<String> list = map.get(columnLabel);
						if(list == null){
							list = new LinkedList<>();
							map.put(columnLabel, list);
						}
						list.add(rs.getString(columnLabel));
					}
				}
			};
			return map;
		}catch(Exception e){
			
		};
		return null;
	}
	
	private void init(){
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		init();
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	
}
