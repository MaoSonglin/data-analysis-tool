package dat.data;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;

public class VirtualTableConnManager implements InvocationHandler,Serializable {
	


	private static Logger logger = Logger.getLogger(VirtualTableConnManager.class.getName());
	private static final long serialVersionUID = 1592222980328103165L;
	private VirtualTable table;
	
	// 实体数据表的数据库连接
//	private List<Connection> connList;
	private List<VirtualColumn> columns;
	private HashMap<DataTable, Set<TableColumn>> tableColumnMap;
	private HashMap<Source,Connection> connMap;
	private PreparedStatement ps;
	// 虚拟字段名称与该字段对应的实体字段名称的映射
	private Map<String,Set<String>> nameMap;
//	private ApplicationContext context;
//	private VirtualTableRepository virtualTableRepository;
//	
	private List<PreparedStatement> psList;
	
	private VirtualTableConnManager(VirtualTable table) throws Exception {
		super();
		this.table = table;
		nameMap = new HashMap<>();
		tableColumnMap = new HashMap<>();
		connMap = new HashMap<>();
		columns = this.table.getColumns();
		
		createConnections();
	}

	/**
	 * 遍历数据表引用的所有实体字段，建立访问实体字段的数据源连接
	 */
	private void createConnections() {
		// 遍历数据表中的字段
		for (VirtualColumn vc : columns) {
			// 遍历字段引用的实体字段,获取实体字段所在的数据源信息，并建立数据源连接，将连接保存为数据源到连接的映射
			for (TableColumn tableColumn : vc.getRefColumns()) {
				Source source = tableColumn.getDataTable().getSource();
				// 来自同一个数据源的字段公用同一个数据源连接
				// 所以先检查数据源连接是否已经建立了，如果没有则添加新的连接
				Connection conn = connMap.get(source);
				if(conn == null){
					conn = DataSourceConnManager.getConnection(source);
					connMap.put(source, conn);
				}
			}
		}
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String name = method.getName();
		logger.info("try to invoke method named "+name+" with arguments "+Arrays.toString(args));
		if("close".equals(name)){
			close();
			return null;
		}else if("isClose".equals(name)){
			return isClose();
		}else if("prepareStatement".equals(name)){
			if(args.length == 1){
				return prepareStatement(args[0].toString());
			}
		}
		throw new Exception("The method "+name+" with parameter "+Arrays.toString(args)+" is not implemented!");
	}

	@SuppressWarnings("deprecation")
	public PreparedStatement prepareStatement(String sql) throws Exception {
		tableColumnMap.clear();
		// SQL语句解析器
		SqlParser parser = new SelectSqlParser(sql);
		// 标记是否查询出所有的列
		boolean f = false;
		// 获取要查询的列
		List<String> columnLabel = parser.getColumnLabel();
		// 如果待查询的列名称数组为空或者只有一项且是“*”，则查询出所有的列
		if(columnLabel.isEmpty() || (columnLabel.size()==1 && ("*".equals(columnLabel.get(0))))){
			f = true;
		}
		// 遍历数据表table中存在的所有列
		for (VirtualColumn virtualColumn : columns) {
			if(f){// 添加所有字段为查询列
				addQueryColumn(virtualColumn);
			}else{
				// 获取列名称
				String name = virtualColumn.getName();
				// 如果当前列包含在待查询列表中
				if(columnLabel.contains(name)){
					addQueryColumn(virtualColumn);
				}
			}
		}
		psList = new ArrayList<>();
		// 遍历待查询的数据表和待查询的字段
		Set<Entry<DataTable,Set<TableColumn>>> entrySet = tableColumnMap.entrySet();
		for (Entry<DataTable, Set<TableColumn>> entry : entrySet) {
			DataTable dataTable = entry.getKey();	// 待访问的数据表
			Source source = dataTable.getSource();	// 带访问的数据表所属的数据源
			// 获取连接
			Connection conn = connMap.get(source);	
			// 创建SQL语句
			String querySql = constructSqlIn(dataTable, entry.getValue());
			logger.info("执行SQL语句："+querySql);
			// SQL语句的预处理接口
			ps = conn.prepareStatement(querySql);
			psList.add(ps);
		}
		logger.info("获取查询语句对象个数："+psList.size());
		PreparedStatement ps = VirtualTableStateManager.getPreparedStatement(this);
		return ps;
	}

