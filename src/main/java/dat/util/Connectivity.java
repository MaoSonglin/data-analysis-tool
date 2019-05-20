package dat.util;

import java.util.ArrayList;
import java.util.List;

import dat.model.Graph;
import dat.model.Vertex;
import dat.model.Vertex.Arc;

/**
 * 将一个图拆分为多个连通分量
 * @author MaoSonglin
 *
 */
public class Connectivity<T> {
	
	
	
	public Connectivity(Graph<T> graph) {
		super();
		this.graph = graph;
	}

	private Graph<T> graph;
	
	public List<Graph<T>> getSubgraph(){
		// 图中的所有顶点
		List<Vertex<T>> vertexs = graph.getVertexs();
		// 编辑图中的顶点是否已经遍历过的数组
		List<Boolean> set = new ArrayList<>(graph.count());
		// 初始化为都没有遍历
		for(int i = 0; i < graph.count(); i++){
			set.add(Boolean.FALSE);
		}
		// 存放子图的数组
		List<Graph<T>> graphs = new ArrayList<>();
		// 遍历图中的所有顶点
		for (Vertex<T> vertex : vertexs) {
			// 为每一个顶点构建一个子图
			Graph<T> g = new Graph<T>();
			// 当前顶点在图中的位置
			int index = graph.indexOf(vertex.getData());
			// 如果这个顶点还没有遍历过
			if(!set.get(index)){
				// 将这个顶点加入到子图g中
				g.addVertex(vertex.getData());
				// 设置遍历标志
				set.set(index, Boolean.TRUE);
				// 遍历与这个顶点邻接点
				getSubGraph(vertex, g, set);
			}
			if(g.count() > 0) graphs.add(g);
		}
		return graphs;
	}

	protected void getSubGraph(Vertex<T> vertex, Graph<T> g, List<Boolean> set) {
		// 顶点相关的所有弧
		List<Arc> arcs = vertex.getArcs();
		for (Arc arc : arcs) {
			// vertex的邻接点
			int adj = arc.getAdj();
			Vertex<T> v2 = graph.getVertexs().get(adj);
			if(!set.get(adj)){
				// 如果这个邻接点没有遍历，将这个邻接点加入到图g中
				g.addVertex(v2.getData());
				g.addArc(vertex.getData(), v2.getData());
				set.set(adj, Boolean.TRUE);
				// 遍历这个邻接点的邻接点
				getSubGraph(v2,g,set);
			}
		}
	}
	
}
