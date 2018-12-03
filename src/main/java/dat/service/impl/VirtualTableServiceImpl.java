package dat.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import dat.data.VirtualTableConnManager;
import dat.data.VirtualTableQueryHandler;
import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.DataTableRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.service.DataTableService;
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

	public Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns,int offset,int limit) {
		// 构建SQL语句
		String sql = constructSQL(table, columns);
		Map<String,List<String>> map = new HashMap<>();
		for (VirtualColumn virtualColumn : columns) {
			map.put(virtualColumn.getName(), new ArrayList<>(limit));
		}
		logger.debug("在虚拟数据表上执行SQL语句："+sql);
		try (Connection conn = VirtualTableConnManager.getConnection(table);
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()){
			rs.absolute(offset);	
			while(rs.next() && limit-- > 0){
				Set<Entry<String,List<String>>> entrySet = map.entrySet();
				for (Entry<String, List<String>> entry : entrySet) {
					entry.getValue().add(rs.getString(entry.getKey()));
				}
			}
			logger.debug("查询成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("查询失败");
		}
		return map;
	}

	/**
	 * @param table
	 * @param columns
	 * @return 
	 */
	private String constructSQL(VirtualTable table, List<VirtualColumn> columns) {
		StringBuffer sb = new StringBuffer("select ");
		for (VirtualColumn virtualColumn : columns) {
			sb.append(virtualColumn.getName()).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" from ");
		sb.append(table.getName());
		String sql = sb.toString();
		return sql;
	}
	
	@Deprecated
	public List<Map<String,String>> getData(VirtualTable t){
		List<VirtualColumn> vcolumns = t.getColumns();
		if(vcolumns == null || vcolumns.isEmpty()){
			vcolumns = virtualColumnRepos.findAll((root,query,cb)->{
				return cb.equal(root.get("table").get("id"), t.getId());
			});
			System.err.println(vcolumns.size());
		}
		
		// 待查询的数据表目录，key代表要查询的实际数据表格，value代表待查询的表格中需要检索的字段
		Map<DataTable, Set<TableColumn>> map = getQueryField(vcolumns);
		
		Map<DataTable, List<Map<String, String>>> m = toQuery(map);
		
		List<Map<String,String>> values = new ArrayList<>(m.size());
		// TODO 把多个表连接成一个表
		List<Map<String,String>> xx = join(m,t);
		for (Map<String, String> map2 : xx) {
			Map<String,String> row = new HashMap<>();
			for (VirtualColumn vc : vcolumns) {
				// TODO 按照公式计算虚拟字段的值，这里默认将虚拟字段和真实字段一一对应了
				String columnName = vc.getName();//tableColumn.getColumnName();
				String v = map2.get(columnName);
				row.put(vc.getName(), v);
			}
			values.add(row);
		}
		
		return values;
	}

	/**
	 * @param map
	 * @return
	 */
	public Map<DataTable, List<Map<String, String>>> toQuery(
			Map<DataTable, Set<TableColumn>> map) {
		Map<DataTable,List<Map<String,String>>> m = new HashMap<>();
		// 遍历查询每一张表
		map.forEach((key,value)->{
			// 查询出一张表的数据
			List<Map<String,String>> values = dataTableService.getValues(key, new ArrayList<>(value));
			m.put(key, values);
		});
		return m;
	}

	/**
	 * @param vcolumns
	 * @return
	 */
	public Map<DataTable, Set<TableColumn>> getQueryField(
			List<VirtualColumn> vcolumns) {
		Map<DataTable,Set<TableColumn>> map = new HashMap<>();
		Set<String> ids = new HashSet<>();
		vcolumns.forEach(elem->{
			ids.add(elem.getId());
		});
		
		// 遍历虚拟数据表中的虚拟字段
		vcolumns.forEach(elem->{
			// 遍历虚拟字段引用的实际字段
			elem.getRefColumns().forEach(column->{
				// 获取实际字段所在的数据表 TODO 待优化
				DataTable dataTable = column.getDataTable();
				// 将数据表和字段添加到待查询的目录中
				Set<TableColumn> s = map.get(dataTable);
				if(s == null){
					s = new HashSet<>();
					map.put(dataTable, s);
				}
				s.add(column);
			});
		});
		return map;
	}
	public Map<DataTable, Set<TableColumn>> getQueryField2(
			List<VirtualColumn> vcolumns) {
		Map<DataTable,Set<TableColumn>> map = new HashMap<>();
		Set<String> ids = new HashSet<>();
		vcolumns.forEach(elem->{
			ids.add(elem.getId());
		});
		/*List<TableColumn> columns = tableColumnRepos.findByVirtualColumnIds(ids);
		for (TableColumn tableColumn : columns) {
			
		}*/
		return map;
	}

	/**
	 * 将多张表的数据连接成一张表的数据
	 * @param m
	 * @param t
	 * @return
	 */
	private List<Map<String,String>>  join(Map<DataTable, List<Map<String, String>>> m,
			VirtualTable t) {
		// TODO 待修改，这里只是默认所有数据都来自一个表
		Set<Entry<DataTable,List<Map<String,String>>>> entrySet = m.entrySet();
		for (Entry<DataTable, List<Map<String, String>>> entry : entrySet) {
			return entry.getValue();
		}
		return null;
	}

	public Map<String, List<String>> listData(VirtualTable t) {
		VirtualTable table = vtRepos.findById(t.getId()).get();
		VirtualTableQueryHandler queryHandler = new VirtualTableQueryHandler(table);
		try {
			Map<String, List<String>> list = queryHandler.list();
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
