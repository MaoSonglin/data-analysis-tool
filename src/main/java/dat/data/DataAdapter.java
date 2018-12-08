package dat.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import dat.domain.DataTable;
import dat.domain.ForeignKey;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.DsRepository;
import dat.service.DataSourceService;
import dat.service.TableColumnService;
import dat.service.VirtualTableService;
import dat.util.DialectUtil;
import dat.util.FormulaParser;

public interface DataAdapter extends Iterable<Map<String,String>>,Closeable {
	
	void filter(String where);
	
	int clearFilter();
	
	void limit(int offset,int size);
}


class SingleSourceDataAdapter implements DataAdapter, Iterator<Map<String, String>>{
	
	private static Logger logger = LoggerFactory.getLogger(SingleSourceDataAdapter.class);
	
	private ApplicationContext context;
	
	/**
	 * 需要访问的实体数据表
	 */
	private List<DataTable> dataTables;
	
	/**
	 * 需要查询的虚拟数据表字段
	 */
	private List<VirtualColumn> columns;

	/**
	 * 数据库连接
	 */
	private Connection conn;

	/**
	 * 结果过滤条件数组
	 */
	private List<String> filters = new ArrayList<>();

	/**
	 * 当前行数据射影
	 */
	private Map<String,String> row;

	/**
	 * 数据库方言
	 */
	private Dialect dialect;

	/**
	 * 分页设置对象
	 */
	private RowSelection selection;

	private PreparedStatement ps;

	private ResultSet rs;

	/**
	 * 待查询的实体数据表字段
	 */
	private List<TableColumn> tableColumns;
	
	public SingleSourceDataAdapter(ApplicationContext context) {
		super();
		this.context = context;
	}

	/**
	 * 初始化
	 */
	private void init() {
		Set<Source> s = new HashSet<>();
		for (DataTable table : this.dataTables) {
			Source source = table.getSource();
			s.add(source);
		}
		if(s.size()>1){
			throw new IllegalArgumentException("dataTables 中的数据表不是同源数据表");
		}
		try {
			// 获取数据库连接
			DataSourceService dataSourceService = context.getBean(DataSourceService.class);
			Source next = s.iterator().next();
			conn = dataSourceService.getTemplate(next).getDataSource().getConnection();
			// 获取数据库方言
			dialect = DialectUtil.getDialect(next.getDatabaseName());
		} catch (SQLException e) {
			throw new IllegalArgumentException("get connection fail!");
		}
		tableColumns = context.getBean(TableColumnService.class).getColumnsByVirtualColumns(columns);
	}

