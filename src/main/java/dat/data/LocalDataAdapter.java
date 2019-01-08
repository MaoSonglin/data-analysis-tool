package dat.data;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.WorkPackageService;
import dat.service.impl.VirtualTableServiceImpl;
import dat.service.impl.VirtualTableServiceImpl.ResultSetHandler;
import dat.util.SqlHelper;
import dat.util.VariableTypeParser;

/**
 * @author MaoSonglin
 * 操作本地数据的适配器
 */
public class LocalDataAdapter implements ApplicationContextAware,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7628186146324507837L;

	private static Logger logger = Logger.getLogger(LocalDataAdapter.class);
	
	private ApplicationContext context;

	public LocalDataAdapter(ApplicationContext context) {
		super();
		this.context = context;
	}
	
	public <T> List<T> query(List<VirtualColumn> columns,SqlBuilder builder,RowHandler<T> handler)throws Exception{
		ResultSetHandler<List<T>> x =	resultSet -> {
			int i = 0;
			List<T> list = new ArrayList<>();
			while(resultSet.next()){
				T row = handler.handRow(i++, resultSet);
				list.add(row);
			}
			return list;
		};
		return query(columns,builder,x);
	}
	
	public <T> T query(List<VirtualColumn> columns,SqlBuilder builder,ResultSetHandler<T> handler)throws Exception{
		// 数据表集合
		Set<VirtualTable> tableSet = new HashSet<>();
		// 遍历数据字段获取对应的字段所在的数据表
		columns.forEach(column->{tableSet.add(column.getTable());});
		
		String wpkg = tableSet.iterator().next().getPackages().iterator().next().getId();
		synchronized (this) {
			try(Connection conn = context.getBean(WorkPackageService.class).getConnection(wpkg);){
				String tableName = null;
				if(tableSet.size() == 1){
					tableName = tableSet.iterator().next().getId();
				}else if(tableSet.size() > 1){
					tableName = createTemporaryTable(conn,tableSet,columns);
				}
				SqlInfo sqlInfo = new SqlInfo() ;
				builder.build(tableName,sqlInfo);
				logger.debug(sqlInfo.getSql());
				logger.debug(sqlInfo.getParams());
				try(PreparedStatement ps = conn.prepareStatement(sqlInfo.getSql())){
					List<Object> params = sqlInfo.getParams();
					for(int i = 0, size = params.size(); i < size; i++){
						ps.setObject(i+1, params.get(i));
					}
					try(ResultSet rs = ps.executeQuery()){
						T t = handler.doResultSet(rs);
						return t;
					}
				}
			}
		}
	}
	
	public Map<String, List<Object>> query(List<VirtualColumn> columns,SqlBuilder builder) throws Exception{
		return query(columns,builder,VirtualTableServiceImpl.getMapListHandler());
	}

	public Map<String, List<Object>> query(List<VirtualColumn> columns) throws Exception{
		// 数据表集合
		Set<VirtualTable> tableSet = new HashSet<>();
		// 遍历数据字段获取对应的字段所在的数据表
		columns.forEach(column->{tableSet.add(column.getTable());});
		
		String wpkg = tableSet.iterator().next().getPackages().iterator().next().getId();
		synchronized (this) {
			try(Connection conn = context.getBean(WorkPackageService.class).getConnection(wpkg);){
				String tableName = null;
				if(tableSet.size() == 1){
					tableName = tableSet.iterator().next().getId();
				}else if(tableSet.size() > 1){
					tableName = createTemporaryTable(conn,tableSet,columns);
				}
				String sql = querySql(conn, tableName, columns);
				logger.debug(sql);
				try(PreparedStatement ps = conn.prepareStatement(sql);ResultSet rs = ps.executeQuery()){
					Map<String, List<Object>> data = VirtualTableServiceImpl.getMapListHandler().doResultSet(rs);
					return data;
				}
			}
		}
	}

	protected SqlInfo buildSql(String tableName,List<VirtualColumn> fields,VirtualColumn groupByColumn,Object...filters){
		Assert.isTrue(filters.length%2==0, "the length of array ‘filters’ must be even numbers");
		ArrayList<VirtualColumn> arrayList = new ArrayList<>(fields);
		arrayList.remove(groupByColumn);
		StringBuffer sb = new StringBuffer("SELECT ");
		SqlInfo sqlInfo = new SqlInfo();
		sb.append(groupByColumn.getName()).append(" , ");
		for (VirtualColumn virtualColumn : arrayList) {
			String typeName = virtualColumn.getTypeName();
			if(typeName.equalsIgnoreCase("number")){
				sb.append("SUM(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}else{
				sb.append("COUNT(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}
		}
		SqlHelper.deleteLast(sb, 3);
		sb.append(" FROM ").append(tableName);
		sb.append(" WHERE ");
		for(int i = 0,length = filters.length; i < length-1; i+=2){
			sb.append(filters[i]).append(" = ? AND ");
			sqlInfo.getParams().add(filters[i+1]);
		}
		sb.append(groupByColumn.getName()).append(" IS NOT NULL ");
		
		sb.append(" GROUP BY ");
		sb.append(groupByColumn.getName());
		sb.append(" ORDER BY ");
		sb.append(groupByColumn.getName());
		sqlInfo.setSql(sb.toString());
		return sqlInfo;
	}

	/**
	 * @param conn
	 * @param tableName
	 * @param columns
	 * @return 
	 * @throws Exception
	 * @throws SQLException
	 */
	private String querySql(Connection conn, String tableName,
			List<VirtualColumn> columns) throws Exception, SQLException {
//		StringBuffer buffer = SqlHelper.selectList(columns, "name", "id");
//		buffer.append(" FROM ").append(tableName);
		
//		Map<VirtualColumn, String> dataType = getDataType(columns, buffer, conn);
		StringBuffer sb = new StringBuffer("SELECT ");
		Iterator<VirtualColumn> iterator = columns.iterator();
		VirtualColumn next = iterator.next();
		sb.append(next.getName()).append(" , ");
		while(iterator.hasNext()){
			VirtualColumn virtualColumn = iterator.next();
//			String typeName = dataType.get(virtualColumn);
			String typeName = virtualColumn.getTypeName();
			if(typeName.equalsIgnoreCase("number")){
				sb.append("SUM(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}else{
				sb.append("COUNT(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}
		}
		SqlHelper.deleteLast(sb, 3);
		sb.append(" FROM ").append(tableName);
		sb.append(" WHERE ").append(next.getName()).append(" IS NOT NULL ");
		sb.append(" GROUP BY ");
		sb.append(next.getName());
		sb.append(" ORDER BY ");
		sb.append(next.getName());
		return sb.toString();
	}
	
	
	@SuppressWarnings("unused")
	private Map<VirtualColumn, String> getDataType(List<VirtualColumn> columns,
			StringBuffer buffer, Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(buffer.toString());
		ResultSet rs = ps.executeQuery();
		VariableTypeParser typeParser = new VariableTypeParser();
		Map<VirtualColumn,String> map = new HashMap<>();
		while(rs.next()){
			columns.forEach(elem->{
				String id2 = elem.getId();
				try {
					// 获取值
					String value = rs.getString(id2);
					// 获取值的数据类型
					typeParser.setVariable(value);
					String typeName2 = typeParser.getTypeName();
					String typeName = map.get(elem);
					if(typeName == null)
						map.put(elem, typeName2);
					else if(!typeName.equalsIgnoreCase(typeName2)){
						if("any".equalsIgnoreCase(typeName2)){
							map.put(elem,typeName);
						}else{
							map.put(elem,"String");
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return map;
	}
	
	
	private String createTemporaryTable(Connection conn,Set<VirtualTable> tableSet,
			List<VirtualColumn> columns) throws Exception{
		logger.debug("建立一个临时表");
		Map<VirtualTable, Set<VirtualColumn>> map = tableMapColumn(tableSet, columns);
		
		int hashCode = tableSet.hashCode();
		// 临时表名称
		String tableName = "temp_"+Math.abs(hashCode);
		// 创建临时表的SQL语句
		StringBuffer sb = createTableSql(columns, tableName);
		// 建立临时表
		createTable(conn, tableName, sb);
		try {
			conn.setAutoCommit(false);
			// 根据各个数据表中记录条数从大到小进行排序
			List<Entry<VirtualTable,Set<VirtualColumn>>> list = new ArrayList<>(map.entrySet());
			sort(conn,list);
			
			// 迭代器
			Iterator<Entry<VirtualTable, Set<VirtualColumn>>> iterator = list.iterator();
			Entry<VirtualTable, Set<VirtualColumn>> next = iterator.next();
			
			// 将数据最多的数据表中的记录先移动到临时表中
			moveFirstTable(tableName, next ,conn);
			
			// 将剩余表中的数据插入到临时表中
			while(iterator.hasNext()){
				Entry<VirtualTable, Set<VirtualColumn>> entry = iterator.next();
				// 数据表
				VirtualTable virtualTable = entry.getKey();
				// 待查询的列
				Set<VirtualColumn> virtualColumns = entry.getValue();
				
				// 拼装SQL语句
				StringBuffer buffer = SqlHelper.selectList(virtualColumns, "name");
				buffer.append(" FROM ").append(virtualTable.getId());
				String sql = buffer.toString();
				
				logger.debug(sql);
				// 从源表中查询出记录，然后将记录插入到临时表
				try(PreparedStatement ps = conn.prepareStatement(sql);
						ResultSet rs = ps.executeQuery();){
					
					// sql语句
					sql = updateSql(tableName, entry.getValue()) +" WHERE ROW_NUM = ?";
					logger.debug(sql);
					try(PreparedStatement ps2 = conn.prepareStatement(sql)){
						while(rs.next()){
							int i = 0,j = 1000;
							for(VirtualColumn vc : virtualColumns){
								String value = rs.getString(vc.getName());
								ps2.setString(++i, value);
							}
							ps2.setInt(++i, rs.getRow());
							ps2.addBatch();
							if(--j==0){
								ps2.executeBatch();
								j = 1000;
								ps2.clearBatch();
							}
						}
						ps2.executeBatch();
					}
				}
				
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally{
			conn.setAutoCommit(true);
		}  
		return tableName;
	}


	/**
	 * @param tableName
	 * @param next
	 * @throws SQLException
	 */
	private void moveFirstTable(String tableName,
			Entry<VirtualTable, Set<VirtualColumn>> next,Connection conn) throws SQLException {
		VirtualTable table = next.getKey();
		String id = table.getId();
		StringBuffer insertSql= new StringBuffer("INSERT INTO ");
		StringBuffer selectSql = new StringBuffer("SELECT ");
		insertSql.append(tableName);
		insertSql.append(" ( ");
		Set<VirtualColumn> value2 = next.getValue();
		value2.forEach(column->{
			insertSql.append(column.getName()).append(" , ");
			selectSql.append(column.getName()).append(" , ");
		});
		SqlHelper.deleteLast(insertSql, 3);
		SqlHelper.deleteLast(selectSql, 3);
		insertSql.append(" ) ");
		selectSql.append(" FROM ").append(id);
		insertSql.append(selectSql);
		String sql = insertSql.toString();
		logger.debug(sql);
		try(Statement st = conn.createStatement()) {
			st.execute(sql);
		}
	}


	private void sort(Connection conn,List<Entry<VirtualTable, Set<VirtualColumn>>> list) throws SQLException {
		logger.debug("根据数据表中的记录数从大到小进行排序");
		Map<String,Integer> map = new HashMap<>();
		for (Entry<VirtualTable, Set<VirtualColumn>> entry : list) {
			VirtualTable table = entry.getKey();
			logger.debug("SELECT COUNT(*) FROM "+table.getId());
			try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM "+table.getId());ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					int count = rs.getInt(1);
					map.put(table.getId(), count);
				}
			}
		}
		list.sort((a,b)->{
			return map.get(a.getKey().getId()) < map.get(b.getKey().getId()) ? 1 : -1;
		});
	}


	/**
	 * @param conn
	 * @param tableName
	 * @param sb
	 * @throws SQLException
	 */
	private void createTable(Connection conn, String tableName, StringBuffer sb)
			throws SQLException {
		conn.setAutoCommit(false);
		try(Statement statement = conn.createStatement();){
			logger.debug("DROP TABLE IF EXISTS "+tableName);
			statement.addBatch("DROP TABLE IF EXISTS "+tableName);
			logger.debug(sb);
			statement.addBatch(sb.toString());
			statement.executeBatch();
			conn.commit();
		} catch(SQLException e){
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(true);
		}
	}

	/**
	 * @param tableSet
	 * @param columns
	 * @return
	 */
	private Map<VirtualTable, Set<VirtualColumn>> tableMapColumn(
			Set<VirtualTable> tableSet, List<VirtualColumn> columns) {
		Map<VirtualTable,Set<VirtualColumn>> map = new HashMap<>();
		tableSet.forEach(table->{
			List<VirtualColumn> list = table.getColumns();
			map.put(table, new HashSet<>());
			columns.forEach(column->{
				if(list.contains(column))
					map.get(table).add(column);
			});
		});
		return map;
	}

	private String updateSql(String tableName, Set<VirtualColumn> virtualColumns) {
		StringBuffer sb = new StringBuffer("UPDATE ");
		sb.append(tableName);
		sb.append(" SET ");
		virtualColumns.forEach(column->{
			String name = column.getName();
			sb.append(name).append(" = ? , ");
			
		});
		sb.delete(sb.length()-3, sb.length());
		return sb.toString();
	}


	/**
	 * @param columns
	 * @param tableName
	 * @return
	 */
	private StringBuffer createTableSql(List<VirtualColumn> columns,
			String tableName) {
//		StringBuffer sb = new StringBuffer("CREATE TABLE ");
		StringBuffer sb = new StringBuffer("CREATE TEMPORARY TABLE ");
		sb.append(tableName);
		sb.append(" ( ");
		sb.append("ROW_NUM Integer PRIMARY KEY AUTOINCREMENT , ");
		columns.forEach(column->{
			String name = column.getName();
			sb.append(name).append(' ').append("varchar(255)").append(" , ");
		});
		sb.delete(sb.length()-3, sb.length());
		sb.append(" ) ");
		return sb;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}
	
	public static interface SqlBuilder{
		void build(String tableName,SqlInfo sqlInfo);
	}

	public static interface RowHandler<T>{
		T handRow(int rowNum,ResultSet rs) throws SQLException;
	}
}
