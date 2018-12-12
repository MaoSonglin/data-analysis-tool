package dat.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import dat.App;
import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.DataSourceService;
import dat.service.DataTableService;
import dat.service.VirtualTableService;
import dat.util.FormulaParser;


class MultipleTableAdapter implements DataAdapter{
	
	private static Logger logger = LoggerFactory.getLogger(MultipleTableAdapter.class);
	// 待查询的虚拟表
	private VirtualTable virtualTable;
	// 待查询的虚拟字段数组
	private Collection<VirtualColumn> virtualColumns;
	// spring 容器
	private ApplicationContext context;
	// 待查询实体数据表数组
	private List<DataTable> queryDataTables;
	// 数据库连接数组
	private List<Connection> connList;
	// SQL操作的对象数组
	private ArrayList<PreparedStatement> psList;
	// 结果集对象数组
	private ArrayList<ResultSet> rsList;
	// 实体字段ID到包含该实体字段的结果的映射
	private HashMap<String, ResultSet> columnRsMap;
	
	public MultipleTableAdapter(VirtualTable table) {
		// 待查询的虚拟数据表
		this.virtualTable = table;
		// 虚拟数据表中的字段
		virtualColumns = virtualTable.getColumns();
		// spring context
		context = App.getContext();
		// 虚拟数据表服务层接口
		VirtualTableService tableService = context.getBean(VirtualTableService.class);
		// 虚拟数据表底层引用的实体数据表，查询虚拟数据表中的数据必须先查询出实体表中的数据
		queryDataTables = tableService.getQuoteTable(virtualTable);
		
		// 数据库连接数组
		connList = new ArrayList<>();
		psList = new ArrayList<>();
		rsList = new ArrayList<>();
		// 实体字段列的ID到该result set的映射
		columnRsMap = new HashMap<String,ResultSet>();
		// 遍历待查询的实体数据库
		queryDataTables.forEach(dataTable->{
			initQuery(dataTable);
		});
	}

	/**
	 * @param dataTable
	 */
	private void initQuery(DataTable dataTable) {
		// 服务层接口
		DataTableService dataTableService = context.getBean(DataTableService.class);
		DataSourceService dataSourceService = context.getBean(DataSourceService.class);
		// 获取实体数据表所属的数据源
		Source source = (Source) dataTableService.getSource(dataTable.getId()).getData();
		try {
			// 获取数据源连接
			Connection conn = dataSourceService.getTemplate(source).getDataSource().getConnection();
			// 实体数据表中包含的字段
			List<TableColumn> columns = dataTable.getColumns();
			String sql = querySql(dataTable, columns);
			logger.debug(sql);
			// SQL语句操作对象
			PreparedStatement ps = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			// 查询结果集对象
			ResultSet rs = ps.executeQuery();
			
			for(TableColumn tableColumn : columns){
				String id = tableColumn.getId();
				columnRsMap.put(id, rs);
			}
			
			rsList.add(rs);
			psList.add(ps);
			connList.add(conn);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/**
	 * @param dataTable
	 * @param columns
	 * @return
	 */
	private String querySql(DataTable dataTable, List<TableColumn> columns) {
		// SQL语句缓存区
		StringBuffer buffer = new StringBuffer("SELECT ");
		// 拼接查询的SQL语句
		for (TableColumn tableColumn : columns) {
			buffer.append(tableColumn.getColumnName())
			.append(' ')
			.append(tableColumn.getId())
			.append(',');
		}
		buffer.deleteCharAt(buffer.length()-1);
		buffer.append(" FROM ");
		buffer.append(dataTable.getName());
		String sql = buffer.toString();
		return sql;
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		try {
			for(ResultSet rs : rsList){
			// 遍历所有结果集，只要还有一个结果集中有数据，就返回true，否则返回false
				boolean last = rs.isLast() || rs.isAfterLast();//rs.isLast();
				if(!last)
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Map<String, String> next() {
		/**
		 * 
		 */
		boolean moveNext = moveToNext();
		if(!moveNext){
			boolean hasNext = hasNext();
			throw new RuntimeException("there is no move datas and hastNext() reture "+hasNext);
		}
		// 存放该行数据的map映射
		Map<String,String> resultMap = new HashMap<>(virtualColumns.size());
		// 遍历待查询的虚拟字段数组
		for(VirtualColumn vc : virtualColumns){
			// 获取当前虚拟字段引用的实体字段
			List<TableColumn> refColumns = vc.getRefColumns();
			
			Map<String,String> tmpMap = new HashMap<>(refColumns.size());
			// 遍历实体字段
			for (TableColumn tableColumn : refColumns) {
				// 用实体字段ID获取包含该字段的结果集
				String id = tableColumn.getId();
				ResultSet rs = columnRsMap.get(id);
				try {
					if(rs != null){
						String value = rs.getString(id);
						tmpMap.put(id, value);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			// 计算当前虚拟字段的结果
			String value = new FormulaParser(vc.getFormula(), tmpMap).getValue();
			resultMap.put(vc.getName(), value);
		}
		return resultMap;
	}

	/**
	 * @return
	 */
	private boolean moveToNext() {
		boolean moveNext = false;
		try {
			// 遍历所有结果集
			for(ResultSet rs : rsList){
//				logger.debug(rs.toString()+" isLast()="+rs.isLast()+" isAfterLast()="+rs.isAfterLast());
				// move to the next line
				boolean f = rs.next();
				if(!f && columnRsMap.containsValue(rs))// if fail operated, remove the result set in the columnRsMap
				{
					virtualColumns.forEach(virtualColumn->{
						virtualColumn.getRefColumns().forEach(column->{
							String id = column.getId();
							ResultSet resultSet = columnRsMap.get(id);
							if(resultSet == rs){
								columnRsMap.remove(id, resultSet);
							}
						});
					});
				}
				moveNext = moveNext || f;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(!moveNext){
			logger.debug("hasNext() = "+hasNext());
			rsList.forEach(rs->{
				try {
					logger.debug(rs.toString()+" isLast()="+rs.isLast()+" isAfterLast()="+rs.isAfterLast());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		return moveNext;
	}

	@Override
	public void close() {
		logger.debug("close the connections");
		rsList.forEach(rs->{
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		psList.forEach(ps->{
			try {
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		connList.forEach(conn->{
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void filter(String where) {
		logger.warn("异源多表的虚拟数据表不支持该操作");
	}

	@Override
	public int clearFilter() {
		logger.warn("异源多表的虚拟数据表不支持该操作");
		return 0;
	}

	@Override
	public void limit(int offset, int size) {
		logger.warn("异源多表的虚拟数据表不支持该操作");
	}
	
}



