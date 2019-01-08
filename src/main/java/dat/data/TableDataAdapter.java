package dat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import dat.App;
import dat.domain.DataTable;
import dat.domain.ForeignKey;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.service.DataSourceService;
import dat.service.DataTableService;
import dat.util.FormulaParser;

public class TableDataAdapter implements DataAdapter,ApplicationContextAware{
	
	private static Logger logger = LoggerFactory.getLogger(TableDataAdapter.class);
	private ApplicationContext context = App.getContext();
	
	private List<VirtualColumn> columns;
	
	private List<SqlRowSet> rowSets = new ArrayList<>();
	
	public TableDataAdapter(List<VirtualColumn> columns) {
		super();
		this.columns = columns;
		init();
	}

	protected void init(){
		// 待查询的虚拟字段引用的实体数据表
		List<DataTable> dataTables = context.getBean(DataTableService.class).getByVirtualColumns(columns);
		logger.debug("待查询的数据表个数:%d",dataTables.size());
		// 遍历数据表
		for(int i = 0; i < dataTables.size(); i++){
			// 当前操作的数据表
			DataTable dataTable = dataTables.get(i);
			// 构建SQL语句
			String sql = buildSql(dataTable,dataTables);
			// 数据库操作模板
			JdbcTemplate template = context.getBean(DataSourceService.class).getTemplate(dataTable.getSource());
			logger.debug(sql);
			// 查询结果集
			SqlRowSet rowSet = template.queryForRowSet(sql);
			rowSets.add(rowSet);
		}
	}
	
	protected String buildSql(DataTable dataTable,List<DataTable> tables){
		// 数据库方言
//		Dialect dialect = DialectUtil.getDialect(dataTable.getSource().getDatabaseName());
		// 待查的数据表中包含的实体字段
		List<TableColumn> list = dataTable.getColumns();
		// 
//		char openQuote = dialect.openQuote();
//		char closeQuote = dialect.closeQuote();
		
		// 遍历待查的字段拼接查询的SQL语句
		StringBuffer buffer = new StringBuffer("SELECT ");
		for (TableColumn tableColumn : list) {
//			buffer.append(openQuote).append(tableColumn.getDataTable().getName()).append(closeQuote)
//			.append('.').append(openQuote);
			buffer.append(tableColumn.getColumnName())
			/*.append(closeQuote)*/.append(" AS ").append(tableColumn.getId()).append(" , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
		buffer.append(" FROM ").append(dataTable.getName());
		
		return buffer.toString();
	}

	/**
	 * @param joinFragment
	 * @param tables
	 * @param foreignKeys
	 * @param list
	 */
	@SuppressWarnings("unused")
	private void join(JoinFragment joinFragment, List<DataTable> tables,
			List<ForeignKey> foreignKeys, List<TableColumn> list) {
		for (ForeignKey foreignKey : foreignKeys) {
			DataTable table = foreignKey.getPrimaryTable();
			DataTable foreignTable = foreignKey.getForeignTable();
			if(tables.contains(table) && tables.contains(foreignTable)){
				boolean remove = tables.remove(table);
				if(remove){
					List<TableColumn> columns2 = table.getColumns();
					list.addAll(columns2);
					TableColumn foreignColumn = foreignKey.getForeignColumn();
					TableColumn primaryColumn = foreignKey.getPrimaryColumn();
					joinFragment.addJoin(table.getName(), table.getName(), new String[]{foreignColumn.getId()}, new String[]{primaryColumn.getId()}, JoinType.INNER_JOIN);
				}
			}
		}
	}
	
	@Override
	public Iterator<Map<String, String>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		// 如果存在某个结果集还有数据，则返回true，否则返回false
		for(SqlRowSet rowSet : rowSets){
			if(!rowSet.isAfterLast() && !rowSet.isLast()) return true;
		}
		return false;
	}

	@Override
	public Map<String, String> next() {
		// 存放底层数据表数据的map
		Map<String,String> map = new HashMap<>();
		// 遍历所有结果集
		for(SqlRowSet rowSet : rowSets){
			// 结果集游标下移一行
			if(rowSet.next()){
				SqlRowSetMetaData metaData = rowSet.getMetaData();
				// 结果集中字段名称数组
				String[] columnNames = metaData.getColumnNames();
				for (String columnName : columnNames) {
					String value = rowSet.getString(columnName);
					map.put(columnName, value);
				}
			}
		}
		
		Map<String,String> resultMap = new HashMap<>(map.size());
		// 将实体字段值映射到虚拟字段集
		FormulaParser parser = new FormulaParser();
		parser.setValues(map);
		for (VirtualColumn virtualColumn : columns) {
			parser.setFormula(virtualColumn.getFormula());
			String value = parser.getValue();
			resultMap.put(virtualColumn.getName(), value);
		}
		return resultMap;
	}

	@Override
	public void close() {
		
	}

	@Override
	public void filter(String where) {
		throw new UnsupportedOperationException("多表适配器不支持该操作");
	}

	@Override
	public int clearFilter() {
		return 0;
	}

	@Override
	public void limit(int offset, int size) {
		throw new UnsupportedOperationException("多表适配器不支持该操作");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}
	
}