	/**
	 * 1. 添加待查的数据列和待查询的列所在实体数据表映射<br>
	 * 2. 添加待查询的数据列名称与该列引用的实体数据列名称的映射
	 * @param virtualColumn	待查的虚拟数据表的字段信息
	 */
	private void addQueryColumn(VirtualColumn virtualColumn) {
		
		// 获取关联的实体字段
		List<TableColumn> refColumns = virtualColumn.getRefColumns();
		// 遍历关联的实体字段
		for (TableColumn tableColumn : refColumns) {
			// 实体字段所属数据表
			DataTable dataTable = tableColumn.getDataTable();
			// 保存数据表与需要查询的字段映射
			Set<TableColumn> set = tableColumnMap.get(dataTable);
			if(set == null){
				set = new HashSet<>();
				tableColumnMap.put(dataTable, set);
			}
			set.add(tableColumn);
			
			// 添加虚拟字段名称到该字段引用的实体字段的映射
			String name = virtualColumn.getName();
			Set<String> list = nameMap.get(name);
			if(list == null){
				list = new HashSet<>();
				nameMap.put(name, list);
			}
			String columnName = tableColumn.getColumnName();
			list.add(columnName);
		}
	}

	/**
	 * 构建在数据表dataTable上查询出指定列columns的SQL语句
	 * @param dataTable	待查询的数据表信息
	 * @param columns	待检索的字段列表
	 * @return			SQL语句字符串
	 */
	private String constructSqlIn(DataTable dataTable, Set<TableColumn> columns) {
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		for (TableColumn tableColumn : columns) {
			// select columnName ,
			sb.append(tableColumn.getColumnName())/*.append(' ').append(tableColumn.getId())*/.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		// selection columnName[,columnName...] from tableName
		sb.append(" from ");
		sb.append(dataTable.getName());
		String querySql = sb.toString();
		return querySql;
	}
	
	/*public static void getColumn(String sql){
		char[] charArray = sql.toCharArray();
		StringBuffer sb = new StringBuffer();
		Pattern compile = Pattern.compile("\\s");
		List<String> columnLabels = new ArrayList<>();
		boolean startColumn = false;
		boolean startTable = false;
		for (char c : charArray) {
			boolean isSpace = Pattern.matches("\\s", Character.toString(c));
			if((isSpace||c == ',') && sb.length()>0){
				String world = sb.toString();
				sb.delete(0, sb.length());
				if("select".equalsIgnoreCase(world)){
					startColumn = true;
					startTable = false;
					continue;
				}else if("from".equalsIgnoreCase(world)){
					startColumn = false;
					startTable = true;
					continue;
				}else if("as".equalsIgnoreCase(world)){
					continue;
				}
				if(startColumn){
					columnLabels.add(world);
				}
				if(startTable){
					
				}
			} 
			sb.append(c);
		}
	}*/

	public boolean isClose() throws SQLException {
		for(Connection conn : connMap.values()){
			if(!conn.isClosed())
				return false;
		}
		return true;
	}

	public void close() throws SQLException {
		for(Connection conn : connMap.values()){
			conn.close();
		}
	}
	
	

	public VirtualTable getTable() {
		return table;
	}

	public void setTable(VirtualTable table) {
		this.table = table;
	}

	public List<VirtualColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<VirtualColumn> columns) {
		this.columns = columns;
	}

	public HashMap<DataTable, Set<TableColumn>> getTableColumnMap() {
		return tableColumnMap;
	}

	public void setTableColumnMap(
			HashMap<DataTable, Set<TableColumn>> tableColumnMap) {
		this.tableColumnMap = tableColumnMap;
	}

	public HashMap<Source, Connection> getConnMap() {
		return connMap;
	}

	public void setConnMap(HashMap<Source, Connection> connMap) {
		this.connMap = connMap;
	}

	public PreparedStatement getPs() {
		return ps;
	}

	public void setPs(PreparedStatement ps) {
		this.ps = ps;
	}

	public Map<String, Set<String>> getNameMap() {
		return nameMap;
	}

	public void setNameMap(Map<String, Set<String>> nameMap) {
		this.nameMap = nameMap;
	}

