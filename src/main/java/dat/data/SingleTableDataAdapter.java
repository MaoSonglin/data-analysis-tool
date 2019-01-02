package dat.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import dat.util.DialectUtil;
import dat.util.FormulaParser;

public class SingleTableDataAdapter implements DataAdapter,Iterator<Map<String,String>>{
	private static Logger logger = LoggerFactory.getLogger(SingleTableDataAdapter.class);
	private Connection conn;
	private PreparedStatement ps;
	private List<TableColumn> columns;
	private List<VirtualColumn> virtualColumns;
	private List<String> filters;
	private LimitHandler limitHandler;
	private RowSelection selection;
	private DataTable dataTable;
	private List<String> columnNames;
	private ResultSet rs;
	private Map<String, String> next;
	
	public SingleTableDataAdapter(DataTable dataTable,List<VirtualColumn> virtualColumns,List<TableColumn> columns) {
		super();
		// 数据源
		filters = new ArrayList<>();
		Source source = dataTable.getSource();
		limitHandler = DialectUtil.getDialect(source.getDatabaseName()).getLimitHandler();
		this.virtualColumns = virtualColumns;
		this.columns = columns;
		this.dataTable = dataTable;
		try {
			String driverClass = source.getDriverClass();
			logger.debug("加载数据库驱动程序："+driverClass);
			Class.forName(driverClass);
			logger.debug("获取数据库连接:"+source.getUrl()+",username:"+source.getUsername());
			// 数据源连接
			conn = DriverManager.getConnection(source.getUrl(), source.getUsername(), source.getPassword());
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
		
	}
	private void toQuery() throws SQLException{
		if(ps == null){
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
		if(selection != null)
			limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
	}

	/**
	 * @param dataTable
	 * @param columns
	 * @return
	 */
	private String constructSql(DataTable dataTable, List<TableColumn> columns) {
		columnNames = new ArrayList<>();
		StringBuffer sb = new StringBuffer("select ");
		for (TableColumn tableColumn : columns) {
			String columnName = tableColumn.getColumnName();
			sb.append(columnName).append(',');
			columnNames.add(columnName);
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
		try {
			toQuery();
			rs = ps.executeQuery();
			return this;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	

	public void close() throws IOException {
		if (rs != null)
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (ps != null)
			try {
				ps.close();
			} catch (SQLException e) {
				throw new IOException(e);
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
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

	public void limit(int offset, int size) {
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size);
	}
	@Override
	public boolean hasNext() {
		try {
			if(next == null)
				next = readrow();
			return next != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public Map<String, String> next() {
		try {
			if(next == null)
				return readrow();
			else{
				Map<String,String> x = next;
				next = null;
				return x;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @return 
	 * @throws SQLException
	 */
	private Map<String, String> readrow() throws SQLException {
		while(rs.next()){
			Map<String,String> map = new HashMap<>();
			boolean f = false;
			for (VirtualColumn vc : virtualColumns) {
				String formula = vc.getFormula();
				// 虚拟字段应用的实体字段和实体字段的值的映射
				Map<String, String> tmpMap = getValue(rs,formula,columns);
				// 公式解析器
				FormulaParser parser = new FormulaParser(formula,tmpMap);
				// 获取虚拟字段的值
				String value = parser.getValue();
				// 保存虚拟字段和值的映射关系
				map.put(vc.getName(), value);
			}
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
	}
	/**
	 * @param rs 
	 * @param formula
	 * @return
	 * @throws SQLException
	 */
	static Map<String, String> getValue(ResultSet rs, String formula,List<TableColumn> columns) throws SQLException {
		Map<String,String> tmpMap = new HashMap<>();
		Pattern compile = Pattern.compile("(FD\\d+)|(DF\\d+)");
		Matcher matcher = compile.matcher(formula);
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String id = formula.substring(start, end);
			for (TableColumn column : columns) {
				if(column.getId().equals(id)){
					String value = rs.getString(column.getColumnName());
					tmpMap.put(id, value);
					break;
				}
			}
		}
		return tmpMap;
	}
}
