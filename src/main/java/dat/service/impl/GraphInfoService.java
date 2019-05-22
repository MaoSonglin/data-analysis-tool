package dat.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.data.JoinUtil;
import dat.data.Judgment;
import dat.domain.GraphInfo;
import dat.domain.Reference;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.model.Graph;
import dat.model.TreeNode;
import dat.model.TreeNode.Gardener;
import dat.model.TreeNode.Tree;
import dat.repos.GraphInfoRepository;
import dat.repos.ReferenceRepository;
import dat.service.VirtualTableService;
import dat.util.Connectivity;
import dat.util.Kruskal;
import dat.vo.SelectVo;

@Service("graphInfoServiceImpl")
public class GraphInfoService extends GraphInfoServiceImpl {

	@Autowired
	private ApplicationContext context;
	/**
	 * 获取指定图表需要的数据
	 * @param id
	 * @return
	 */
	public DataTable<DataMap> getGraphData(String id){
		// 查找图表信息
		Optional<GraphInfo> optional = context.getBean(GraphInfoRepository.class).findById(id);
		GraphInfo graphInfo = optional.get();
		// 图中需要的显示的内容字段
		List<VirtualColumn> columns = graphInfo.getColumns();
		if(columns.isEmpty()){
			columns = graphInfo.getReport().getColumns();
		}
		// 需要访问的数据字段出自那些数据表
		List<VirtualTable> tables = columns.stream().map(column -> column.getTable()).distinct().collect(Collectors.toList());
		
		// 构建一张图，将tables中的表都放到图中
		Graph<VirtualTable> graph = new Graph<>();
		tables.forEach(table -> graph.addVertex(table));
		
		// 查询出系统中的所有关联关系，构建图中的弧
		List<Reference> list = context.getBean(ReferenceRepository.class).findAll();
		for (Reference reference : list) {
			VirtualTable t1 = reference.getPrimaryTable();
			graph.addVertex(t1);
			VirtualTable t2 = reference.getReferencedTable();
			graph.addVertex(t2);
			if(!t1.equals(t2))
			graph.addArc(t1, t2);
		}
		
		DataTable<DataMap> data = null;
		// 计算graph的联通子图
		Connectivity<VirtualTable> cty = new Connectivity<>(graph);
		List<Graph<VirtualTable>> subgraph = cty.getSubgraph();
		
		// 结算每个联通子图的最小生成树的算法
		Kruskal kruskal = new Kruskal();
		// 花匠
		Gardener gardener = new Gardener();
		for (Iterator<Graph<VirtualTable>> iterator = subgraph.iterator(); iterator.hasNext();) {
			Graph<VirtualTable> graph2 = iterator.next();
			Tree<VirtualTable> tree = kruskal.minSpanningTree(graph2);
			gardener.pruning(tree, tables);
			if(tree.isEmpty())
				continue;
			DataTable<DataMap> tmp = build(tree.getRoot());
			if(data == null)
				data = tmp;
			else{
				data = data.join(tmp, "rownum", "rownum");
			}
		}
		StringBuffer sBuffer = new StringBuffer();
		for (VirtualColumn column : columns) {
			sBuffer.append('"').append(column.getName()).append('"').append(" , ");
		}
		sBuffer.deleteCharAt(sBuffer.length()-2);
		DataTable<DataMap> select = data.select(sBuffer.toString(), "", null, null);
		return select;
	}
	
	DataTable<DataMap> build(TreeNode<VirtualTable> treeNode){
		ReferenceRepository repos = context.getBean(ReferenceRepository.class);
		VirtualTableService service = context.getBean(VirtualTableService.class);
		// 虚拟数据表
		VirtualTable data = treeNode.getData();
		// 获取虚拟数据表中的数据
		DataTable<DataMap> dataTable = service.getTableBody(data.getId());
		List<TreeNode<VirtualTable>> items = treeNode.getItems();
		for (TreeNode<VirtualTable> node : items) {
			VirtualTable virtualTable = node.getData();
			// 查找两个表之间的关联关系
			List<Reference> collect = repos.findAll((root, query, cb) -> {
				Predicate equal = cb.equal(root.get("primaryTable").get("id"), data.getId());
				Predicate equal2 = cb.equal(root.get("referencedTable").get("id"), virtualTable.getId());
				Predicate and = cb.and(equal, equal2);
				Predicate equal3 = cb.equal(root.get("primaryTable").get("id"), virtualTable.getId());
				Predicate equal4 = cb.equal(root.get("referencedTable").get("id"), data.getId());
				Predicate and2 = cb.and(equal3, equal4);
				
				return cb.or(and, and2);
			}).stream().distinct().collect(Collectors.toList());
			if(collect.isEmpty()){
				throw new IllegalStateException(String.format("虚拟表'%s'与虚拟表'%s'之间没有任何关联关系，却放到了同一个联通分量中", data.getName(), node.getData().getName()));
			}
			Reference reference = collect.iterator().next();
			
			DataTable<DataMap> build = build(node);
			JoinUtil joinUtil = new JoinUtil();
			joinUtil.setLeftColumnName(reference.getReferencedColumn().getName());
			joinUtil.setRightColumnName(reference.getPrimaryTable().getName());
			joinUtil.setLeftTable(dataTable);
			joinUtil.setRightTable(build);
			String joinClassName = reference.getJoinClassName();
			try {
				// 获取连接类
				Judgment instance = (Judgment) Class.forName(joinClassName).newInstance();
				// 连接两个表
				dataTable = joinUtil.doJoint(instance);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return dataTable;
	}


	public DataTable<DataMap> getData(SelectVo selectVo) {
		DataTable<DataMap> graphData = getGraphData(selectVo.getGraphId());
		DataTable<DataMap> select = graphData.select(selectVo.getSelect(), selectVo.getWhere(), selectVo.getOrderBy(), selectVo.getGroupBy());
		System.gc();
		return select;
	}
	
}
