package dat.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import dat.data.DataAdapter;
import dat.data.QueryHelper;
import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.DataTableRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.service.DataTableService;
import dat.service.TableColumnService;
import dat.service.VirtualTableService;
import dat.util.Constant;
import dat.vo.Response;

@Service
public class VirtualTableServiceImpl implements VirtualTableService {

	private static Logger logger = Logger.getLogger(VirtualTableServiceImpl.class);
	
	@Resource(name="virtualTableRepository")
	private VirtualTableRepository vtRepos;
	
	@Resource(name="virtualColumnRepository")
	private VirtualColumnRepository virtualColumnRepos;
	
	@Resource(name="dataTableRepository")
	private DataTableRepository dataTableRepos;
	
	@Resource(name="dataTableServiceImpl")
	private DataTableService dataTableService;
	
	@Resource(name="tableColumnRepository")
	private TableColumnRepository tableColumnRepos;

	@Resource(name="tableColumnServiceImpl")
	private TableColumnService tableColumnService;
	
	@Autowired
	private ApplicationContext context;

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	public Response getById(String id) {
		try {
			VirtualTable virtualTable = vtRepos.getOne(id);
			Class<? extends VirtualTable> cls = virtualTable.getClass();
			if(logger.isDebugEnabled()){
				String name = cls.getName();
				System.err.println("virtual table instance class name is "+name);
				Field[] declaredFields = cls.getDeclaredFields();
				System.err.println("******************************************************");
				for (Field field : declaredFields) {
					String typeName = field.getType().getName();
					System.err.println(field.getName()+":"+typeName);
				}
				Method[] declaredMethods = cls.getDeclaredMethods();
				for (Method method : declaredMethods) {
					System.err.println(method.getName());
				}
				System.err.println("******************************************************");
			}
			return new Response(Constant.SUCCESS_CODE,"查询成功",virtualTable);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,String.format("ID为“%s”的虚拟数据表不存在！", id));
		}
	}

	public Response getVirtualColumns(String id) {
//		List<VirtualColumn> virtualColumns = vtRepos.getColumnsWithId(id);
		
		List<VirtualColumn> virtualColumns = virtualColumnRepos.findAll((root,query,cb)->{
			Path<String> path = root.get("table").get("id");
			Predicate equal = cb.equal(path, id);
			Predicate equal2 = cb.equal(root.get("state"), Constant.ACTIVATE_SATE);
			return cb.and(equal,equal2);
		});
		if(logger.isDebugEnabled() && !virtualColumns.isEmpty()){
			String simpleName = virtualColumns.get(0).getClass().getName();
			logger.debug(simpleName);
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
		return null;
	}

	@Override
	public Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns,int offset,int limit) {
		table = vtRepos.findById(table.getId()).get();
		// 存放查询结果的map
		Map<String, List<String>> m = getEmptyResultMap(columns);
		// 查询助手
		QueryHelper queryHelper = context.getBean(QueryHelper.class);
		// 查询开始
		try (DataAdapter dataAdapter = queryHelper.query(table, columns);) {
			// 设置分页
			dataAdapter.limit(offset, limit);
			
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
	public List<Map<String,String>> getData(VirtualTable t){
		QueryHelper queryHelper = context.getBean(QueryHelper.class);
		try(DataAdapter adapter = queryHelper.query(t, t.getColumns())){
			List<Map<String,String>> list = new LinkedList<>();
			for (Map<String, String> map : adapter) {
				list.add(map);
			}
			return list;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, List<String>> listData(VirtualTable t) {
		QueryHelper helper = context.getBean(QueryHelper.class);
		VirtualTable table = vtRepos.findById(t.getId()).get();
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
		return resultMap;
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
		// 待查询的虚拟字段
		List<VirtualColumn> columns = table.getColumns();
		
		List<TableColumn> tableColumns = tableColumnService.getColumnsByVirtualColumns(columns);
		Set<String> ids = new HashSet<>();
		tableColumns.forEach(tableColumn->{ ids.add(tableColumn.getId());});
		List<DataTable> dataTables = dataTableRepos.findAll((root,query,cb)->{
			Predicate in = root.join("columns").get("id").in(ids);
			return in;
		});
		HashSet<DataTable> hashSet = new HashSet<>(dataTables);
		return new ArrayList<>(hashSet);
	}


	public DataTable getMainTable(VirtualTable table) {
		String sql = "\nSELECT \n" + 
			"	count(tb.id) count, \n" + 
			"	tb.id id \n" +
			"FROM \n" +
			"	data_table tb \n" +
			"JOIN table_column tc ON tb.id = tc.data_table_id \n" +
			"JOIN virtual_column_ref_columns vcrc ON tc.id = vcrc.ref_columns_id \n" +
			"JOIN virtual_column vc ON vcrc.virtual_column_id = vc.id \n" +
			"JOIN virtual_table vt ON vt.id = vc.table_id \n" +
			"WHERE \n" +
			"	vt.id = ? \n" +
			"GROUP BY tb.id \n" +
			"ORDER BY count desc \n";
		logger.debug(sql);
		String tableId = jdbcTemplate.query(sql, new Object[]{table.getId()}, (rs)->{
			while(rs.next()){
				String id = rs.getString("id");
				return id;
			}
			return null;
		});
		DataTable dataTable = dataTableRepos.findById(tableId).orElse(null);
		return dataTable;
	}

	@Override
	public DataTable getTableTree(VirtualTable table) {
		/*DataTable dataTable = getMainTable(table);
		List<DataTable> quoteTable = getQuoteTable(table);
		List<DataTable> backList = new ArrayList<>(quoteTable);
		if(!quoteTable.contains(dataTable)){
			throw new IllegalArgumentException();
		}else{
			int size = quoteTable.size();
			if(size == 1) return dataTable;
			else{
				removeFrom(quoteTable,dataTable);
				if(quoteTable.isEmpty()) return dataTable;
				backList.removeAll(quoteTable);
				
			}
		}*/
		
//		return null;
		throw new UnsupportedOperationException("method dat.service.impl.VirtualTableServiceImpl.getTableTree(VirtualTable) is not implemented !");
	}
}
