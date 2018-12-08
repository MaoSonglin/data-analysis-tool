package dat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import dat.data.QueryHandler.JoinHandler;
import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.VirtualTableRepository;

@Deprecated
public class VirtualQueryHandler implements QueryHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4275321163319801521L;
	private VirtualTable table;
	/**
	 * 查询的结果
	 */
	private Map<String,List<String>> tableValue;
	/**
	 * 当前可查询的列
	 */
	private List<VirtualColumn> columns;
	/**
	 * 待查询的列
	 */
	private List<String> queryColumnNames;
	/**
	 * 
	 */
	private List<String> aliasNames;
	/**
	 * 待查询的数据实体表和实体表中待查的字段的映射
	 */
	private Map<DataTable,List<String>> queryTableMap;
	
	/**
	 * 待查的的列的别名 
	 */
	private Map<DataTable,List<String>> aliasNameMap;
	
	public VirtualQueryHandler(VirtualTable table) {
		setTable(table);
	}

	/**
	 * @param table
	 */
	private void setTable(VirtualTable table) {
		this.table = table;
		List<VirtualColumn> columns2 = table.getColumns();
		setColumns(columns2);
	}

	protected void setColumns(List<VirtualColumn> columns2) {
		this.columns = columns2;
	}

	@Override
	public Map<String, List<String>> list() throws Exception {
		// 要查询的实体表和实体表中要查询的实体字段
		getEntityTableAndEntityColumn(columns);
		// 查询的实体表和实体表中的数据的映射
		Map<DataTable, Map<String, List<String>>> resultMap = table2Data(queryTableMap);
		// 计算虚拟数据表的数据
		getVirtualTableValue(columns, resultMap);
		return tableValue;
	}

	@Override
	public void query(List<String> columnNames) throws Exception {
		query(columnNames,columnNames);
	}

	@Override
	public void query(List<String> columnNames, List<String> aliasNames)
			throws Exception {
		queryColumnNames = columnNames;
		this.aliasNames  = aliasNames;
		
	}

	/**
	 * @param columns
	 * @param resultMap
	 */
	protected void getVirtualTableValue(List<VirtualColumn> columns,
			Map<DataTable, Map<String, List<String>>> resultMap) {
		tableValue = new HashMap<>();
		// 计算虚拟字段的信息
		for (VirtualColumn virtualColumn : columns) {
			String virtualName = virtualColumn.getName();
			if(!queryColumnNames.contains(virtualName)){
				continue;
			}
			// 虚拟字段所引用的实体字段
			List<TableColumn> refColumns = virtualColumn.getRefColumns();
			// 存放实体字段的id到值的映射
			Map<String,List<String>> id2Datas = new HashMap<>();
			// 遍历所有引用的字段，获取字段的数据和字段的ID添加到id2Datas中
			for (TableColumn tableColumn : refColumns) {
				DataTable dataTable = tableColumn.getDataTable();
				// 获取数据表中的数据
				Map<String, List<String>> map2 = resultMap.get(dataTable);
				// 获取字段的数据
				List<String> values = map2.get(tableColumn.getId());
				id2Datas.put(tableColumn.getId(), values);
			}
			// 虚拟字段的计算公式
			String formula = virtualColumn.getFormula();
			// 根据字段的计算公式和数据，计算虚拟字段的结果
			List<String> virtualColumnValues = jisuan(formula,id2Datas);
			int indexOf = this.queryColumnNames.indexOf(virtualName);
			if(indexOf>-1)
				virtualName = this.aliasNames.get(indexOf);
			tableValue.put(virtualName, virtualColumnValues);
		}
	}

	protected List<String> jisuan(String formula, Map<String, List<String>> id2Datas) {
		List<String> list = id2Datas.get(formula);
		if(list!=null)
			return list;
		// TODO 解析计算公式，进行计算
		return null;
	}

	/**
	 * @param map 待查的数据表到在该数据表上需要查询的字段的映射
	 * @return
	 * @throws Exception
	 */
	protected Map<DataTable, Map<String, List<String>>> table2Data(
			Map<DataTable, List<String>> map) throws Exception {
		// 数据表到数据的映射
		Map<DataTable,Map<String,List<String>>> resultMap = new HashMap<>();
		// 遍历数据表到查询字段名称的映射
		Set<Entry<DataTable,List<String>>> entrySet = map.entrySet();
		for (Entry<DataTable, List<String>> entry : entrySet) {
			// 数据表
			DataTable key = entry.getKey();
			// 查询字段的名称
			List<String> value = entry.getValue();
			// 查询字段的别名
			List<String> aliasName = this.aliasNameMap.get(key);
			// 实体数据表查询对象
			EntityTableQueryHandler entityTableQueryHandler = new EntityTableQueryHandler(key);
			// 查询
			entityTableQueryHandler.query(value,aliasName);
			// 查询结果
			Map<String, List<String>> list = entityTableQueryHandler.list();
			// 存放到对应的数据表到数据结果映射中
			resultMap.put(key, list);
		}
		return resultMap;
	}

	/**
	 * 获取要查询的虚拟字段表包含的实体字段以及所在的实体数据表
	 * @param columns
	 * @return 
	 * @return
	 */
	protected void getEntityTableAndEntityColumn(
			List<VirtualColumn> columns) {
		queryTableMap = new HashMap<>();
		aliasNameMap = new HashMap<>();
		for (VirtualColumn virtualColumn : columns) {
			// 遍历所有虚拟字段，如果虚拟字段包含在待查数组中
			if(queryColumnNames.contains(virtualColumn.getName())){
				// 获取虚拟字段所引用的实体字段数组
				List<TableColumn> refColumns = virtualColumn.getRefColumns();
				// 遍历虚拟字段引用的实体字段数组，因为一个虚拟字段引用的实体字段可能来自不同的数据源、数据表
				for (TableColumn tableColumn : refColumns) {
					// 获取虚拟字段所在数据表
					DataTable dataTable = tableColumn.getDataTable();
					String columnName = tableColumn.getColumnName();
					// 保存映射关系
					setMap(queryTableMap,dataTable, columnName);
					setMap(aliasNameMap,dataTable,tableColumn.getId());
				}
			}
		}
	}

	/**
	 * @param queryTableMap 
	 * @param dataTable
	 * @param columnName
	 */
	private void setMap(Map<DataTable, List<String>> queryTableMap, DataTable dataTable, String columnName) {
		List<String> lists = queryTableMap.get(dataTable);
		if(lists == null){
			lists = new ArrayList<>();
			queryTableMap.put(dataTable, lists);
		}
		lists.add(columnName);
	}



	@Override
	public QueryHandler limit(int offset, int size) {
		return null;
	}



	@Override
	public JoinHandler join(String tableName) throws Exception {
		ServletRequestAttributes request = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		ServletContext sc = request.getRequest().getServletContext();
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sc);
		VirtualTableRepository virtualTableRepository = context.getBean(VirtualTableRepository.class);
		VirtualTable virtualTable = null;
		try {
			virtualTable = virtualTableRepository.findByName(tableName);
		} catch (Exception e) {
		}
		if(virtualTable == null){
			throw new IllegalArgumentException("数据表"+tableName+"不存在");
		}
		return new VirtualTableJoinHandler(this.table,virtualTable);
	}


}
@SuppressWarnings("unused") @Deprecated
class VirtualTableJoinHandler extends VirtualQueryHandler implements JoinHandler{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3113966016736424114L;
	private VirtualTable rightTable;
	private VirtualTable leftTable;
	private VirtualColumn pkColumn;
	private VirtualColumn fkColumn;
	private List<String> queryColumnNames;
	private List<VirtualColumn> columns;
	private List<String> aliasNames;
	{
		queryColumnNames = new ArrayList<>();
		columns = new ArrayList<>();
	}
	
