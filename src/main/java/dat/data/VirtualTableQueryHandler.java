package dat.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;

import dat.data.QueryHandler.JoinHandler;
import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;

public class VirtualTableQueryHandler implements JoinHandler,Serializable {
	
	private static final long serialVersionUID = 7420357338798441118L;
	
	private static Logger logger = Logger.getLogger(VirtualTableQueryHandler.class);
	
	private VirtualTable table;
	// 待查的字段队列
	private List<VirtualColumn> queryColumns;
	// 待查字段队列的别名
	private List<String> aliasNames;

	private QueryHandler joinHandler;
	
	private Map<DataTable,List<String>> tableMapColumn;
	private String pkColumnName;
	private String fkColumnName;
	private Map<String,List<String>> tableValues;

	private int size;

	private int offset;
	
	{
		queryColumns = new ArrayList<>();
		aliasNames = new ArrayList<>();
		tableMapColumn = new HashMap<>();
	}
	
	public VirtualTableQueryHandler(VirtualTable table) {
		this.table = table;
	}
	
	@Override
	public Map<String, List<String>> list() throws Exception {
		if(tableMapColumn == null){
			List<VirtualColumn> columns = table.getColumns();
			List<String> columnNames = new ArrayList<>(columns.size());
			columns.forEach(elem->{
				columnNames.add(elem.getName());
			});
			query(columnNames);
		}
		Set<Entry<DataTable,List<String>>> entrySet = tableMapColumn.entrySet();
		Map<DataTable,Map<String,List<String>>> resultTableList = new HashMap<>();
		for (Entry<DataTable, List<String>> entry : entrySet) {
			DataTable dataTable = entry.getKey();
			List<String> columns = entry.getValue();
			EntityTableQueryHandler queryHandler = new EntityTableQueryHandler(dataTable);
			queryHandler.query(columns, columns);
			Map<String, List<String>> list = queryHandler.list();
			resultTableList.put(dataTable, list);
		}
		calculate(resultTableList);
		doJoin();
		doLimit();
		return tableValues;
	}

	private void doLimit() {
		if(size>0){
			int beforeIndex = offset;
			int endIndex = offset + size;
			Set<Entry<String,List<String>>> entrySet = tableValues.entrySet();
			for (Entry<String, List<String>> entry : entrySet) {
				List<String> value = entry.getValue();
				int size2 = value.size();
				if(beforeIndex>=size2){
					continue;
				}
				if(endIndex >= size2){
					endIndex = size2-1;
				}
				value = value.subList(beforeIndex, endIndex);
				tableValues.put(entry.getKey(), value);
			}
		}
	}

	private void calculate(
			Map<DataTable, Map<String, List<String>>> resultTableList) {
		tableValues = new HashMap<>();
		logger.debug(queryColumns);
		logger.debug(aliasNames);
		// 待查询的字段迭代器
		Iterator<VirtualColumn> virtualColumnIterator = queryColumns.iterator();
		// 别名迭代器
		Iterator<String> aliasIterator = aliasNames.iterator();
		// 循环迭代
		while(virtualColumnIterator.hasNext() && aliasIterator.hasNext()){
			VirtualColumn column = virtualColumnIterator.next();
			String alias = aliasIterator.next();
			Map<String,List<String>> colValues = new HashMap<>();
			List<TableColumn> refColumns = column.getRefColumns();
			// 遍历虚拟字段依赖的实体字段
			for (TableColumn tableColumn : refColumns) {
				// 实体数据表对象
				DataTable dataTable = tableColumn.getDataTable();
				// 实体数据表中的数据
				Map<String, List<String>> map = resultTableList.get(dataTable);
				// 当前实体列的所有值
				List<String> list = map.get(tableColumn.getColumnName());
				colValues.put(tableColumn.getId(), list);
			}
			String formula = column.getFormula();
			// 计算虚拟字段的值
			List<String> values = eval(formula,colValues);
			tableValues.put(alias, values);
		}
		 
	}

