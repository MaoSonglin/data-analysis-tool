package dat.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.data.EchartOptionUtil;
import dat.data.EchartOptionUtil.QueryUnit;
import dat.data.LocalDataAdapter;
import dat.data.LocalDataAdapter.SqlBuilder;
import dat.data.TempTableAdapter;
import dat.domain.GraphInfo;
import dat.repos.GraphInfoRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.service.GraphInfoService;
import dat.service.WorkPackageService;
import dat.util.Constant;
import dat.util.SqlHelper;
import dat.vo.EchartOptions;
import dat.vo.EchartOptions.DataSet;
import dat.vo.GraphDrillData;
import dat.vo.Response;
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

	@Override
	public List<List<String>> getData(String id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object drill(GraphDrillData drillData) throws Exception {
		return getData2(drillData);
	}

	@Override
	public TreeNode findTree(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response addColumn(String gpid, String vcid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getAthers(String id) {
		GraphInfo graphInfo = graphInfoRepos.findById(id).orElse(null);
		if(graphInfo==null){
			return new Response(Constant.ERROR_CODE,"id不存在");
		}
		List<GraphInfo> graphs = graphInfo.getReport().getGraphs();
		ArrayList<GraphInfo> arrayList = new ArrayList<>(graphs);
		arrayList.removeIf(elem->{
			return elem.getId().equals(id);
		});
		return new Response(Constant.SUCCESS_CODE,"查询成功",arrayList);
	}
	
	protected Response getData(GraphDrillData data) throws Exception{
		// 分类id
		String graphId = data.getGraphId();
		GraphInfo graphInfo = graphInfoRepos.findById(graphId).orElse(null);
		if(graphInfo == null)
			return new Response(Constant.ERROR_CODE,"指定ID的图表不存在",data);
		
		// 待查的分类名称
		String itemName = data.getItemName();
		// 查询的数据名
		List<String> seriesName = data.getSeriesName();
		
		// 数据查询接口
		LocalDataAdapter localDataAdapter = new TempTableAdapter(context);
		SqlBuilder builder = getSqlBuilder(data, itemName, seriesName);
		
		// 查询结果处理器
		MatrixResultHandler matrixResultHandler = new MatrixResultHandler();
		ArrayList<String> names = new ArrayList<>(seriesName);
		names.add(itemName);
		matrixResultHandler.setNames(names);
		
		// 查询数据
		List<List<Object>> list = localDataAdapter.query(graphInfo.getReport().getColumns(), builder,matrixResultHandler);
		
		// 设置数据格式
		DataSet dataSet = new DataSet();
		dataSet.setDimensions(names);
		dataSet.setSource(list);
		return new Response(Constant.SUCCESS_CODE,"查询成功",dataSet);
	}

	/**
	 * @param data
	 * @param itemName
	 * @param seriesName
	 * @return
	 */
	private SqlBuilder getSqlBuilder(GraphDrillData data, String itemName,
			List<String> seriesName) {
		// SQL语句构造接口
		SqlBuilder builder = (tableName,sqlInfo)->{
			StringBuffer buffer = new StringBuffer("SELECT ");
			seriesName.forEach(elem->{
				SqlHelper.addFunction(buffer, "sum", elem, elem);
				buffer.append(" , ");
			});
			buffer.append(itemName).append(" FROM ").append(tableName);
			List<Object> param = SqlHelper.addWhere(data.getWheres(), buffer);
			buffer.append(" GROUP BY ").append(itemName);
			sqlInfo.setParams(param);
			sqlInfo.setSql(buffer.toString());
		};
		return builder;
	}
	
	protected Response getData2(GraphDrillData data) throws Exception{
		String graphId = data.getGraphId();
		GraphInfo graphInfo = graphInfoRepos.findById(graphId).orElse(null);
		if(graphInfo == null)
			return new Response(Constant.ERROR_CODE,"指定ID的图表不存在",data);
		// 获取图表的配置
		EchartOptions option = graphInfo.getOption();
		if(data.getSeries() != null){
			option.setSeries(data.getSeries());
		}
		EchartOptionUtil optionUtil = new EchartOptionUtil(option);
		
		// 查询项
		List<QueryUnit> queryUnits = optionUtil.getQueryUnits();
		
		// 存放查询数据的结果集
		List<List<Object>> source = new ArrayList<>();
		// 存放数据维度名称的数组
		List<String> dismissions = new ArrayList<>();
		
		// 查询结果处理器
		MatrixResultHandler matrixResultHandler = new MatrixResultHandler();
		for (QueryUnit queryUnit : queryUnits) {
			
			LocalDataAdapter dataAdapter = new TempTableAdapter(context);
			// 查询结果各个数据列的名称
			matrixResultHandler.setNames(queryUnit.getNames());
			
			// 查询
			List<List<Object>> query = dataAdapter.query(graphInfo.getReport().getColumns(), 
					getSqlBuilder(data, queryUnit.getCategory(), queryUnit.getItems()),
					matrixResultHandler);
			// 连接查询结果和维度名称
			List<List<Object>> tmp = query.size() < source.size() ? source : query;
			List<List<Object>> temp = query.size() < source.size() ? query : source;
			
			if(tmp == source){
				dismissions.addAll(queryUnit.getNames());
			}else{
				dismissions.addAll(0, queryUnit.getNames());
			}
			
			Iterator<List<Object>> iter1 = tmp.iterator();
			Iterator<List<Object>> iter2 = temp.iterator();
			while(iter1.hasNext() && iter2.hasNext()){
				iter1.next().addAll(iter2.next());
			}
			source = tmp;
		}
		DataSet dataSet = new DataSet();
		dataSet.setDimensions(dismissions);
		dataSet.setSource(source);
		return new Response(Constant.SUCCESS_CODE,"查询成功",dataSet);
	}

}
