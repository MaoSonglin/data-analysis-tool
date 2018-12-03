package dat.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;

import dat.data.QueryHandler;
import dat.data.VirtualTableQueryHandler;
import dat.domain.DataTable;
import dat.domain.GraphInfo;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.GraphInfoRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.service.GraphInfoService;
import dat.service.VirtualTableService;
import dat.util.Constant;
import dat.vo.GraphData;
import dat.vo.GraphData2;
import dat.vo.GraphDataQueryBean;
import dat.vo.Response;

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
	private ConfigurableApplicationContext  context;
	
//	private ThreadLocal<Map<String,Integer>> indexMap = new ThreadLocal<Map<String,Integer>>();
	
	@Transactional
	public Response save(GraphInfo graphInfo) {
		logger.debug("保存图表信息:"+graphInfo);
		// 检测是否标题重复
		Specification<GraphInfo> spec = (root,query,cb)->{
			Predicate p1 = cb.equal(root.get("title"), graphInfo.getTitle());
			Predicate p2 = cb.notEqual(root.get("id"), graphInfo.getId());
			Predicate p3 = cb.equal(root.get("report").get("id"), graphInfo.getReport().getId());
			return cb.and(p1,p2,p3);
		};
		long count = graphInfoRepos.count(spec);
		if(count > 0){
			return new Response(Constant.ERROR_CODE,"图表"+graphInfo.getTitle()+"已经存在");
		}
		GraphInfo save = graphInfoRepos.save(graphInfo);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

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

	public Response delete(String id) {
		logger.debug("删除ID为"+id+"的报表");
		graphInfoRepos.deleteById(id);
		return new Response(Constant.SUCCESS_CODE,"删除成功",id);
	}
	
	

	/**
	 * @param source
	 * @return
	 */
	private JdbcTemplate getTemplate(Source source) {
		JdbcTemplate jdbcTemplate;
		String beanName = source.getName()+"jdbcTemplate";
		try {
			jdbcTemplate = (JdbcTemplate) context.getBean(beanName);
		} catch (Exception e) {
			String driverClass = source.getDriverClass();
			String url = source.getUrl();
			String username = source.getUsername();
			String password = source.getPassword();
			if(logger.isDebugEnabled()){
				logger.debug("jdbcTemplate instance named '"+beanName+"' is not exist, attempt created new one");
				logger.debug("get database connection with url "+url+" and username="+username+" and password="+password);
			}
			BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
			DruidDataSource ds = new DruidDataSource();
			ds.setDriverClassName(driverClass);
			ds.setUrl(url);
			ds.setUsername(username);
			ds.setPassword(password);
			beanDefinitionBuilder.addConstructorArgValue(ds);
			AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
			BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context.getBeanFactory();
			beanFactory.registerBeanDefinition(beanName, beanDefinition);
			jdbcTemplate = context.getBean(beanName, JdbcTemplate.class);
		}
		return jdbcTemplate;
	}
	
	@Deprecated
	public List<? extends Object> getVirtualColumnData(VirtualColumn vc){
		
		Optional<VirtualColumn> optional = virtualColumnRepos.findById(vc.getId());
		VirtualColumn virtualColumn = optional.get();
		Map<String,List<String>> map = new HashMap<>();
		virtualColumn.getRefColumns().forEach(item->{
			// 列名称
			String columnName = item.getColumnName();
			// 列所属的数据表
			DataTable dataTable = item.getDataTable();
			// 列所属的数据源
			Source source = dataTable.getSource();
			// 操作数据源的jdbcTemplate
			JdbcTemplate template = getTemplate(source);
			// 查询语句
			String sql = String.format("select %s from %s",columnName,dataTable.getName());
			List<String> list = template.query(sql, (rs,i)->{
				String columnValue = rs.getString(columnName);
				return columnValue;
			});
			map.put(item.getId(), list);
		});
		String formula = virtualColumn.getFormula();
		return map.get(formula);
	}
	
	protected String getJavaType(String jdbcType){
		return null;
	}

	@Override
	public Response getData(GraphInfo g) throws Exception {
		logger.debug("获取图表数据...");
		g = graphInfoRepos.findById(g.getId()).get();
		// 待查询的数据表与检索的字段映射
		Map<VirtualTable,List<VirtualColumn>> map = new HashMap<>();
		// x轴
		List<VirtualColumn> x_axis = g.getxAxis();
		// 提取x轴要检索的字段和数据表
		setTableMapColumn(x_axis, map);
		// 提取y轴要检索的字段和数据表
		List<VirtualColumn> y_axis = g.getyAxis();
		setTableMapColumn(y_axis,map);
		
		// 查询的结果
		Map<String, List<String>> data = null;
		// 虚拟数据表访问接口
		VirtualTableService tableService = context.getBean(VirtualTableService.class);
		for (Entry<VirtualTable, List<VirtualColumn>> entry : map.entrySet()) {
			VirtualTable key = entry.getKey();
			List<VirtualColumn> value = entry.getValue();
			if(data == null)
				data = tableService.getData(key, value);
			else
				data.putAll(tableService.getData(key, value));
		}
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功");
		res.setData(data);
		return res;
		/*Set<VirtualTable> findAll = getVirtualTables(x_axis, y_axis);
		
		queryLogger( findAll,x_axis,y_axis);
		if(findAll.size() == 1){
			VirtualTable vt = findAll.iterator().next();
			VirtualTableService virtualTableService = context.getBean(VirtualTableService.class);
			List<Map<String,String>> data = virtualTableService.getData(vt);

			List<List<String>> x_data_ = wrapper(x_axis, data);
			List<List<String>> y_data_ = wrapper(y_axis,data);
			
			Response res = new Response(Constant.SUCCESS_CODE,"查询成功");
			res.put("x", x_data_);
			res.put("y", y_data_);
			return res;
		}else{
			throw new RuntimeException("目前还么有实现多表查询");
		}
		*/
		/*
		
		List<GraphData2> list = new ArrayList<>();
		Set<DataTable> set = new HashSet<>();
		
		List<TableColumn> axisColumns = getAxisColumns(x_axis);
		axisColumns.addAll(getAxisColumns(y_axis));
		
		Specification<DataTable> spec = (root,query,cb)->{
			Set<String> ids = new HashSet<>();
			axisColumns.forEach(elem->{
				ids.add(elem.getId());
			});
			Predicate in = root.join("columns").get("id").in(ids);
			return in;
		};
		
		DataTableRepository dataTableRepos = context.getBean(DataTableRepository.class);
		List<DataTable> tables = dataTableRepos.findAll(spec);
		
		
		x_axis.forEach(x_elem->{
			y_axis.forEach(y_elem->{
				List<TableColumn> columns = y_elem.getRefColumns();
				TableColumn tableColumn = columns.get(0);
				DataTableService dataTableService = context.getBean(DataTableService.class);
				dataTableService.getValues(tableColumn.getDataTable(), columns);
				GraphData2 graphData = method(x_elem,y_elem);
				list.add(graphData);
			});
		});
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",list);
		return response;*/
	}

	/**
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
	}

	/**
	 * @param x_axis
	 * @param data
	 * @return 
	 */
	public List<List<String>> wrapper(List<VirtualColumn> x_axis,
			List<Map<String, String>> data) {
		List<List<String>> xData = new ArrayList<>(); 
		x_axis.forEach(elem->{
			String name = elem.getName();
			List<String> list = new ArrayList<>();
			for (Map<String,String> map : data) {
				String value = map.get(name);
				list.add(value);
			}
			xData.add(list);
		});
		return xData;
	}

	/**
	 * @param x_axis
	 * @param y_axis
	 * @return
	 */
	public Set<VirtualTable> getVirtualTables(List<VirtualColumn> x_axis,
			List<VirtualColumn> y_axis) {
		VirtualTableRepository vtRepos = context.getBean(VirtualTableRepository.class);
		Set<String> ids = new HashSet<>();
		x_axis.forEach(e->{
			ids.add(e.getId());
		});
		y_axis.forEach(e->{
			ids.add(e.getId());
		});
		List<VirtualTable> findAll = vtRepos.queryByColumnIdIn(ids);
		return new HashSet<>(findAll);
	}

	/**
	 * @param x_axis
	 * @param tableColumnRepository
	 * @return 
	 */
	public List<TableColumn> getAxisColumns(List<VirtualColumn> x_axis) {
		TableColumnRepository tableColumnRepository = context.getBean(TableColumnRepository.class);
		List<TableColumn> findAll = tableColumnRepository.findAll((root,query,cb)->{
			Join<TableColumn, VirtualColumn> join = root.join("virtualColumns");
			Set<String> ids  =new HashSet<>();
			x_axis.forEach(elem->{
				ids.add(elem.getId());
			});
			Predicate in = join.get("id").in(ids);
			return in;
		});
		return findAll;
	}
	
	protected GraphData2 method(VirtualColumn xColumn,VirtualColumn yColumn){
		TableColumn xc = xColumn.getRefColumns().get(0);
		TableColumn yc = yColumn.getRefColumns().get(0);
		String xLabel = xc.getColumnName();
		String yLabel = yc.getColumnName();
		
		DataTable dataTable = xc.getDataTable();
		Source source = dataTable.getSource();
		
		String tableName = dataTable.getName();
		JdbcTemplate template = getTemplate(source);
		String sql = String.format("select t.* from (select sum(%s) yLabel,%s from %s group by %s) t-- where rownum <= 10",yLabel,xLabel,tableName,xLabel);
		List<String> labels = new ArrayList<>();
		List<String> values = new ArrayList<>();
		Map<String,Integer> indexMap = new HashMap<>();
		logger.debug(sql);
		template.query(sql, (rs,i)->{
			String xValue = rs.getString(xLabel);
			String yValue = rs.getString("yLabel");
			if(!"0".equals(yValue) && null != xValue){
				labels.add(xValue);
				values.add(yValue);
			}
			indexMap.put(xValue, i);
			return null;
		});
//		this.indexMap.set(indexMap);
		if(logger.isDebugEnabled()){
			logger.debug(labels);
			logger.debug(values);
		}
		GraphData graphData = new GraphData();
		graphData.setxDatas(labels);
		graphData.getyDatas().add(values);
		GraphData2 graphData2 = new GraphData2();
		graphData2.setLabels(labels);
		graphData2.setValues(values);
		graphData2.setLegends(StringUtils.isEmpty(yc.getChinese()) ?  yLabel : yc.getChinese());
		return graphData2;
	}

	@Override
	public Response getGraphDataById(GraphDataQueryBean page) {
		GraphInfo g = graphInfoRepos.findById(page.getId()).get();
		// x轴
		List<VirtualColumn> xAxisColumns = g.getxAxis();
		// y轴
		List<VirtualColumn> yAxisColumns = g.getyAxis();
		
		// 数据表与该数据表多的查询对象的映射
		Map<VirtualTable,QueryHandler> m = new HashMap<>();
		getQueryHandlerIn(xAxisColumns, m);
		getQueryHandlerIn(yAxisColumns, m);
		int size = m.size();
		logger.debug(m);
		if(size == 1){
			Response res = new Response();
//			res.setData(data);
			Set<Entry<VirtualTable,QueryHandler>> entrySet = m.entrySet();
			for (Entry<VirtualTable, QueryHandler> entry : entrySet) {
				QueryHandler handler = entry.getValue();
				QueryHandler limit = handler.limit(page.getPage().intValue(), page.getLimit().intValue());
				try {
					Map<String, List<String>> list = limit.list();
					page.setData(list);
					res.setData(list);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return res;
		}
//		Set<VirtualTable> keySet = m.keySet();
//		for (VirtualTable table : keySet) {
//			QueryHandler queryHandler = m.get(table);
//			List<Association> foreigns = table.getForeigns();
//			for (Association assoc : foreigns) {
//				DataTable fkTable = assoc.getFkTable();
//			}
//		}
		return null;
	}

	/**
	 * 提取出字段的数据表，并且构建对应的数据表上的查询句柄对象
	 * @param yAxisColumns
	 * @param m
	 */
	private void getQueryHandlerIn(List<VirtualColumn> yAxisColumns,
			Map<VirtualTable, QueryHandler> m) {
		// 存放数据列的id的集合
		Set<String> ids = new HashSet<>();
		yAxisColumns.forEach(elem->{
			String id = elem.getId();
			ids.add(id);
		});
		
		// 查询出所有的需要查询的数据表
		VirtualTableRepository repos = context.getBean(VirtualTableRepository.class);
		List<VirtualTable> virtualTables = repos.findAll((root,query,cb)->{
			Predicate in = root.join("columns").get("id").in(ids);
//			Predicate in = root.get("columns").get("id").in(ids);
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return cb.and(notEqual,in);
		});
		// 遍历数据表
		for (VirtualTable key : virtualTables) {
			logger.debug("查询数据表"+key.getName());
			
			// 数据表包含的列
			List<VirtualColumn> columns = key.getColumns();
			
			// 获取数据表相关的查询句柄
			QueryHandler queryHandler = m.get(key);
			if(queryHandler==null){
				queryHandler = new VirtualTableQueryHandler(key);
			}
			// 遍历数据表的字段数组
			for (VirtualColumn vc : columns) {
				// 如果字段包含在待查询数组中
				if(yAxisColumns.contains(vc)){
					try {
						// 在查询句柄中添加该字段
						queryHandler.query(vc.getName(),vc.getChinese());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			m.put(key, queryHandler);
		}
		/*for (VirtualColumn vc : yAxisColumns) {
			VirtualTable table = vc.getTable();
			QueryHandler queryHandler = m.get(table);
			if(queryHandler == null){
				queryHandler = new VirtualTableQueryHandler(table);
				m.put(table, queryHandler);
			}
			try {
				queryHandler.query(vc.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}

	

}
