package dat.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import dat.domain.Relevance;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.RelevanceService;

public class TempTableAdapter extends LocalDataAdapter {

	private static final long serialVersionUID = -1805947764468714305L;
	
	private static final Logger logger = LogManager.getLogger(TempTableAdapter.class);

	private ApplicationContext context ;
	
	public TempTableAdapter(ApplicationContext context) {
		super(context);
		this.context = context;
	}

	@Override
	protected String createTemporaryTable(Connection conn,
			Set<VirtualTable> tableSet, List<VirtualColumn> columns)
			throws Exception {
		
		RelevanceService relevanceService = context.getBean(RelevanceService.class);
		Set<Relevance> relevances = relevanceService.getByTables(tableSet);
		List<List<VirtualTable>> list = new ArrayList<>();
		tableSet.forEach(elem->{
			List<VirtualTable> v = new ArrayList<>();
			v.add(elem);
			list.add(v);
		});
		// 将有关联的数据表放在一个集合中
		关联集合(list, relevances);
		list.removeIf(elem->{// 移除空集合
			return elem.isEmpty();
		});
		
		int hashCode = tableSet.hashCode();
		String tableName = "temp_"+Math.abs(hashCode);
		
		// 新建表
		createTable(conn, tableName, createTableSql(columns, tableName));
		
		conn.setAutoCommit(false);
		// 插入数据
		int rowNum = 0; // 插入的数据条数
		for (List<VirtualTable> group : list) {
			List<VirtualColumn> columns2 = getColumns(group, columns);
			StringBuffer buffer = new StringBuffer("SELECT ");
			// 添加查询的字段
			addSelectList(buffer, columns2);
			// 添加查询的数据表
			addFrom(group,buffer);
			// 添加连接条件
			if(group.size()>1)
				addJoin(relevances, group, buffer);
			String sql = buffer.toString();
			logger.debug(sql);
			try(PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();){
//				conn.prepareStatement("")
				String insertSql = insert(tableName, columns2);
				String update = update(tableName, columns2);
				try(PreparedStatement insertps = conn.prepareStatement(insertSql);
						PreparedStatement updateps = conn.prepareStatement(update);){
					int m = 1000, n = 1000;
					while(rs.next()){
						int row = rs.getRow();
						if(row > rowNum){
							// insert
							rowNum = row;
							setparam(columns2, rs, insertps);
							insertps.addBatch();
							if(--m < 0){
								logger.debug(insertSql);
								insertps.executeBatch();
								insertps.clearBatch();
								m = 1000;
							}
						}else{
							// update
							int count = setparam(columns2, rs, updateps);
							updateps.setObject(count++, row);
							updateps.addBatch();
							if(--n < 0){
								logger.debug(update);
								updateps.executeBatch();
								updateps.clearBatch();
								n = 1000;
							}
						}
					}
					insertps.executeBatch();
					updateps.executeBatch();
				}
			}
		}
		logger.debug("插入"+rowNum+"条数据");
		conn.commit();
		conn.setAutoCommit(true);
		return tableName;
	}

	/**
	 * @param columns2
	 * @param rs
	 * @param updateps
	 * @return
	 * @throws SQLException
	 */
	private int setparam(List<VirtualColumn> columns2, ResultSet rs,
			PreparedStatement updateps) throws SQLException {
		int count = 1;
		for (VirtualColumn virtualColumn : columns2) {
			updateps.setObject(count++, rs.getObject(virtualColumn.getName()));
		}
		return count;
	}

	/**
	 * @param tableName
	 * @param columns2
	 * @return
	 */
	private String update(String tableName, List<VirtualColumn> columns2) {
		StringBuffer updateSql = new StringBuffer("UPDATE ");
		updateSql.append(tableName);
		updateSql.append(" SET ");
		columns2.forEach(e->{
			updateSql.append(e.getName()).append(" = ? , ");
		});
		updateSql.delete(updateSql.length()-3, updateSql.length());
		updateSql.append(" WHERE ROW_NUM = ? ");
		String update = updateSql.toString();
		return update;
	}

