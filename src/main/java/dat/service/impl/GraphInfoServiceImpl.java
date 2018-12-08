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
	}
}