	public Iterator<Map<String, String>> iterator() {
		try {
			if(conn == null)
				init();
			queryInTable(dataTables,tableColumns);
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 初始化查询
	 * @param dataTables	需要查询的数据表
	 * @param columns2		带检索的虚拟字段
	 * @throws SQLException
	 */
	private void queryInTable(List<DataTable> dataTables,List<TableColumn> columns2) throws SQLException {
		DataTable dataTable = dataTables.remove(0);
		StringBuffer sb = new StringBuffer("SELECT ");
		fromColumns(sb, columns2);
		sb.append(" FROM ");
		sb.append(dataTable.getName());
		
		// 已经添加到查询列表的数据表
		List<DataTable> joinTables = new ArrayList<>(dataTables.size());
		// 将dataTable加入
		joinTables.add(dataTable);
		// 遍历已经加入的数据表
		joinTables.forEach(joinTable->{
			// 获取当前数据表jointable的外键信息
			List<ForeignKey> foreignKeys = joinTable.getForeignKeys();
			multiplyTableJoin(dataTables, joinTables, foreignKeys, sb);
			List<ForeignKey> quotes = joinTable.getQuotes();
			multiplyTableJoin(dataTables,joinTables,quotes,sb);
		});
		String sql = sb.toString();
		if(selection != null){
			LimitHandler limitHandler = this.dialect.getLimitHandler();
			sql = limitHandler.processSql(sql, selection);
			ps = conn.prepareStatement(sql);
			limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
		}else{
			ps = conn.prepareStatement(sql);
		}
		logger.debug(sb.toString());
		rs = ps.executeQuery();
	}

	/**
	 * 多表连接	
	 * @param dataTables	为加入连接的数据表
	 * @param joinTables	已经加入连接的数据表
	 * @param foreignKeys	外键关系
	 * @param sb			SQL语句
	 */
	private void multiplyTableJoin(List<DataTable> dataTables, List<DataTable> joinTables,
			List<ForeignKey> foreignKeys, StringBuffer sb) {
		// 遍历外键
		for (ForeignKey foreignKey : foreignKeys) {
			// 获取外键关联的数据表
			DataTable foreignTable = foreignKey.getForeignTable();
			// 如果该数据表包含在待查询数据表集合中
			if(dataTables.remove(foreignTable)){
				// 添加join查询SQL语句
				// 外键参考数据表的名称
				String name = foreignTable.getName();
				String pkTname = foreignKey.getPrimaryTable().getName();
				// 外键字段名称
				String columnName = foreignKey.getForeignColumn().getColumnName();
				// 外键引用的字段名称
				String pkColumnName = foreignKey.getPrimaryColumn().getColumnName();
				sb.append(" join ")
				.append(pkTname)
				.append(" on ")
				.append(name).append('.').append(columnName)
				.append('=').append(pkTname).append('.').append(pkColumnName);
				// 将关联的表添加到joinTables
				joinTables.add(foreignTable);
			}
		}
	}

	/**
	 * @param sb
	 * @param columns2
	 */
	private void fromColumns(StringBuffer sb, List<TableColumn> columns2) {
		for (TableColumn tableColumn : columns2) {
			sb.append(tableColumn.getColumnName());
			sb.append(' ');
			sb.append(tableColumn.getId());
			sb.append(',');
		}
		sb.deleteCharAt(sb.length()-1);
	}

	@Override
	public void close() throws IOException {
		try {
			if(rs != null)
				rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			if(ps != null)
				ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			if(conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	@Override
	public void filter(String where) {
		filters.add(where);
	}

	@Override
	public int clearFilter() {
		int s = filters.size();
		filters.clear();
		return s;
	}

	@Override
	public void limit(int offset, int size) {
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size);
	}

	@Override
	public boolean hasNext() {
		return getLine();
	}

	@Override
	public Map<String, String> next() {
		if(row == null)
			getLine();
		Map<String,String> tmp = row;
		row = null;
		return tmp;
	}

	/**
	 * 获取一行数据
	 */
	private boolean getLine() {
		try {
			if(row != null ) return true;
			
			while(rs.next()){
				// 存放一行数据的map
				Map<String,String> map = new HashMap<>();
				// 遍历查询的字段数组，用字段的ID从结果集中获取结果
				for(TableColumn tableColumn : tableColumns){
					String columnLabel = tableColumn.getId();
					String value = rs.getString(columnLabel);
					// 保存字段ID到子弹值的映射
					map.put(columnLabel, value);
				}
				// 计算的虚拟字段值映射
				row = new HashMap<>();
				// 遍历待查询的虚拟子弹
				for (VirtualColumn column : columns) {
					// 获取虚拟字段与实体字段的映射关系
					String formula = column.getFormula();
					// 解析映射关系并根据实体字段数据计算得到虚拟字段的值
					FormulaParser parser = new FormulaParser(formula, map);
					String value = parser.getValue();
					// 保存
					row.put(column.getName(), value);
				}
				// 过滤条件是否满足
				boolean f = false;
				for(String filter : this.filters){
					f = f || FilterUtils.satisfy(filter, row);
				}
				if(f) // 如果改行数据满足所有的过滤条件，则跳过本条数句，获取下一行数据
					continue;
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setDataTables(List<DataTable> dataTables) {
		this.dataTables = dataTables;
	}

	public void setColumns(List<VirtualColumn> columns) {
		this.columns = columns;
	}
	
}




@SuppressWarnings("unused")
class MultipleSourceDataAdapter implements DataAdapter,Serializable{

	private static final long serialVersionUID = -872987029738756872L;
	
	private static Logger logger = LoggerFactory.getLogger(MultipleSourceDataAdapter.class);
	
	private ApplicationContext context;
	
	private Collection<VirtualColumn> virtualColumns;
	
	private Collection<TableColumn> tableColumns;
	
	private Collection<DataTable> dataTables;
	
	private Collection<Source> sources;
	
	private RowSelection selection;
	
	public MultipleSourceDataAdapter(ApplicationContext context){
		this.context = context;
		init();
	}
	
	protected void setQueryVirtualTable(VirtualTable table){
		VirtualTableService virtualTableService = context.getBean(VirtualTableService.class);
		List<DataTable> quoteTable = virtualTableService.getQuoteTable(table);
		Set<String> ids = new HashSet<>();
		quoteTable.forEach(elem->{ids.add(elem.getId());});
		Specification<Source> spec = (root,query,cb)->{
			Join<Source, DataTable> join = root.join("tables");
			Predicate predicate = join.get("id").in(ids);
			return predicate;
		};
		List<Source> ss = context.getBean(DsRepository.class).findAll(spec);
		sources = new HashSet<>(ss);
		if(sources.size() == 1){
			logger.warn("virtual table %s contains only one source", table.getName());
		}
		if(!hasTable(table)){
			createTable(table);
			loadData(table);
		}
	}

	private void loadData(VirtualTable table) {
		TableColumnService tableColumnService = context.getBean(TableColumnService.class);
		List<VirtualColumn> virtualColumns = table.getColumns();
		tableColumns = tableColumnService.getColumnsByVirtualColumns(virtualColumns);
		Map<Long, DataTable> map = findCount();
		ArrayList<Long> list = new ArrayList<>(map.keySet());
		list.sort(null);
		for(int index = list.size()-1, f = 0; index >= 0; index--){
			Long key = list.get(index);
			DataTable dataTable = map.get(key);
			List<Map<String, String>> query = queryTable(virtualColumns,dataTable);
			for (Map<String, String> map2 : query) {
				String sql = null;
				List<String> values = new ArrayList<>();
				Set<Entry<String,String>> entrySet = map2.entrySet();
				if((f++) == 0){// 插入 
					StringBuffer insertList = new StringBuffer("(");
					StringBuffer valueList = new StringBuffer(" values (");
					for (Entry<String, String> entry : entrySet) {
						String value = entry.getValue();
						if(value != null){
							insertList.append(entry.getKey()).append(",");
							valueList.append("?").append(",");
							values.add(value);
						}
					}
					insertList.deleteCharAt(insertList.length()-1);
					valueList.deleteCharAt(valueList.length()-1);
					insertList.append(')');
					valueList.append(')');
					insertList.insert(0, table.getName());
					insertList.insert(0, "insert into ");
					insertList.append(valueList);
					sql = insertList.toString();
				}else{// 修改
					for (Entry<String, String> entry : entrySet) {
						
					}
				}
				try(Connection conn= getConnection(table);PreparedStatement ps = conn.prepareStatement(sql)){
				} catch(Exception e){
					
				}
			}
		}
	}

	/**
	 * @param virtualColumns
	 * @param dataTable
	 * @return
	 */
	private List<Map<String, String>> queryTable(
			List<VirtualColumn> virtualColumns, DataTable dataTable) {
		List<TableColumn> tableColumns = dataTable.getColumns();
		String sql = constructSQL(tableColumns);
		DataSourceService sourceService = context.getBean(DataSourceService.class);
		JdbcTemplate template = sourceService.getTemplate(dataTable.getSource());
		List<Map<String,String>> query = template.query(sql, new Object[]{}, (rs,i)->{
			Map<String,String> m = new HashMap<>(tableColumns.size());
			for(TableColumn tableColumn : tableColumns){
				String id = tableColumn.getId();
				String value = rs.getString(id);
				m.put(id, value);
			}
			Map<String,String> x = new HashMap<>(virtualColumns.size());
			for(VirtualColumn virtualColumn : virtualColumns){
				String formula = virtualColumn.getFormula();
				String value = new FormulaParser(formula, m).getValue();
				x.put(virtualColumn.getName(), value);
			}
			
			return x;
		});
		return query;
	}

	/**
	 * @return
	 */
	private Map<Long, DataTable> findCount() {
		DataSourceService sourceService = context.getBean(DataSourceService.class);
		Map<Long,DataTable> map = new HashMap<>(dataTables.size()); 
		dataTables.forEach(elem->{
			// jdbc操作模板类
			JdbcTemplate template = sourceService.getTemplate(elem.getSource());
			String selectCount = "select count(*) from "+elem.getName();
			Long count = template.query(selectCount, new Object[]{}, (rs)->{
				if(rs.next())
					return rs.getLong(1);
				return null;
			});
			map.put(count, elem);
		});
		return map;
	}

	/**
	 * @param virtualColumns
	 * @param columns
	 * @return
	 */
	private RowMapper<Map<String, String>> getRowMapper(
			List<VirtualColumn> virtualColumns, List<TableColumn> columns) {
		return (rs,row)->{
			Map<String,String> map = new HashMap<>(columns.size());
			for (TableColumn i : columns) {
				String columnName = i.getColumnName();
				String value = rs.getString(columnName);
				String newLabel = i.getId();
				map.put(newLabel,value);
			}
			Map<String,String> m = new HashMap<>(virtualColumns.size());
			for(VirtualColumn column : virtualColumns){
				String formula = column.getFormula();
				FormulaParser parser = new FormulaParser(formula, map);
				m.put(column.getName(), parser.getValue());
			}
			return m;
		};
	}

	/**
	 * @param columns
	 * @return
	 */
	private String constructSQL(List<TableColumn> columns) {
		// SQL语句
		StringBuffer sb =  new StringBuffer("select ");
		columns.forEach(column->{
			sb.append(column.getColumnName()).append(" as ").append(column.getId()).append(',');
		});
		sb.deleteCharAt(sb.length()-1);
		String sql = sb.toString();
		logger.debug(sql);
		return sql;
	}

	private boolean hasTable(VirtualTable table) {
		try(Connection conn = getConnection(table)) {
			DatabaseMetaData metaData = conn.getMetaData();
			try(ResultSet rs = metaData.getTables(null, null, table.getName(), new String[]{"TABLE"})){
				return rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 新建数据表
	 * @param table
	 */
	private void createTable(VirtualTable table) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("create table if not exists  \"").append(table.getName()).append('"').append('(');
		table.getColumns().forEach(column->{
			buffer.append(column.getName()).append(' ').append("varchar(255)").append(',');
		});
		buffer.deleteCharAt(buffer.length()-1);
		buffer.append(')');
		try (Connection conn = getConnection(table);
				Statement st = conn.createStatement();){
			st.execute(buffer.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Connection getConnection(VirtualTable table){
		String tmpdir = System.getProperty("java.io.tmpdir");
		String userdir = System.getProperty("user.dir");
		tmpdir = context.getBean(Environment.class).getProperty("db.cache.dir", tmpdir);
		Path path = Paths.get(userdir);
		Path fileName = path.getFileName();
		String str = Paths.get(tmpdir, fileName.toString(),MultipleSourceDataAdapter.class.getSimpleName(),table.getName()+".db3").toString();
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:"+str);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 */
	private void init() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		String userdir = System.getProperty("user.dir");
		tmpdir = context.getBean(Environment.class).getProperty("db.cache.dir", tmpdir);
		Path path = Paths.get(userdir);
		Path fileName = path.getFileName();
		File file = Paths.get(tmpdir, fileName.toString(),MultipleSourceDataAdapter.class.getSimpleName()).toFile();
		if(!file.isDirectory()){
			file.mkdirs();
		}
	}
	
	@Override
	public Iterator<Map<String, String>> iterator() {
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void filter(String where) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int clearFilter() {
		return 0;
	}

	@Override
	public void limit(int offset, int size) {
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size);
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}
	
} 


















