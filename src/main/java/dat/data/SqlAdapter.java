package dat.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import dat.App;
import dat.domain.DataTable;
import dat.domain.ForeignKeyInfo;
import dat.domain.TableColumn;
import dat.repos.ForeignKeyInfoRepository;

/**
 * @author MaoSonglin
 * 将dataTable实体类获取VirtualTable实体类对象映射为SQL语句的接口
 */
public interface SqlAdapter {

	List<SqlInfo> getSqlInfo();
}


class DataTableSqlAdapter implements SqlAdapter{
	
	private List<DataTable> tables;

	private List<SqlInfo> sqlInfos;
	
	public DataTableSqlAdapter(List<DataTable> tables) {
		super();
		this.tables = tables;
		sqlInfos = new ArrayList<>();
		init();
	}
	
	private void init(){
		ApplicationContext context = App.getContext();
		ForeignKeyInfoRepository foreignKeyInfoRepos = context.getBean(ForeignKeyInfoRepository.class);
		// 1、 查询出所有数据表中的所有外键信息
		List<String> columnIds = new ArrayList<>();
		for (DataTable dataTable : tables) {
			List<TableColumn> columns = dataTable.getColumns();
			columns.forEach(elem->{
				columnIds.add(elem.getId());
			});
		}
		// 存放外键的集合
		Set<ForeignKeyInfo> foreignKeyInfos = foreignKeyInfoRepos.findByTableColumnIds(columnIds);
		
		if(!foreignKeyInfos.isEmpty()){
			// 外键处理
			join(foreignKeyInfos, new ArrayList<>(tables), sqlInfos);
		}else{
			tables.forEach(table->{
				buildSql(table);
			});
		}
	}

	/**
	 * @param foreignKeyInfos
	 * @param tables
	 * @param sqlInfoList
	 */
	protected void join(Set<ForeignKeyInfo> foreignKeyInfos,
			ArrayList<DataTable> tables, List<SqlInfo> sqlInfoList) {
		for (ForeignKeyInfo foreignKeyInfo : foreignKeyInfos) {
			// 外键所在的表和外键参考的表
			DataTable dataTable = foreignKeyInfo.getDataTable();
			DataTable referencedTable = foreignKeyInfo.getReferencedTable();
			
			// 如果外键引用的表和外键参考的表包含在查询队列中
			if(tables.contains(dataTable) && tables.contains(referencedTable)){
				ArrayList<TableColumn> columnsList = new ArrayList<>(dataTable.getColumns());
				columnsList.addAll(referencedTable.getColumns());
				StringBuffer buffer = new StringBuffer("SELECT ");
				appendColumn(buffer,columnsList);
				buffer.append(" FROM ").append(dataTable.getName())
				.append(" JOIN ").append(referencedTable.getName())
				.append(" ON ").append(foreignKeyInfo.getForeignKey().getId()).append(" = ").append(foreignKeyInfo.getReferencedColumn().getId());
				
				SqlInfo sqlInfo = new SqlInfo();
				sqlInfo.setSql(buffer.toString());
				sqlInfo.setSource(dataTable.getSource());
				
				sqlInfoList.add(sqlInfo);
				
				// 将连接的表从队列中移除
				tables.remove(dataTable);
				tables.remove(referencedTable);
			}
		}
		tables.forEach(table->{
			buildSql(table);
		});
	}

	/**
	 * @param table
	 */
	protected void buildSql(DataTable table) {
		List<TableColumn> columns = table.getColumns();
		StringBuffer buffer = new StringBuffer("SELECT ");
		appendColumn(buffer, columns);
		
		buffer.append(" FROM ").append(table.getName());
		
		SqlInfo sqlInfo = new SqlInfo();
		sqlInfo.setSql(buffer.toString());
		sqlInfo.setSource(table.getSource());
		sqlInfos.add(sqlInfo);
	}

	/**
	 * @param buffer
	 * @param columns
	 */
	protected void appendColumn(StringBuffer buffer, List<TableColumn> columns) {
		for (TableColumn tableColumn : columns) {
//					buffer.append(openQuote).append(tableColumn.getDataTable().getName()).append(closeQuote)
//					.append('.').append(openQuote);
			buffer.append(tableColumn.getColumnName())
			/*.append(closeQuote)*/.append(" AS ").append(tableColumn.getId()).append(" , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
	}

	@Override
	public List<SqlInfo> getSqlInfo() {
		return sqlInfos;
	}

}

