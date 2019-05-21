package dat.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.tsc9526.monalisa.core.query.datatable.DataMap;

import dat.data.DataAdapter;
import dat.data.MonaliseDataSource;
import dat.data.TableDataAdapter;
import dat.domain.DataTable;
import dat.domain.ForeignKeyInfo;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.model.Graph;
import dat.model.TreeNode;
import dat.model.TreeNode.Gardener;
import dat.model.TreeNode.Tree;
import dat.repos.DataTableRepository;
import dat.repos.ForeignKeyInfoRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.service.DataTableService;
import dat.service.TableColumnService;
import dat.service.VirtualTableService;
import dat.service.WorkPackageService;
import dat.util.Connectivity;
import dat.util.Constant;
import dat.util.Kruskal;
import dat.vo.Response;
import lombok.Data;

@Service
@Data
public class VirtualTableServiceImpl implements VirtualTableService {

	private static Logger logger = Logger.getLogger(VirtualTableServiceImpl.class);
	
	@Autowired
	private VirtualTableRepository vtRepos;
	
	@Autowired
	private VirtualColumnRepository virtualColumnRepos;
	
	@Autowired
	private DataTableRepository dataTableRepos;
	
	@Autowired
	private DataTableService dataTableService;
	
	@Autowired
	private TableColumnRepository tableColumnRepos;

	@Autowired
	private TableColumnService tableColumnService;
	
	@Autowired
	private ForeignKeyInfoRepository foreignKeyInfoRepository;
	
