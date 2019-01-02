package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import dat.data.LocalDataAdapter;
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
import dat.util.DrillUtils;
import dat.vo.EchartOptions;
import dat.vo.GraphDrillData;
import dat.vo.Response;
import dat.vo.Serie;
import dat.vo.TreeNode;

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
		logger.debug("保存图表信息:"+JSON.toJSONString(graphInfo));
		
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
		return graphInfoRepos.findById(id).orElse(null);
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
//		Set<VirtualTable> tableSet = new HashSet<>();
		// 待查的数据字段
		List<VirtualColumn> columns = g.getColumns();
		String options = g.getOptions();
		EchartOptions option = JSON.parseObject(options, EchartOptions.class);
		columns.removeIf(elem->{
			String id2 = elem.getId();
			if(id2.equalsIgnoreCase(option.getCategory())){
				return false;
			}
			List<Serie> series = option.getSeries();
			for (Serie serie : series) {
				boolean equalsIgnoreCase = id2.equalsIgnoreCase(serie.getColumnId());
				if(equalsIgnoreCase){
					return false;
				}
			}
			return true;
		});
		Map<String, List<String>> query = new LocalDataAdapter(context).query(columns);
		return format(columns,query);
	}

	
	
	/**
	 * @param columns
	 * @param data
	 * @return
	 */
	private List<List<String>> format(List<VirtualColumn> columns,
			Map<String, List<String>> data) {
		List<List<String>> list = new ArrayList<>(data.size());
		columns.forEach(column->{
			String name = column.getName();
			List<String> list2 = data.get(name);
			if(list2 == null)
				return ;
			String chinese = column.getChinese();
			list2.add(0, chinese==null?name:chinese);
			list.add(list2);
		});
		return list;
	}

	

	@Override
	public Object drill(GraphDrillData drillData) throws Exception{
		GraphInfo graphInfo = graphInfoRepos.findById(drillData.getGraphId()).orElse(null);
		VirtualColumn virtualColumn = virtualColumnRepos.findById(drillData.getColumnId()).orElse(null);
		List<VirtualColumn> columns = graphInfo.getValueColumns();// graphInfo.getColumns();
		// 解析获取echart的配置对象
		String options = graphInfo.getOptions();
		EchartOptions echartOptions = DrillUtils.parseOptions(options);
		System.out.println(echartOptions);
		// 系列
//		List<Serie> series = echartOptions.getSeries();
		// 过滤掉不用显示的数据字段
		/*columns.removeIf(elem->{
			for(Serie serie : series){
				// 如果字段elem的ID保存在serie的属性中，则不移除
				if(elem.getId().equalsIgnoreCase(serie.getColumnId())){
					return false;
				}
			}
			// 如果elem的ID不存在所有的series中，则移除
			return true;
		});*/
		// 添加作为钻取维度的列
		columns.add(0,virtualColumn);
		Map<String, List<String>> map = new LocalDataAdapter(context).query(columns,DrillUtils.getSqlInfo(drillData, virtualColumn, columns));
		// 重新设置数据格式
		List<List<String>> list = format(columns,map);
		// echarts 配置对象
		// 设置dataset
		DrillUtils.setDataSet(echartOptions, list);
		// 设置系列颜色
		DrillUtils.setSeriesColor(echartOptions);
		// 设置轴
		DrillUtils.setAxis(virtualColumn, echartOptions);
		// 设置滑块组件
		DrillUtils.setDataZoom(echartOptions, list);
		return echartOptions;
	}

	@Override
	public TreeNode findTree(String id) {
		GraphInfo graph = this.graphInfoRepos.findById(id).orElse(null);
		if(graph == null){
			return null;
		}
		// 已经添加的字段
		List<VirtualColumn> excludes = graph.getColumns();
		// 所属数据包
		WorkPackage pkg = graph.getReport().getPkg();
		TreeNode root = new TreeNode(pkg.getId(),pkg.getName(),new ArrayList<>());
		// 数据包中包含的数据表
		List<VirtualTable> tables = pkg.getTables();
		for (VirtualTable virtualTable : tables) {
			// 遍历数据包中的数据表
			TreeNode treeNode = new TreeNode(virtualTable.getId(),virtualTable.getChinese()!=null?virtualTable.getChinese():virtualTable.getName(),new ArrayList<>());
			// 数据表中的字段
			List<VirtualColumn> columns = virtualTable.getColumns();
			for (VirtualColumn virtualColumn : columns) {
				// 不添加已经添加到图表中的字段
				if(excludes.contains(virtualColumn))
					continue;
				TreeNode tn = new TreeNode();
				tn.setId(virtualColumn.getId());
				tn.setText(virtualColumn.getChinese() != null ? virtualColumn.getChinese():virtualColumn.getName());
				tn.setText(tn.getText()+"("+virtualColumn.getTypeName()+")");
				tn.setType(virtualColumn.getTypeName());
				treeNode.getNodes().add(tn);
			}
			root.getNodes().add(treeNode);
		}
		return root;
	}

	@Override
	@Transactional
	public Response addColumn(String gpid, String vcid) {
		GraphInfo orElse = this.graphInfoRepos.findById(gpid).orElse(null);
		if(orElse == null){
			return new Response(Constant.ERROR_CODE,"指定id的图表不存在");
		}
		VirtualColumn virtualColumn = this.virtualColumnRepos.findById(vcid).orElse(null);
		if(virtualColumn == null){
			return new Response(Constant.ERROR_CODE,"字段不存在");
		}
		orElse.getColumns().add(virtualColumn);
		this.graphInfoRepos.save(orElse);
		return new Response(Constant.SUCCESS_CODE,"添加成功",orElse);
	}

	
}