	public List<PreparedStatement> getPsList() {
		return psList;
	}

	public void setPsList(List<PreparedStatement> psList) {
		this.psList = psList;
	}

	public static Connection getConnection(VirtualTable table) throws Exception{
		VirtualTableConnManager handler = new VirtualTableConnManager(table);
		return (Connection) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class<?>[]{Connection.class}, handler);
	}
	@Deprecated
	public interface SqlParser {

		/**
		 * 获取查询的列名称
		 * @return
		 */
		List<String> getColumnLabel();

		/**
		 * 获取查询的数据表名称
		 * @return
		 */
		String getTableName();
		
		/**
		 * 获取查询的数据表别名
		 * @return
		 */
		String getTableAlias();
		
		/**
		 * 获取所有待查的数据字段的别名
		 * @return
		 */
		List<String> getColumnAlias();
		
		/**
		 * 获取所有连接的数据表
		 * @return
		 */
		List<String> getJoinTables();
		
		List<String> getJoinTableAlias();
		
		default int getOffset(){
			return 0;
		}
		
		default int getLimit(){
			return 1000;
		}

	}

}


/**
 * @author MaoSonglin <br>
 * 解析查询语句格式如下：<br>
 * 	[columnName|columnAlias....][tableName|tableAlias][table.column=table.column]
 * 	[where][offset,size]
 */
@SuppressWarnings("unused")
class MySqlParser implements dat.data.SqlParser{
	
	private String sql;
	private int selectIndex;
	private int fromIndex;
	private int whereIndex;
	private int limitIndex;
	
	public MySqlParser(String sql) {
		this.sql = sql.toLowerCase();
		selectIndex = this.sql.indexOf("select");
		fromIndex = this.sql.indexOf("from");
		whereIndex = this.sql.indexOf("where");
		limitIndex = this.sql.indexOf("limit");
		if(selectIndex < 0){
			throw new IllegalArgumentException("sql error: do not find key word of 'select'");
		}
		if(fromIndex < 0){
			throw new IllegalArgumentException("sql error: do not find key word of 'from'");
		}
	}

	public List<Renameable> getColumns() {
		// 获取关键词 “select” 与“from”之间的部分
		String string = sql.substring(selectIndex+7, fromIndex-1).trim();
		// select column1,column2 from table,安装“，”分割
		String[] split = string.split(",");
		List<Renameable> columns = new ArrayList<>();
		for (String str : split) {
			Renameable column = getColumn(str);
			columns.add(column);
		}
		return columns;
	}

	/**
	 * @param tmp
	 */
	private Renameable getColumn(String tmp) {
		String[] array = tmp.split("\\s+");
		switch(array.length){
		case 1:
			return new ColumnName(array[0], null, null);
		case 2:
			return new ColumnName(array[0], array[1], null);
		case 3:
			return new ColumnName(array[0], array[2], array[1]);
		default:
			throw new IllegalArgumentException(tmp);
		}
	}

	@Override
	public Renameable getTable() {
		int indexOf = sql.indexOf("join");
		if(indexOf != -1){
			sql.substring(fromIndex, indexOf);
		}
		return dat.data.SqlParser.super.getTable();
	}

	@Override
	public List<JoinAble> getJoinTables() {
		// TODO Auto-generated method stub
		return dat.data.SqlParser.super.getJoinTables();
	}

	@Override
	public int getOffset() {
		// TODO Auto-generated method stub
		return dat.data.SqlParser.super.getOffset();
	}

	@Override
	public int getMax() {
		// TODO Auto-generated method stub
		return dat.data.SqlParser.super.getMax();
	}

	@Override
	public List<FilterCondition> getWhere() {
		// TODO Auto-generated method stub
		return dat.data.SqlParser.super.getWhere();
	}
	
}

class ColumnName implements Renameable{

	String columnName;
	
	String aliasName;
	
	String condition;
	
	public ColumnName(String columnName, String aliasName, String condition) {
		super();
		this.columnName = columnName;
		this.aliasName = aliasName;
		this.condition = condition;
	}

	public String getName() {
		return columnName;
	}

	public String getAlias() {
		return aliasName;
	}

	public String getConjunction() {
		return condition;
	}
	
}