	public VirtualTableJoinHandler(VirtualTable table, VirtualTable virtualTable) {
		super(table);
		this.leftTable = table;
		this.rightTable = virtualTable;
		addColumns(table.getColumns());
		addColumns(virtualTable.getColumns());
	}
	
	private void addColumns(List<VirtualColumn> columns2) {
		this.columns.addAll(columns2);
	}

	@Override
	public Map<String, List<String>> list() throws Exception {
		return super.list();
	}

	@Override
	public void query(List<String> columnNames, List<String> aliasNames)
			throws Exception {
		queryColumnNames.addAll(columnNames);
		this.aliasNames = aliasNames;
	}
	@Override
	public JoinHandler setPkColumn(String columnName) {
		pkColumn = check(this.leftTable,columnName);
		queryColumnNames.add(columnName);
		return this;
	}

	

	@Override
	public JoinHandler setFkColumn(String columnName) {
		fkColumn = check(this.rightTable,columnName);
		queryColumnNames.add(columnName);
		return this;
	}

	/**
	 * @param leftTable2 
	 * @param columnName
	 * @return 
	 */
	private VirtualColumn check(VirtualTable leftTable2, String columnName) {
		for (VirtualColumn column : leftTable2.getColumns()) {
			if(column.getName().equals(columnName)){
				return column;
			}
		}
		throw new IllegalArgumentException("数据表"+leftTable.getName()+"不包含数据列"+columnName);
	}
	
	public VirtualTable getRightTable() {
		return rightTable;
	}

	public void setRightTable(VirtualTable rightTable) {
		this.rightTable = rightTable;
	}

	public VirtualTable getLeftTable() {
		return leftTable;
	}

	public void setLeftTable(VirtualTable leftTable) {
		this.leftTable = leftTable;
	}
	
}

