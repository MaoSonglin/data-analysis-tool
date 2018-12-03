package dat.data;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.util.Classify;
import dat.util.DialectUtil;


public class QueryHelper implements Serializable{
	public static void main(String[] args) {
		List<String> segments = FilterUtils.getSegments("name like 2 ");
		System.out.println(segments);
	}
	
	private static final long serialVersionUID = -5188110890420853846L;
	
	private VirtualTable table;

	private ArrayList<DataTable> dataTables;
	
	
	public DataAdapter query(List<VirtualColumn> columns){
		boolean b = table.getColumns().containsAll(columns);
		if(!b){
			throw new IllegalArgumentException("query list contains one or more columns not included in table \""+table.getName()+"\"");
		}
		// 获取主表
		DataTable table = findPrimaryDataTable();
		// 如果不包含其他的表
		if(dataTables.isEmpty()){
			List<TableColumn> clst = retriveColumnInVirtualTable(columns,table);
			return new SingleTableDataAdapter(table,clst);
		}else{
			// 遍历其他的表
			for (DataTable dataTable : dataTables) {
				// 获取两个数据表之前的关联关系
				List<TableColumn[]> relation = getRelation(table, dataTable);
				if(relation.isEmpty()){// 没有关联关系
					
				}else{	// 存在一个或者多个关联的字段
					
				}
			}
		}
		return null;
	}
	
	private List<TableColumn> retriveColumnInVirtualTable(
			List<VirtualColumn> columns, DataTable table2) {
		List<TableColumn> c = new ArrayList<>();
		for (VirtualColumn virtualColumn : columns) {
			List<TableColumn> refColumns = virtualColumn.getRefColumns();
			for (TableColumn tableColumn : refColumns) {
				if(tableColumn.getDataTable().equals(table2)){
					c.add(tableColumn);
				}
			}
		}
		return c;
	}

	/**
	 * 找到虚拟数据表中应用字段最多的那个数据表
	 * @return
	 */
	protected DataTable findPrimaryDataTable(){
		List<VirtualColumn> columns = table.getColumns();
		Map<DataTable,Integer> map = new HashMap<>();
		// 遍历所有字段
		for (VirtualColumn virtualColumn : columns) {
			List<TableColumn> refColumns = virtualColumn.getRefColumns();
			// 遍历该字段引用的实体字段
			for (TableColumn tableColumn : refColumns) {
				// 获取数据表
				DataTable dataTable = tableColumn.getDataTable();
				// 记录数据表应应用的次数
				Integer integer = map.get(dataTable);
				if(integer==null){
					map.put(dataTable, 1);
				}else{
					map.put(dataTable, integer+1);
				}
			}
		}
		dataTables = new ArrayList<DataTable>(map.keySet());
		dataTables.sort((a,b)->{
			return map.get(a) > map.get(b) ? 0 : 1;
		});
		return dataTables.remove(0);
	}


	protected List<TableColumn[]> getRelation(DataTable t1,DataTable t2){
		Map<Integer, Set<TableColumn>> map = getCategory(t1.getColumns(), t2.getColumns());
		Set<Entry<Integer,Set<TableColumn>>> entrySet = map.entrySet();
		List<TableColumn[]> list = new ArrayList<>();
		for (Entry<Integer, Set<TableColumn>> entry : entrySet) {
			Set<TableColumn> value = entry.getValue();
			ArrayList<TableColumn> arrayList = new ArrayList<>(value);
			int size = arrayList.size();
			for(int i = 0; i < size-1; i++){
				for(int j = i+1; j< size; j++){
					TableColumn c1 = arrayList.get(i);
					TableColumn c2 = arrayList.get(j);
					boolean f = t1.getColumns().contains(c1) && t2.getColumns().contains(c2);
					if(f){
						TableColumn[] relation = new TableColumn[2];
						relation[0] = c1;
						relation[1] = c2;
						list.add(relation);
					}
					f = t2.getColumns().contains(c1) && t1.getColumns().contains(c2);
					if(f){
						TableColumn[] relation = new TableColumn[2];
						relation[0] = c2;
						relation[1] = c1;
						list.add(relation);
					}
				}
			}
		}
		return list;
	}

	/**
	 * @param columns
	 * @param columns2
	 * @return
	 */
	private Map<Integer, Set<TableColumn>> getCategory(
			List<TableColumn> columns, List<TableColumn> columns2) {
		Map<Integer,Set<TableColumn>> map = new HashMap<>();
		classify(columns, map);
		classify(columns2,map);
		return map;
	}

	/**
	 * @param columns
	 * @param map
	 */
	private void classify(List<TableColumn> columns,
			Map<Integer, Set<TableColumn>> map) {
		Classify classify = new Classify();
		for (TableColumn tableColumn : columns) {
			classify.setCnName(tableColumn.getChinese());
			classify.setEnName(tableColumn.getColumnName());
			classify.setDataType(tableColumn.getTypeName());
			Integer category = classify.getCategory();
			Set<TableColumn> set = map.get(category);
			if(set == null){
				set = new HashSet<>();
				map.put(category, set);
			}
			set.add(tableColumn);
		}
	}
	
	
}

