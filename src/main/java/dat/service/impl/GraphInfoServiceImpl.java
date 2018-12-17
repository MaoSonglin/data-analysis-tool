package dat.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.GraphInfo;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.repos.GraphInfoRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.service.GraphInfoService;
import dat.service.WorkPackageService;
import dat.util.Constant;
import dat.util.SqlHelper;
import dat.util.VariableTypeParser;
import dat.vo.Response;
import dat.vo.TableDataPagingBean;

@Service
public class GraphInfoServiceImpl implements GraphInfoService {
	private static Logger logger = Logger.getLogger(GraphInfoServiceImpl.class);
	@Autowired
	private GraphInfoRepository graphInfoRepos;
	
	@Autowired
	private VirtualColumnRepository virtualColumnRepos;
	
	@Autowired
	private TableColumnRepository tableColumnRepos;
	
	@Autowired
	private WorkPackageService workPackageService;
	
	@Autowired
	private ConfigurableApplicationContext  context;
	
	@Transactional
	@Override
	public Response save(GraphInfo graphInfo) {
		logger.debug("保存图表信息:"+graphInfo);
		
		String title = graphInfo.getTitle();
		long count = 1,i = 1;
		while(count > 0){
			// 检测是否标题重复
			Specification<GraphInfo> spec = (root,query,cb)->{
				Predicate p1 = cb.equal(root.get("title"), graphInfo.getTitle());
				Predicate p2 = cb.notEqual(root.get("id"), graphInfo.getId());
				Predicate p3 = cb.equal(root.get("report").get("id"), graphInfo.getReport().getId());
				return cb.and(p1,p2,p3);
			};
			count = graphInfoRepos.count(spec);
			if(count > 0){
				graphInfo.setTitle(title + i++);
			}
		}
		GraphInfo save = graphInfoRepos.save(graphInfo);
		logger.debug("保存结果："+save);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

	@Override
	public GraphInfo getById(String id) {
		logger.debug("获取ID为"+id+"的图表");
		Optional<GraphInfo> optional = graphInfoRepos.findById(id);
		try {
			GraphInfo graphInfo = optional.get();
			if(Constant.DELETE_STATE == graphInfo.getState())
				return null;
			return graphInfo;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@Transactional
	public Response delete(String id) {
		logger.debug("删除ID为"+id+"的报表");
		graphInfoRepos.deleteById(id);
		return new Response(Constant.SUCCESS_CODE,"删除成功",id);
	}

	public List<List<String>> getData(String id) throws Exception{
		// 获取ID对应的报表
		GraphInfo g = graphInfoRepos.findById(id).get();
		// 数据表集合
		Set<VirtualTable> tableSet = new HashSet<>();
		// 待查的数据字段
		List<VirtualColumn> columns = g.getColumns();
		// 遍历数据字段获取对应的字段所在的数据表
		columns.forEach(column->{tableSet.add(column.getTable());});
		// 构建SQL语句
		StringBuffer buffer = SqlHelper.selectList(columns, "name","id");
		SqlHelper.from(buffer, tableSet, "id");
		SqlHelper.limit(buffer, 0, 10);
		// 报表中的数据表所属的业务包的ID
		String wpkgid = tableSet.iterator().next().getPackages().iterator().next().getId();
		// 因为对sqlite数据库只支持单线程访问，所有使用synchronized锁
		synchronized (this) {
			try (Connection conn = workPackageService.getConnection(wpkgid)){
				logger.debug(buffer);
				Map<VirtualColumn, String> map = getDataType(columns, buffer, conn);
				// SQL语句
				String sql = querySql(tableSet, columns, map);
				logger.debug(sql);
				try(PreparedStatement ps = conn.prepareStatement(sql);
						ResultSet rs = ps.executeQuery()) {
					Map<String, List<String>> data = VirtualTableServiceImpl.getMapListHandler().doResultSet(rs);
					List<List<String>> res = format(columns,data);
					return res;
				}
			} 
		}
	}

	/**
	 * @param tableSet
	 * @param columns
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private String querySql(Set<VirtualTable> tableSet,
			List<VirtualColumn> columns, Map<VirtualColumn, String> map)
			throws Exception {
		StringBuffer sb = new StringBuffer("SELECT ");
		Iterator<VirtualColumn> iterator = columns.iterator();
		VirtualColumn virtualColumn = iterator.next();
		String name = virtualColumn.getName();
		sb.append(name).append(" , ");
		while(iterator.hasNext()){
			virtualColumn = iterator.next();
			String typeName = map.get(virtualColumn);
			if(typeName.equalsIgnoreCase("number")){
				sb.append("SUM(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}else{
				sb.append("COUNT(")
				.append(virtualColumn.getName())
				.append(") ")
				.append(virtualColumn.getName())
				.append(" , ");
			}
		}
		SqlHelper.deleteLast(sb, 3);
		SqlHelper.from(sb, tableSet, "id");
		sb.append(" WHERE ").append(name).append(" IS NOT NULL ");
		sb.append(" GROUP BY ");
		sb.append(name);
		sb.append(" ORDER BY ");
		sb.append(name);
		
		String sql = sb.toString();
		return sql;
	}

	
	/**
	 * 各个字段的数据的真实数据类型
	 * @param columns
	 * @param buffer
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private Map<VirtualColumn, String> getDataType(List<VirtualColumn> columns,
			StringBuffer buffer, Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(buffer.toString());
		ResultSet rs = ps.executeQuery();
		VariableTypeParser typeParser = new VariableTypeParser();
		Map<VirtualColumn,String> map = new HashMap<>();
		while(rs.next()){
			columns.forEach(elem->{
				String id2 = elem.getId();
				try {
					// 获取值
					String value = rs.getString(id2);
					// 获取值的数据类型
					typeParser.setVariable(value);
					String typeName2 = typeParser.getTypeName();
					String typeName = map.get(elem);
					if(typeName == null)
						map.put(elem, typeName2);
					else if(!typeName.equalsIgnoreCase(typeName2)){
						if("any".equalsIgnoreCase(typeName2)){
							map.put(elem,typeName);
						}else{
							map.put(elem,"String");
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return map;
	}

	@Override
	public Response getData(TableDataPagingBean pagingBean) throws Exception {
		GraphInfo g = pagingBean.getGraph();
		g = graphInfoRepos.findById(g.getId()).get();
		if(logger.isDebugEnabled()){
			logger.debugf("查询出图表“%s”需要展示的数据'%d'",g.getTitle(),g.getColumns().size());
		}
		// 待查询的数据表与检索的字段映射
		Map<VirtualTable,List<VirtualColumn>> map = new HashMap<>(); 
		List<VirtualColumn> columns = g.getColumns();
		// 提取检索的字段和数据表
		setTableMapColumn(columns, map);
		
		// 查询的结果
		Map<String, List<String>> data = new HashMap<>();
		// 数据包服务层接口
		WorkPackageService packageService = context.getBean(WorkPackageService.class);
		// loop through
		for (Entry<VirtualTable, List<VirtualColumn>> entry : map.entrySet()) {
			// 待访问的数据表
			VirtualTable key = entry.getKey();
			// 待查的列
			List<VirtualColumn> value = entry.getValue();
//			Map<String, List<String>> data2 = tableService.getData(key, value,pagingBean.getCurPage()*pagingBean.getPageSize(),pagingBean.getPageSize());
//			data.putAll(data2);
			// 虚拟数据表所属的数据包
			WorkPackage wpkg = key.getPackages().iterator().next();
			try(Connection conn = packageService.getConnection(wpkg.getId());){
				
				// 查询所所有的数据，分别出每一列的数据的数据类型
				// 查询数据的SQL语句
				String sql = selectSql1(key, value);
				// 获取字段数据类型
				Map<VirtualColumn, Set<String>> columnTypeNames = getTypeNames(conn,sql,value);
				// 查询SQL语句
				String sb = selectSql2(key, value, columnTypeNames);
				try (PreparedStatement ps = conn.prepareStatement(sb);
						ResultSet rs = ps.executeQuery()){
					Map<String, List<String>> data2 = VirtualTableServiceImpl.getMapListHandler().doResultSet(rs);
					data.putAll(data2);
				}
				
			} catch (SQLException e){
				
			}
			
		}
		// 将数据格式转化为ECharts能够识别的数据格式
		List<List<String>> list = format(columns, data);
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功");
		res.setData(list);
		return res;
	}

	/**
	 * @param columns
	 * @param data
	 * @return
	 */
	private List<List<String>> format(List<VirtualColumn> columns,
			Map<String, List<String>> data) {
		List<List<String>> list = new ArrayList<>(data.size());
		data.forEach((key,value)->{
			String columnName = getColumnName(key, columns);
			value.add(0, columnName);
			list.add(value);
		});
		return list;
	}

	/**
	 * @param key				待查询的数据表
	 * @param value				待查询的列
	 * @param columnTypeNames	待查询的列到该列中包含的数据类型的映射
	 * @return 					SQL语句
	 */
	private String selectSql2(VirtualTable key, List<VirtualColumn> value,
			Map<VirtualColumn, Set<String>> columnTypeNames) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ");
		
		// 待查的列的第一条数据默认为分组的对象
		Iterator<VirtualColumn> iterator = value.iterator();
		VirtualColumn groupTarget = iterator.next();
		sb.append(groupTarget.getName()).append(" , ");
		
		while(iterator.hasNext()){
			VirtualColumn virtualColumn = iterator.next();
			// 该列中的数据包含的数据类型字符串集合
			Set<String> typeNames = columnTypeNames.get(virtualColumn);
			// 默认都是String类型
			String type = "String";
			// 如果该列中只包含一种数据类型
			if(typeNames.size() == 1){
				type = typeNames.iterator().next();
			}
			
			// 如果当前列的所有数据类型都是number
			if("Number".equals(type)){
				sb.append("SUM(").append(virtualColumn.getName()).append(") ");
			}
			else{// 包含其他类型
				sb.append("COUNT(").append(virtualColumn.getName()).append(") ");
			}
			sb.append(virtualColumn.getName());
			sb.append(" , ");
		}
		sb.delete(sb.length()-3, sb.length());
		sb.append(" FROM ");
		sb.append(key.getId());
		// 忽略掉分组列中为null的数据
		sb.append(" WHERE ").append(groupTarget.getName()).append(" IS NOT NULL ");
		sb.append("GROUP BY ").append(groupTarget.getName());
		logger.debug(sb);
		return sb.toString();
	}

	/**
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	private Map<VirtualColumn, Set<String>> getTypeNames(Connection conn,String sb,
			List<VirtualColumn> value) throws SQLException {
		// 存放数据字段和数据字段的类型的映射
		Map<VirtualColumn,Set<String>> columnTypeNames = new HashMap<>();
		value.forEach(e->{
			columnTypeNames.put(e, new HashSet<>());
		});
		// 解析变量类型的解析器
		VariableTypeParser typeParser = new VariableTypeParser();
		try(PreparedStatement ps = conn.prepareStatement(sb.toString());
				ResultSet rs = ps.executeQuery()){
			while(rs.next()){
				for (VirtualColumn virtualColumn : value) {
					String name = virtualColumn.getName();
					String columnValue = rs.getString(name);
					typeParser.setVariable(columnValue);
					// 变量类型
					String typeName;
					try {
						typeName = typeParser.getTypeName();
						columnTypeNames.get(virtualColumn).add(typeName);
					} catch (Exception e) {
					}
				}
			}
		}
		return columnTypeNames;
	}

	/**
	 * @param key
	 * @param value
	 * @param columnTypeNames
	 * @return
	 */
	private String selectSql1(VirtualTable key,
			List<VirtualColumn> value) {
		StringBuffer sb = new StringBuffer("select ");
		value.forEach(column->{
			String name = column.getName();
			sb.append(name).append(" , ");
			
		});
		sb.delete(sb.length()-3, sb.length());
		sb.append(" from ");
		sb.append(key.getId());
		sb.append(" limit 0,20");
		logger.debug(sb);
		return sb.toString();
	}

	/**
	 * @param key
	 * @param x_axis
	 * @return
	 */
	private String getColumnName(String key, List<VirtualColumn> x_axis) {
		for (VirtualColumn virtualColumn : x_axis) {
			if(virtualColumn.getName().equals(key)){
				String chinese = virtualColumn.getChinese();  
				if(chinese != null) key = chinese;
			}
		}
		return key;
	}


	/**
	 * 遍历数据表数组x_axis，获取每个字段所属的数据表信息，并将该字段添加到映射map当中，
	 * map是一个VirtualTable到Set<VirtualColumn>的映射，表示某个虚拟数据表
	 * 包含的字段
	 * @param x_axis
	 * @param map
	 */
	private void setTableMapColumn(List<VirtualColumn> x_axis,
			Map<VirtualTable, List<VirtualColumn>> map) {
		for (VirtualColumn virtualColumn : x_axis) {
			VirtualTable table = virtualColumn.getTable();
			List<VirtualColumn> list = map.get(table);
			if(list == null){
				list = new ArrayList<>();
				map.put(table, list);
			}
			list.add(virtualColumn);
		}
		if(logger.isDebugEnabled()){
			StringBuffer sb = new StringBuffer();
			map.forEach((table,columns)->{
				columns.forEach(column->{
					sb.append(table.getName()).append(".").append(column.getName()).append(";");
				});
			});
			sb.deleteCharAt(sb.length()-1);
			logger.debug("待查询的列："+sb.toString());
		}
	}
}
