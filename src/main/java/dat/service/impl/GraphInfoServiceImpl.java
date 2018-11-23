package dat.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	
	/*public Response getData(GraphInfo g) throws Exception{
		List<VirtualColumn> x_axis = g.getxAxis();
		for (VirtualColumn x : x_axis) {
			// 查找出虚拟字段信息
			List<? extends Object> xdata = getVirtualColumnData(x);
			String name = x.getName();
			GraphData graphData = new GraphData();
			g.getyAxis().forEach(y->{
				VirtualColumn column = virtualColumnRepos.findById(y.getId()).get();
				TableColumn tc = column.getRefColumns().get(0);
				DataTable dataTable = tc.getDataTable();
				Source source = dataTable.getSource();
				JdbcTemplate template = getTemplate(source);
				
				String columnName = tc.getColumnName();
				xdata.forEach(elem->{
					String sql = String.format("select %s,? as %s from %s where %s = ?",columnName,"x_value",dataTable.getName(),columnName);
					logger.debug("查询语句："+sql);
					List<String> values = template.query(sql, (rs,i)->{
						String columnValue = rs.getString(columnName);
						String v_value = rs.getString("x_value");
						return columnValue;
					}, elem,elem);
					
				});
			});
			
			virtualColumnRepos.findAll((root,query,cb)->{
				cb.equal(root.get(name), xdata.get(0));
				return null;
			});
		}
		return null;
	}*/

	/**
	 * @param source
	 * @return
	 */
	public JdbcTemplate getTemplate(Source source) {
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
		g = graphInfoRepos.findById(g.getId()).get();
		List<VirtualColumn> x_axis = g.getxAxis();
		List<VirtualColumn> y_axis = g.getyAxis();
		
		Set<VirtualTable> findAll = getVirtualTables(x_axis, y_axis);
		
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

}
