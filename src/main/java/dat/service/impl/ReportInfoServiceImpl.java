package dat.service.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.data.LocalDataAdapter;
import dat.data.LocalDataAdapter.RowHandler;
import dat.data.LocalDataAdapter.SqlBuilder;
import dat.domain.GraphInfo;
import dat.domain.Menu;
import dat.domain.ReportInfo;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.repos.GraphInfoRepository;
import dat.repos.MenuRepository;
import dat.repos.ReportInfoRepository;
import dat.repos.VirtualColumnRepository;
import dat.service.ReportInfoService;
import dat.service.impl.VirtualTableServiceImpl.ResultSetHandler;
import dat.util.Constant;
import dat.util.DrillUtils;
import dat.vo.EchartOptions;
import dat.vo.EchartOptions.Axis;
import dat.vo.EchartOptions.DataSet;
import dat.vo.GraphDrillData;
import dat.vo.ReportPagingBean;
import dat.vo.Response;
import dat.vo.TreeNode;

@Service
public class ReportInfoServiceImpl implements ReportInfoService {

	private static Logger logger = Logger.getLogger(ReportInfoServiceImpl.class);
	@Autowired
	ReportInfoRepository reportInfoRepos;
	
	@Autowired
	GraphInfoRepository graphInfoRepos;
	
	@Autowired
	ApplicationContext  context;
	
	public Response searchByPageInfo(ReportPagingBean pageInfo) {
		Specification<ReportInfo> spec = (root,query,cb)->{
			List<Predicate> predicates = new ArrayList<>();
			String keyword = pageInfo.getKeyword();
			if(!StringUtils.isEmpty(keyword)) {
				Predicate like = cb.like(root.get("name"), "%"+keyword+"%");
				predicates.add(like);
			}
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			predicates.add(notEqual);
			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};
		Page<ReportInfo> page = reportInfoRepos.findAll(spec,PageRequest.of(pageInfo.getCurPage()-1, pageInfo.getPageSize(),Sort.by(Direction.DESC, "id")));
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功");
		response.setData(page);
		response.put("searchInfo", pageInfo);
		return response;
	}
	
	@Transactional
	public Response save(ReportInfo report) {
		Specification<ReportInfo> spec = (root,query,cb)->{
			Predicate p1 = cb.notEqual(root.get("id"), report.getId());
			Predicate p2 = cb.equal(root.get("name"), report.getName());
			Predicate p3 = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return cb.and(p1,p2,p3);
		};
		List<ReportInfo> list = reportInfoRepos.findAll(spec);
		if(list.isEmpty()){
			ReportInfo save = reportInfoRepos.save(report);
			if(logger.isDebugEnabled())
				logger.debug(save);
			return new Response(Constant.SUCCESS_CODE,"保存成功",save);
		}else{
			return new Response(Constant.ERROR_CODE,"报表名称“"+report.getName()+"”已经存在了",report);
		}
	}

	@Override
	public ReportInfo getById(String id) {
		return reportInfoRepos.findById(id).orElse(null);
	}

	@Override
	public List<GraphInfo> getGraphs(String id) {
		List<GraphInfo> list = graphInfoRepos.findAll((root,query,cb)->{
			Predicate equal = cb.equal(root.get("report").get("id"), id);
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return cb.and(equal,notEqual);
		});
		return list;
	}
	
	
	
	@Override
	@Transactional
	public Response unpublish(String reportid) {
		ReportInfo reportInfo = reportInfoRepos.findById(reportid).orElse(null);
		if(reportInfo == null){
			return new Response(Constant.ERROR_CODE,"报表不存在",reportid);
		}
		// 获取发布目录
		Menu publish = reportInfo.getPublish();
		reportInfo.setPublish(null);
		ReportInfo save = reportInfoRepos.save(reportInfo);
		// 删除之前发布的目录
		context.getBean(MenuRepository.class).delete(publish);
		return new Response(Constant.SUCCESS_CODE,"操作成功",save);
	}

	@Override
	@Transactional
	public Response delete(String id) {
		ReportInfo reportInfo = reportInfoRepos.findById(id).orElse(null);
		if(reportInfo == null){
			return new Response(Constant.ERROR_CODE,"ID不存在");
		}
		Menu menu = reportInfo.getPublish();
		// 删除发布的目录
		if(menu != null)
			context.getBean(MenuRepository.class).delete(menu);
		List<GraphInfo> graphs = reportInfo.getGraphs();
		// 删除图表信息
		context.getBean(GraphInfoRepository.class).deleteAll(graphs);
		reportInfoRepos.delete(reportInfo);
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}

	@Override
	public Response getTableTree(String id) {
		ReportInfo reportInfo = reportInfoRepos.findById(id).orElse(null);
		if(reportInfo== null){
			return new Response(Constant.ERROR_CODE,"ID不存在");
		}
		List<VirtualColumn> excludes = reportInfo.getColumns();
		WorkPackage pkg = reportInfo.getPkg();
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
		return new Response(Constant.SUCCESS_CODE,"查询成功",root);
	}