class SingleTableDataAdapter implements DataAdapter{
	private static Logger logger = LoggerFactory.getLogger(SingleTableDataAdapter.class);
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	private List<TableColumn> columns;
	private List<String> filters;
	private LimitHandler limitHandler;
	private RowSelection selection;
	private DataTable dataTable;
	
	public SingleTableDataAdapter(DataTable dataTable,List<TableColumn> columns) {
		super();
		// 数据源
		filters = new ArrayList<>();
		Source source = dataTable.getSource();
		limitHandler = DialectUtil.getDialect(source.getDatabaseName()).getLimitHandler();
		this.columns = columns;
		this.dataTable = dataTable;
		try {
			Class.forName(source.getDriverClass());
			// 数据源连接
			conn = DriverManager.getConnection(source.getUrl(), source.getUsername(), source.getPassword());
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		
	}
	private void toQuery() throws SQLException{
		if(rs == null){
			initQuery();
		}
	}
	/**
	 * @throws SQLException
	 */
	private void initQuery() throws SQLException {
		String sql = constructSql(dataTable, columns);
		logger.debug(sql);
		ps = conn.prepareStatement(sql);
		if(selection!=null)
			limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
		rs = ps.executeQuery();
	}

	/**
	 * @param dataTable
	 * @param columns
	 * @return
	 */
	private String constructSql(DataTable dataTable, List<TableColumn> columns) {
		StringBuffer sb = new StringBuffer("select ");
		for (TableColumn tableColumn : columns) {
			String columnName = tableColumn.getColumnName();
			sb.append(columnName).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from ");
		sb.append(dataTable.getName());
		String sql = sb.toString();
		if(selection != null){
			return limitHandler.processSql(sql, selection);
		}
		return sql;
	}

	public Iterator<Map<String, String>> iterator() {
		return this;
	}

	public boolean hasNext() {
		try {
			toQuery();
			boolean last = rs.isLast();
			return last;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<String, String> next() {
		try {
			toQuery();
			while(rs.next()){// 获取结果集中的下一行数据
				// 存放数据的map集合
				Map<String,String> map = new HashMap<>();
				// 遍历要查询的列
				for (TableColumn tableColumn : columns) {
					String columnName = tableColumn.getColumnName();
					String value = rs.getString(columnName);
					map.put(columnName, value);
				}
				boolean f = false;
				// 遍历过滤条件
				for(String filter : filters){
					f =  FilterUtils.satisfy(filter, map);
					if(f){
						break;
					}
				}
				// 如果满足过滤条件，则重新获取数据
				if(f)
					continue;
				return map;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() throws IOException {
		try {
			if(rs != null )
				rs.close();
			if(ps != null)
				ps.close();
			if(conn != null)
				conn.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	public void filter(String where) {
		filters.add(where);
	}

	public int clearFilter() {
		int size = filters.size();
		filters.clear();
		return size;
	}

	@Override
	public void limit(int offset, int size) {
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size+1);
	}
}

class FilterUtils{
	public static boolean satisfy(String filter,Map<String,String> map){
		List<String> names = getSegments(filter);
		if(names.size() == 3){
			String name = names.get(0);
			boolean b = map.containsKey(name);
			if(!b)
				throw new IllegalArgumentException("invalid column name \""+name+"\"");
			String value = map.get(name);
			String tmp = names.get(1);
			String s = names.get(2);
			if(tmp.equals("=")){
				return s.equals(value);
			}else if("!=".equals(tmp) || "<>".equals(tmp)){
				return !s.equals(value);
			}else if("<".equals(tmp)){
				return Double.valueOf(value) > Double.valueOf(s);
			}else if(">".equals(tmp)){
				return Double.valueOf(value) < Double.valueOf(s);
			}else if("<=".equals(tmp)){
				return Double.valueOf(s) <= Double.valueOf(value);
			}else if(">=".equals(tmp)){
				return Double.valueOf(s) >= Double.valueOf(value);
			}
		}
		return false;
	}

	/**
	 * @param filter
	 * @return
	 */
	protected static List<String> getSegments(String filter) {
		List<String> names = new ArrayList<>();
		Pattern compile = Pattern.compile("(\\s|\\W)+");
		Matcher matcher = compile.matcher(filter);
		List<Integer> list = new ArrayList<>();
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			list.add(start);
			list.add(end);
		}
		list.add(filter.length());
		int start = 0;
		for (Integer end : list) {
			if(start != end){
				String name = filter.substring(start, end).trim();
				if(!name.isEmpty())
					names.add(name);
			}
			start = end;
		}
		return names;
	}
}