	/**
	 * @param tableName
	 * @param columns2
	 * @return
	 */
	private String insert(String tableName, List<VirtualColumn> columns2) {
		StringBuffer insert = new StringBuffer("INSERT INTO ");
		StringBuffer values = new StringBuffer("VALUES (");
		insert.append(tableName).append(" ( ");
		columns2.forEach(column->{
			insert.append(column.getName()).append(" , ");
			values.append(" ? , ");
		});
		insert.delete(insert.length()-3, insert.length());
		values.delete(values.length()-3, values.length());
		values.append(" ) ");
		insert.append(" ) ");
		insert.append(values);
		String insertSql = insert.toString();
		return insertSql;
	}

	/**
	 * @param buffer
	 * @param columns2
	 */
	private void addSelectList(StringBuffer buffer, List<VirtualColumn> columns2) {
		columns2.forEach(column->{
			buffer.append(column.getTable().getId()).append('.').append(column.getName()).append(" AS ").append(column.getName()).append(" , ");
		});
		buffer.delete(buffer.length()-3, buffer.length());
	}
	

	/**
	 * @param group
	 * @param buffer
	 */
	private void addFrom(List<VirtualTable> group, StringBuffer buffer) {
		buffer.append(" FROM ");
		for (VirtualTable virtualTable : group) {
			buffer.append(virtualTable.getId()).append(" , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
	}

	/**
	 * @param relevances
	 * @param group
	 * @param buffer
	 */
	private void addJoin(Set<Relevance> relevances, List<VirtualTable> group,
			StringBuffer buffer) {
		buffer.append(" WHERE ");
		Set<Relevance> rel = find(group,relevances);
		for (Relevance relevance : rel) {
			VirtualTable table1 = relevance.getTable1();
			VirtualColumn column1 = relevance.getColumn1();
			VirtualTable table2 = relevance.getTable2();
			VirtualColumn column2 = relevance.getColumn2();
			buffer.append(table1.getId()).append('.').append(column1.getName());
			buffer.append(" = ");
			buffer.append(table2.getId()).append('.').append(column2.getName());
			buffer.append(" AND ");
		}
		buffer.delete(buffer.length()-5, buffer.length());
	}
	
	private List<VirtualColumn> getColumns(List<VirtualTable> group,
			List<VirtualColumn> columns) {
		List<VirtualColumn> list = new ArrayList<>();
		group.forEach(table->{
			List<VirtualColumn> columns2 = table.getColumns();
			columns2.forEach(column->{
				if(columns.contains(column)){
					list.add(column);
				}
			});
		});
		return list;
	}

	private Set<Relevance> find(List<VirtualTable> group,
			Set<Relevance> relevances) {
		Set<Relevance> set = new HashSet<>();
		for (Relevance relevance : relevances) {
			VirtualTable table1 = relevance.getTable1();
			VirtualTable table2 = relevance.getTable2();
			if(group.contains(table1) && group.contains(table2)){
				set.add(relevance);
			}
		}
		return set;
	}
	
	
	private void 关联集合(List<List<VirtualTable>> talbes,Set<Relevance> relevances){
		for(int i = 0, size = talbes.size(); i < size-1; i++){
			for(int j = i+1; j < size; j++){
				List<VirtualTable> list = talbes.get(i);
				List<VirtualTable> list2 = talbes.get(j);
				boolean f = 是否关联(list, list2, relevances);
				if(f){
					list.addAll(list2);
					list2.clear();
				}
			}
		}
	}

	/**
	 * 判断集合list和list2中是否存在有关联关系的数据表
	 * @param list
	 * @param list2
	 * @param relevances
	 * @return
	 */
	private boolean 是否关联(List<VirtualTable> list, List<VirtualTable> list2,
			Set<Relevance> relevances) {
		for (VirtualTable table1 : list) {
			for (VirtualTable table2 : list2) {
				for(Relevance relevance : relevances){
					VirtualTable t1 = relevance.getTable1();
					VirtualTable t2 = relevance.getTable2();
					if((table1.equals(t1)&&table2.equals(t2))||(table2.equals(t1)&&table1.equals(t2))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
}