	@Override
	public Response getData(GraphDrillData data) {
		// 分类id
		String columnId = data.getColumnId();
		String graphId = data.getGraphId();
		GraphInfo graphInfo = graphInfoRepos.findById(graphId).orElse(null);
		if(graphInfo == null)
			return new Response(Constant.ERROR_CODE,"指定ID的图表不存在",data);
		EchartOptions option = graphInfo.getOption();
		if(columnId == null){
			columnId = option.getxAxis().getColumnId();
		}
		VirtualColumn virtualColumn = context.getBean(VirtualColumnRepository.class).findById(columnId).orElse(null);
		if(virtualColumn == null){
			return new Response(Constant.ERROR_CODE,"分类id不存在",columnId);
		}
		
		List<VirtualColumn> columns = graphInfo.getReport().getColumns();
		columns.remove(virtualColumn);
//		List<Serie> series = option.getSeries();
//		columns.removeIf(elem->{
//			for (Serie serie : series) {
//				if(serie.getColumnId().equalsIgnoreCase(elem.getId())) return false;
//			}
//			return true;
//		});
		if(logger.isDebugEnabled()){
			logger.debug("分类字段："+virtualColumn.getChinese());
			for (VirtualColumn v : columns) {
				logger.debug(v.getChinese());
			}
		}
		try {
			columns.add(0, virtualColumn);
			LocalDataAdapter localDataAdapter = new LocalDataAdapter(context);
			SqlBuilder sqlInfo = DrillUtils.getSqlBuilder(columns, data.getWheres());//DrillUtils.getSqlInfo(data, virtualColumn, columns);
			Axis axis = option.getxAxis();
			String name = null;
			if(axis != null){
				name = axis.getName();
			}
			if(name == null){
				name = virtualColumn.getChinese() != null ? virtualColumn.getChinese() : virtualColumn.getName();
			}
			MatrixResultHandler matrixResultHandler = new MatrixResultHandler(columns);
			List<List<Object>> map = localDataAdapter.query(columns,sqlInfo,matrixResultHandler);
			if(logger.isDebugEnabled()){
			}
			/*for(Serie s : series){
				HashMap<String, Object> encode = new HashMap<>();
				encode.put("x", virtualColumn.getName());
				encode.put("itemName", virtualColumn.getName());
				encode.put("label", virtualColumn.getName());
				
				columns.forEach(elem->{
					if(elem.getId().equals(s.getColumnId())){
						encode.put("y", elem.getName());
						encode.put("value", elem.getName());
					}
				});
				
				s.setEncode(encode);
			}*/
			DataSet dataset = new DataSet(map);
			dataset.setDimensions(matrixResultHandler.getNames());
//			option.setDataset(dataset);
//			int size = map.size();
//			
//			if(size > 8 ){
//				DataZoom dz = option.getDataZoom();
//				if(dz != null){
//					dz.setShow(true);
//					dz.setStart(10);
//					dz.setEnd(8f / size * 100f + 10);
//				}
//			}
			Response response = new Response(Constant.SUCCESS_CODE,"查询成功",dataset);
//			response.put("graph",graphInfo);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,e.getMessage(),data);
		}
	}
	
	
	
	@SuppressWarnings("unused")
	private List<List<Object>> format(List<VirtualColumn> columns,
			Map<String, List<Object>> map) {
		List<List<Object>> list = new ArrayList<>(map.size());
		columns.forEach(column->{
			String name = column.getName();
			List<Object> list2 = map.get(name);
			if(list2 == null)
				return ;
			String chinese = column.getChinese();
			list2.add(0, chinese==null?name:chinese);
			list.add(list2);
		});
		return list;
	}

	@Override
	@Transactional
	public Response pulish(ReportInfo report) {
		String id = report.getId();
		ReportInfo reportInfo = reportInfoRepos.findById(id).orElse(null);
		if(reportInfo == null)
			return new Response(Constant.ERROR_CODE,"报表ID不存在");
		Menu menu = report.getPublish();
		Menu save = context.getBean(MenuRepository.class).save(menu);
		reportInfo.setPublish(save);
		reportInfoRepos.save(reportInfo);
		return new Response(Constant.SUCCESS_CODE,"保存成功",menu);
	}
	
}

class KeyValuesResulstHandler implements ResultSetHandler<Map<String,List<Object>>> {
	
	private Map<String,String> nameMap;
	
	
	
	public KeyValuesResulstHandler(Map<String, String> nameMap) {
		super();
		this.nameMap = nameMap;
	}

	public KeyValuesResulstHandler(List<VirtualColumn> columns) {
		super();
		nameMap = new HashMap<>();
		for (VirtualColumn virtualColumn : columns) {
			nameMap.put(virtualColumn.getName(), virtualColumn.getChinese());
		}
	}

	@Override
	public Map<String, List<Object>> doResultSet(ResultSet resultSet)
			throws SQLException {
		Map<String,List<Object>> map = new HashMap<>();
		Set<Entry<String,String>> entrySet = nameMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			map.put(entry.getValue(),new ArrayList<>());
		}
		while(resultSet.next()){
			for(Entry<String,String> entry : entrySet){
				String key = entry.getKey();
				Object object = resultSet.getObject(key);
				map.get(entry.getValue()).add(object);
			}
		}
		return map;
	}
	
}


class ListKeyValueResultHandler implements RowHandler<Map<String,Object>>{

	@Override
	public Map<String, Object> handRow(int rowNum, ResultSet rs)
			throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String,Object> map = new HashMap<>();
		for(int i = 1; i < columnCount+1; i++){
			String key = metaData.getColumnLabel(i);
			if(i == 1){
				String string = rs.getString(i);
				map.put(key, string);
			}else{
				Object object = rs.getObject(i);
				map.put(key, object);
			}
		}
		return map;
	}
	
}

class MatrixResultHandler implements RowHandler<List<Object>>{

	public MatrixResultHandler() {
		super();
	}
	
	public MatrixResultHandler(List<VirtualColumn> columns){
		names = new ArrayList<>();
		for (VirtualColumn virtualColumn : columns) {
			names.add(virtualColumn.getName());
		}
	}

	List<String> names ;
	
	@Override
	public List<Object> handRow(int rowNum, ResultSet rs) throws SQLException {
		List<Object> list = new ArrayList<>();
		for (String key : names) {
			Object object = rs.getObject(key);
			list.add(object);
		}
		return list;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}
	
}



