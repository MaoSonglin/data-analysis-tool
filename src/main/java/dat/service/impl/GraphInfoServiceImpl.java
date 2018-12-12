package dat.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
import dat.repos.GraphInfoRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.service.GraphInfoService;
import dat.service.VirtualTableService;
import dat.util.Constant;
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


	@Override
	public Response getData(TableDataPagingBean pagingBean) throws Exception {
		GraphInfo g = pagingBean.getGraph();
		g = graphInfoRepos.findById(g.getId()).get();
		if(logger.isDebugEnabled()){
			String title = g.getTitle();
			List<VirtualColumn> columns = g.getColumns();
			logger.debug("查询出图表“"+title+"”需要展示的数据"+columns.size());
		}
		// 待查询的数据表与检索的字段映射
		Map<VirtualTable,List<VirtualColumn>> map = new HashMap<>(); 
		List<VirtualColumn> columns = g.getColumns();
		// 提取检索的字段和数据表
		setTableMapColumn(columns, map);
		
		// 查询的结果
		Map<String, List<String>> data = new HashMap<>();
		// 虚拟数据表访问接口
		VirtualTableService tableService = context.getBean(VirtualTableService.class);
		for (Entry<VirtualTable, List<VirtualColumn>> entry : map.entrySet()) {
			VirtualTable key = entry.getKey();
			List<VirtualColumn> value = entry.getValue();
			Map<String, List<String>> data2 = tableService.getData(key, value,pagingBean.getCurPage()*pagingBean.getPageSize(),pagingBean.getPageSize());
			data.putAll(data2);
		}
		List<List<String>> list = new ArrayList<>(data.size());
		data.forEach((key,value)->{
			String columnName = getColumnName(key, columns);
			value.add(0, columnName);
			list.add(value);
		});
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功");
		res.setData(list);
		return res;
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