	/**
	 * @throws Exception
	 */
	private void doJoin() throws Exception {
		if(joinHandler==null)
			return;
		// 连接的表的数据
		Map<String, List<String>> list = joinHandler.list();
		// 外键的值
		List<String> joinColumnValues = list.get(this.fkColumnName);
		// 主键的值
		List<String> pkValues = tableValues.get(this.pkColumnName);
		
		// 迭代器
		Iterator<String> iterator = joinColumnValues.iterator();
		Iterator<String> iterator2 = pkValues.iterator();
		
		// 存放外键和主键相等的数据的下标的数组
		List<Integer> indexs = new ArrayList<>();
		int index = 0;
		while(iterator.hasNext() && iterator2.hasNext()){
			String next = iterator.next();
			String next2 = iterator2.next();
			index ++;
			if(next != null && next2 != null && next.trim().equals(next2.trim())){
				indexs.add(index);
			}
		}
		
		Map<String,List<String>> tmpMap = new HashMap<>();
		cancat(this.tableValues,indexs,tmpMap);
		cancat(list, indexs, tmpMap);
		this.tableValues = tmpMap;
	}

	/**
	 * @param list
	 * @param indexs
	 * @param tmpMap
	 */
	private void cancat(Map<String, List<String>> list, List<Integer> indexs,
			Map<String, List<String>> tmpMap) {
		list.forEach((key,value)->{
			Iterator<Integer> indexIter = indexs.iterator();
			List<String> tmpList = new ArrayList<>(indexs.size());
			while(indexIter.hasNext()){
				Integer j = indexIter.next();
				tmpList.add(value.get(j));
			}
			tmpMap.put(key, tmpList);
		});
	}

	private List<String> eval(String formula,
			Map<String, List<String>> colValues) {
		return colValues.get(formula);
	}

	
	@Override
	public void query(List<String> columnNames) throws Exception {
		query(columnNames,columnNames);
	}

	@Override
	public void query(List<String> columnNames, List<String> aliasNames)
			throws Exception {
		// 获取虚拟数据表中包含的虚拟字段
		List<VirtualColumn> columns = table.getColumns();
		readTableColumnMap(columnNames, columns);
		this.aliasNames.addAll(aliasNames);
	}
	
	public void clearQuery(){
		this.aliasNames.clear();
		this.queryColumns.clear();
		this.tableMapColumn.clear();
	}

	/**
	 * @param columnNames
	 * @param columns
	 */
	private void readTableColumnMap(List<String> columnNames,
			List<VirtualColumn> columns) {
		// 遍历虚拟数据表中的虚拟字段
		for (VirtualColumn virtualColumn : columns) {
			String name = virtualColumn.getName();
			// 如果当前的虚拟字段包含在需要查询的字段列表中
			if(columnNames.contains(name)){
				// 将当前虚拟字段保存到待查询字段队列中
				queryColumns.add(virtualColumn);
				// 获取当前字段依赖的实体字段数组
				List<TableColumn> refColumns = virtualColumn.getRefColumns();
				// 遍历依赖的实体字段
				for (TableColumn tableColumn : refColumns) {
					// 获取实体字段所属的数据表
					DataTable dataTable = tableColumn.getDataTable();
					// 保存数据表与实体字段的映射
					List<String> set = tableMapColumn.get(dataTable);
					if(set==null){
						set = new ArrayList<>();
						tableMapColumn.put(dataTable, set);
					}
					set.add(tableColumn.getColumnName());
				}
			}
		}
	}

	@Override
	public JoinHandler join(String table) throws Exception {
		throw new IllegalArgumentException("该方法没有实现，请调用dat.data.QueryHandler.join(QueryHandler)");
	}
	
	@Override
	public QueryHandler join(QueryHandler handler) {
		Object clone = clone();
		if(clone != null){
			VirtualTableQueryHandler queryHandler= (VirtualTableQueryHandler) clone;
			queryHandler.joinHandler = handler;
			return queryHandler;
		}
		joinHandler = handler;
		return this;
	}

	@Override
	public QueryHandler limit(int offset, int size) {
		VirtualTableQueryHandler handler = (VirtualTableQueryHandler) clone();
		if(handler != null){
			handler.offset = offset;
			handler.size = size;
			return handler;
		}
		this.offset = offset;
		this.size = size;
		return this;
	}

	/**
	 * 
	 */
	protected Object clone() {
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream obs = new ObjectOutputStream(baos)){
			obs.writeObject(this);
			obs.flush();
			byte[] byteArray = baos.toByteArray();
			try(ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
					ObjectInputStream ois = new ObjectInputStream(bais);){
				VirtualTableQueryHandler handler = (VirtualTableQueryHandler) ois.readObject();
				return handler;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JoinHandler setPkColumn(String columnName) {
		this.pkColumnName = columnName;
		return this;
	}

	@Override
	public JoinHandler setFkColumn(String columnName) {
		this.fkColumnName = columnName;
		return this;
	}
	
	

}