	@Autowired
	private ApplicationContext context;

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public Response getById(String id) {
		try {
			VirtualTable virtualTable = findById(id);
			return new Response(Constant.SUCCESS_CODE,"查询成功",virtualTable);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,String.format("ID为“%s”的虚拟数据表不存在！", id));
		}
	}

	/**
	 * @param id
	 * @return
	 */
	private VirtualTable findById(String id) {
		VirtualTable virtualTable = vtRepos.findById(id).orElse(null);
		if(logger.isDebugEnabled()){
			logger.debug("获取到ID为“"+id+"”的虚拟数据表"+virtualTable);
		}
		return virtualTable;
	}

	public Response getVirtualColumns(String id) {
		
		List<VirtualColumn> virtualColumns = virtualColumnRepos.findAll((root,query,cb)->{
			Path<String> path = root.get("table").get("id");
			Predicate equal = cb.equal(path, id);
			Predicate equal2 = cb.equal(root.get("state"), Constant.ACTIVATE_SATE);
			return cb.and(equal,equal2);
		});
		if(logger.isDebugEnabled() && !virtualColumns.isEmpty()){
			logger.debugf("查询到ID为%s的数据表中包含的数据字段一共%d条",id,virtualColumns.size());
		}
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",virtualColumns);
		response.put("virtualTableId", id);
		return response;
	}

	@Override
	public Response add(VirtualTable table) {
		List<VirtualColumn> columns = table.getColumns();
		for (VirtualColumn virtualColumn : columns) {
			StringBuffer sb = new StringBuffer();
			for (TableColumn column : virtualColumn.getRefColumns()) {
				sb.append(column.getId()).append("+");
			}
			if(sb.length()>0){
				sb.deleteCharAt(sb.length()-1);
			}
			virtualColumn.setState(Constant.ACTIVATE_SATE);
			virtualColumn.setFormula(sb.toString());
			logger.debug(virtualColumn);
		}
		Object json = JSON.toJSON(table);
		try (PrintStream out = new PrintStream(new FileOutputStream("D:\\Program Files\\eclipse\\txt.json"))){
			out.print(json.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns,Integer offset,Integer limit) {
		if(table.getClass().equals(VirtualTable.class))
			table = vtRepos.findById(table.getId()).get();
		if(logger.isDebugEnabled()){
			StringBuffer sb = new StringBuffer();
			columns.forEach(elem->{
				String name = elem.getName();
				sb.append(name).append(",");
			});
			sb.deleteCharAt(sb.length()-1);
			logger.debugf("查询数据表'%s'中字'%s'的数据，数据偏移量为%d，返回数据最大条数%d",table.getName(),sb.toString(),offset,limit);
		}
		
		// 存放查询结果的map
		Map<String, List<String>> m = getEmptyResultMap(columns);
		// 查询助手
//		QueryHelper queryHelper = context.getBean(QueryHelper.class);
		// 查询开始
		try (DataAdapter dataAdapter = new TableDataAdapter(columns);) {
			// 设置分页
//			dataAdapter.limit(offset, limit);
			
			// 遍历查询结果
			dataAdapter.forEach(map->{
				// 当前行的数据
				Set<Entry<String,String>> entrySet = map.entrySet();
				for (Entry<String,String> entry : entrySet) {
					String key = entry.getKey();
					String value = entry.getValue();
					m.get(key).add(value);
				}
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return m;
	}
	
	@Override
	public Map<String, List<Object>> getData(VirtualTable table,List<VirtualColumn> columns,int offset,int limit) {
		
		// 拼接SQL语句
		StringBuffer sql = new StringBuffer("select ");
		for (VirtualColumn virtualColumn : columns) {
			sql.append(virtualColumn.getName()).append(',');
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" from ");
		sql.append(table.getId());
		sql.append(" limit ");
		sql.append(offset).append(',').append(limit);
		
		// 数据包ID
		String id = table.getPackages().iterator().next().getId();
		Map<String, List<Object>> query = query(id,sql.toString(),getMapListHandler());
		return query;
	}
	
	@Override
	public List<Map<String,String>> getData(VirtualTable t){
		// get the workPackage object that the table related
		WorkPackage next = t.getPackages().iterator().next();
		// execute query SQL statement and get the result.
		List<Map<String, String>> result = query(next.getId(),String.format("SELECT * FROM %s", t.getId()),rs->{
			// the meta data with the result
			ResultSetMetaData metaData = rs.getMetaData();
			// how much column in the result set.
			int columnCount = metaData.getColumnCount();
			// this array will fill with the data in result
			List<Map<String,String>> list = new ArrayList<>();
			while(rs.next()){// move the cursor to the next line unless this is last line
				// this map save the data in current line with key-value
				// key is the name of column 
				Map<String,String> map = new HashMap<>(columnCount);
				for (int i= 1 ; i <= columnCount; i++) {
					// column label 
					String columnLabel = metaData.getColumnLabel(i);
					// column value
					String value = rs.getString(columnLabel);
					map.put(columnLabel, value);
				}
				list.add(map);
			}
			return list;
		});
		return result;
	}

	/**
	 * @return
	 */
	private <T> T query(String id,String sql,ResultSetHandler<T> h,Object...params) {
		// 数据包服务层结果，用来获取数据包的数据库连接
		WorkPackageService packageService = context.getBean(WorkPackageService.class);
		try(Connection conn = packageService.getConnection(id);
				PreparedStatement ps = conn.prepareStatement(sql);) {
			// 设置SQL语句中的参数
			for(int i = 0,length = params.length; i < length; i++)
				ps.setObject(i+1, params[i]);
			// 获取结果集并处理结果
			try(ResultSet rs = ps.executeQuery()){
				return h.doResultSet(rs);
			}
		} catch (SQLException e1) {
			throw new RuntimeException(e1);
		}
	}

	@Override
	public Map<String, List<Object>> listData(VirtualTable table) {
		if(!table.getClass().equals(VirtualTable.class))
			table = findById(table.getId());
		// 虚拟数据表所属数据包的ID
		String pkgid = table.getPackages().iterator().next().getId();
		// SQL语句
		String sql = String.format("select * from %s", table.getId());
		logger.debug(sql);
		// 查询结果
		ResultSetHandler<Map<String, List<Object>>> mapListResultHandler = getMapListHandler();
		Map<String, List<Object>> query = query(pkgid,sql,mapListResultHandler);
		return query;
		
		/*QueryHelper helper = context.getBean(QueryHelper.class);
		List<VirtualColumn> columns = table.getColumns();
		// 获取一个空存放查询结果映射集合
		Map<String, List<String>> resultMap = getEmptyResultMap(columns);
		try (DataAdapter dataAdapter = helper.query(table, columns)) {
			// 遍历查询结果
			for (Map<String, String> map : dataAdapter) {
				Set<Entry<String,String>> entrySet = map.entrySet();
				for (Entry<String, String> entry : entrySet) {
					resultMap.get(entry.getKey()).add(entry.getValue());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resultMap;*/
	}

	/**
	 * get a instance of result set handler, that handler set the data
	 * into a map with a row in the result set. and add each map to a 
	 * array . 
	 * @return
	 */
	public static ResultSetHandler<Map<String, List<Object>>> getMapListHandler() {
		ResultSetHandler<Map<String, List<Object>>> mapListResultHandler = rs->{
			// 结果集元数据信息
			ResultSetMetaData metaData = rs.getMetaData();
			// 结果集中包含的列的个数
			int columnCount = metaData.getColumnCount();
			// 存放结果的map对象
			Map<String,List<Object>> map = new HashMap<>(columnCount);
			for(int i = 0; i < columnCount; i++){
				String columnLabel = metaData.getColumnLabel(i+1);
//				System.err.println(columnLabel);
				map.put(columnLabel, new LinkedList<>());
			}
			// 遍历结果集
			while(rs.next()){
				Set<Entry<String,List<Object>>> entrySet = map.entrySet();
				for (Entry<String, List<Object>> entry : entrySet) {
					String key = entry.getKey();
					Object value = rs.getObject(key);
					entry.getValue().add(value);
				}
			}
			return map;
		};
		return mapListResultHandler;
	}

	/**
	 * @param columns
	 * @return
	 */
	private Map<String, List<String>> getEmptyResultMap(
			List<VirtualColumn> columns) {
		Map<String, List<String>> resultMap = new HashMap<>();
		for(VirtualColumn column : columns){
			resultMap.put(column.getName(), new LinkedList<>());
		}
		return resultMap;
	}
	
	@Override
	public List<DataTable> getQuoteTable(VirtualTable table) {
		// 如果table的类型和VirtualColumn类型一致，则说明table不是持久太对象
		// 则根据ID将table转化为持久态对象
		if(table.getClass().equals(VirtualTable.class))
			table = findById(table.getId());
		if(logger.isDebugEnabled()){
			logger.debug("获取虚拟数据表“"+table.getName()+"”引用的实体数据表");
		}
		// 待查询的虚拟字段
		List<VirtualColumn> columns = table.getColumns();
		// 获取待查询的实体字段数据
		List<TableColumn> tableColumns = tableColumnService.getColumnsByVirtualColumns(columns);
		// 存放字段ID的集合
		Set<String> ids = new HashSet<>();
		tableColumns.forEach(tableColumn->{ ids.add(tableColumn.getId());});
		
		// 查询出tableColumns所属的数据表
		List<DataTable> dataTables = dataTableRepos.findAll((root,query,cb)->{
			Predicate in = root.join("columns").get("id").in(ids);
			return in;
		});
		// 用集合去掉重复的数据表
		HashSet<DataTable> hashSet = new HashSet<>(dataTables);
		return new ArrayList<>(hashSet);
	}

	@Override
	public DataTable getMainTable(VirtualTable table) {
		String tableId;
		List<String> tableIds = getDataTableIds(table);
		if(tableIds.isEmpty()){
			throw new RuntimeException("没有找到虚拟表引用的底层数据表");
		}
		tableId = tableIds.iterator().next();
		DataTable dataTable = dataTableRepos.findById(tableId).orElse(null);
		return dataTable;
	}

	/**
	 * 获取虚拟数据表table引用的所有底层实体数据表的所有ID
	 * @param table
	 * @return
	 */
	private List<String> getDataTableIds(VirtualTable table) {
		String sql = "SELECT " + 
			"count(tb.id) count, " + 
			"tb.id id " +
			"FROM " +
			"data_table tb " +
			"JOIN table_column tc ON tb.id = tc.data_table_id " +
			"JOIN virtual_column_ref_columns vcrc ON tc.id = vcrc.ref_columns_id " +
			"JOIN virtual_column vc ON vcrc.reference_by_id = vc.id " +
			"JOIN virtual_table vt ON vt.id = vc.table_id " +
			"WHERE " +
			"vt.id = ? " +
			"GROUP BY tb.id " +
			"ORDER BY count desc ";
		logger.debug(sql);
		List<String> tableIds = jdbcTemplate.query(sql, new Object[]{table.getId()}, (rs,i)->{
			String id = rs.getString("id");
			return id;
		});
		return tableIds;
	}

	@Override
	public DataTable getTableTree(VirtualTable table) {
		throw new UnsupportedOperationException("method dat.service.impl.VirtualTableServiceImpl.getTableTree(VirtualTable) is not implemented !");
	}

	@Override
	public boolean extract(VirtualTable table) {
		if(table.getClass().equals(VirtualTable.class)) {
			table = findById(table.getId());
		}
		List<VirtualColumn> columns = table.getColumns();
//		// 查询助手
//		QueryHelper queryHelper = context.getBean(QueryHelper.class);
		WorkPackageService packageService = context.getBean(WorkPackageService.class);
		
		// 查询结果适配器
		DataAdapter dataAdapter = new TableDataAdapter(columns);//queryHelper.query(table, columns);
		// 数据包ID，用来数据包对应的数据库连接
		String id = table.getPackages().iterator().next().getId();
		// 构建SQL数据
		String sql = insertSql(table);
		logger.debug(sql);
		// sqlite数据库只能单线程访问，所有使用线程锁
		synchronized (this) {
			try (Connection conn = packageService.getConnection(id);// 数据包所在数据库连接
					PreparedStatement ps = conn.prepareStatement(sql)){
				// 禁止自动提交
				conn.setAutoCommit(false);
				save(columns, dataAdapter, conn, ps);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param columns
	 * @param dataAdapter
	 * @param conn
	 * @param ps
	 * @throws SQLException
	 */
	private void save(List<VirtualColumn> columns, DataAdapter dataAdapter,
			Connection conn, PreparedStatement ps) throws SQLException {
		// 事务回滚点
		Savepoint setSavepoint = conn.setSavepoint();
		try {
			// 批处理执行频率
			int j = 1000;
			// 遍历查询结果适配器
			for(Map<String,String> map : dataAdapter){
				// 遍历查询的数据字段,设置SQL语句的参数
				for(int i = 0 , size = columns.size(); i < size; i++){
					String name = columns.get(i).getName();
					String value = map.get(name);
					ps.setString(i+1, value);
				}
				// 添加批处理
				ps.addBatch();
				if(--j == 0){
					// 执行批处理
					executeBatch(ps);
					j = 1000;
					conn.releaseSavepoint(setSavepoint);
					setSavepoint = conn.setSavepoint();
					logger.debug("插入成功");
				}
			}
			// 执行批处理
			executeBatch(ps);
			// 手动提交
			conn.commit();
		} catch (Exception e) {
			conn.rollback(setSavepoint);
			logger.debug("操作失败，回滚事务");
			e.printStackTrace();
		}
	}

	/**
	 * @param ps
	 * @throws SQLException
	 */
	private void executeBatch(PreparedStatement ps) throws SQLException {
		int[] executeBatch = ps.executeBatch();
		if(logger.isDebugEnabled()){
			int sum = 0;
			for (int i : executeBatch) {
				sum += i;
			}
			logger.debugf("批处理插入%d条数据",sum);
		}
		ps.clearBatch();
	}

	private String insertSql(VirtualTable table){
		List<VirtualColumn> columns = table.getColumns();
		String id2 = table.getId();
		// SQL缓冲区 
		StringBuffer buffer = new StringBuffer();
		StringBuffer b2 = new StringBuffer("VALUES (");
		buffer.append("INSERT INTO ");
		buffer.append(id2);
		buffer.append(" ( ");
		for(int i = 0 , size = columns.size(); i < size; i++) {
			VirtualColumn column = columns.get(i);
			buffer.append(column .getName());
			buffer.append(" , ");
			b2.append("? , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
		b2.delete(b2.length()-3, b2.length());
		buffer.append(")");
		b2.append(")");
		buffer.append(b2);
		return buffer.toString();
	}

	@Override
	public int getType(VirtualTable table) {
		List<String> ids = getDataTableIds(table);
		if(ids.size() == 1)
			return 1;
		List<DataTable> dataTables = dataTableRepos.findByIdIn(ids);
		Set<Source> s = new HashSet<>();
		dataTables.forEach(dataTable->{
			Source source = dataTable.getSource();
			s.add(source);
		});
		if(s.size() == 1){
			return 2;
		}
		if(s.size() > 1)
			return 3;
		return 0;
	}

	 
	public interface ResultSetHandler<T>{
		T doResultSet(ResultSet resultSet) throws SQLException ;
	}


	@Override
	public com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> getTableBody(String id) {
		Graph<DataTable> graph = new Graph<DataTable>();
		
		// 获取数据表
		VirtualTable virtualTable = this.vtRepos.findById(id).orElse(null);
		// 数据表中的字段
		List<VirtualColumn> columns = virtualTable.getColumns();
		List<DataTable> tables = new ArrayList<>();
		// 遍历虚拟字段
		for (VirtualColumn virtualColumn : columns) {
			// 获取虚拟字段引用的实体字段
			List<TableColumn> list = virtualColumn.getRefColumns();
			// 遍历引用的实体字段
			for (TableColumn tableColumn : list) {
				DataTable dataTable = tableColumn.getDataTable();
				graph.addVertex(dataTable);
				tables.add(dataTable);
			}
		}
		// 系统中的所有外键信息
		List<ForeignKeyInfo> foreignKeys = foreignKeyInfoRepository.findAll();
		for (ForeignKeyInfo foreignKey : foreignKeys) {
			// 根据系统中的外键构建一张网图
			DataTable dataTable = foreignKey.getDataTable();
			DataTable refTable = foreignKey.getReferencedTable();
			graph.addVertex(dataTable);
			graph.addVertex(refTable);
			graph.addArc(dataTable, refTable);
		}
		
		Connectivity<DataTable> cty = new Connectivity<DataTable>(graph);
		List<Graph<DataTable>> subgraph = cty.getSubgraph();
		// 克鲁斯卡尔最小生成树算法
		Kruskal kruskal = new Kruskal();
		// 剪枝园丁
		Gardener gardener = new Gardener();
		// 查询结果
		com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> result = null;
		// 遍历所有子图
		for (Graph<DataTable> tmpgraph : subgraph) {
			// 计算最小生成树
			Tree<DataTable> tree = kruskal.minSpanningTree(tmpgraph);
			// 减去不需要的数据表
			gardener.pruning(tree, tables);
			// 剪枝后不为空树
			if(!tree.isEmpty()){
				// 计算每棵树中的数据
				com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> tmpTable = build(tree.getRoot());
				if(result == null){
					result = tmpTable;
				}else{
					// 子图与子图之间按照行号连接
					result = result.joinFull(tmpTable, "rownum", "rownum");
				}
			}
			
		}
		
		return result;
	}

	private com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> build(TreeNode<DataTable> treeNode) {
		DataTable data = treeNode.getData();
		Source source = data.getSource();
		MonaliseDataSource monaliseDataSource = MonaliseDataSource.from(source);
		try {
			// 查询根节点的数据
			com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> dataTable = monaliseDataSource.getDataTableBody(data, data.getColumns());
			// 遍历子节点
			for (TreeNode<DataTable> node : treeNode.getItems()) {
				DataTable table = node.getData();
				// 查询根节点与子节点之间的连接方式
				List<ForeignKeyInfo> list = foreignKeyInfoRepository.findAll((root, query, cb)->{
					Predicate equal = cb.equal(root.get("foreignKey").get("dataTable").get("id"), data.getId());
					Predicate equal2 = cb.equal(root.get("referencedColumn").get("dataTable").get("id"), table.getId());
					Predicate and = cb.and(equal, equal2);
					Predicate equal3 = cb.equal(root.get("foreignKey").get("dataTable").get("id"), table.getId());
					Predicate equal4 = cb.equal(root.get("referencedColumn").get("dataTable").get("id"), data.getId());
					Predicate and2 = cb.and(equal3, equal4);
					return cb.or(and, and2);
				}).stream().distinct().collect(Collectors.toList());
				// 查子节点中的数据
				com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> build = build(node);
				
				// 连接根节点与子节点
				ForeignKeyInfo foreignKeyInfo = list.iterator().next();
				TableColumn foreignKey = foreignKeyInfo.getForeignKey();
				TableColumn referencedColumn = foreignKeyInfo.getReferencedColumn();
				dataTable = dataTable.join(build, foreignKey.getColumnName(), referencedColumn.getColumnName());
			}
			return dataTable;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}


