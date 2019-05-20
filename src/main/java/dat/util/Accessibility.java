package dat.util;

import java.util.ArrayList;
import java.util.List;

import dat.model.Graph;
import dat.model.Vertex;
import dat.model.Vertex.Arc;

/**
 * 可达性预测类型
 * 用来预测某张图中两个顶点之间是否连通的
 * @author MaoSonglin
 *
 */

public class Accessibility {

	
	private Graph<String> graph;
	
	/**
	 * 预测订单vertex1和顶点vertex2之间是否是连通的
	 * @param vertex1
	 * @param vertex2
	 * @return
	 * @throws NoSuchVertexException 顶点没找到
	 */
	public boolean prediction(String vertex1,String vertex2) throws NoSuchVertexException {
		if(vertex1 == null || vertex2 == null)
			throw new IllegalArgumentException("计算路径的节点不能为NULL");
		if(vertex1.equals(vertex2))return true;
		int vIndex1 = graph.indexOf(vertex1);
		if(vIndex1 == -1) {
			throw new NoSuchVertexException(vertex1);
		}
		int vIndex2 = graph.indexOf(vertex2);
		if(vIndex2 == -1)
			throw new NoSuchVertexException(vertex2);
		List<Boolean> flag = new ArrayList<>(graph.count());
		for (int i = 0; i < graph.count(); i++) {
			flag.add(Boolean.FALSE);
		}
		flag.set(vIndex1, Boolean.TRUE);
		Vertex<String> vertex = graph.getVertexs().get(vIndex1);
		return test(vertex, flag, vIndex2) ? true:flag.get(vIndex2);
	}

	private boolean test(Vertex<String> vertex, List<Boolean> flag, int vIndex2) {
		List<Arc> arcs = vertex.getArcs();
		for (Arc arc : arcs) {
			int adj = arc.getAdj();
			if(adj == vIndex2) { // 如果是弧尾
				flag.set(adj, Boolean.TRUE);
				return true;
			}
			if(!flag.get(adj)) { // 如果该顶点没有访问
				flag.set(adj,Boolean.TRUE);
				if(test(graph.getVertexs().get(adj),flag,vIndex2)) return true;
			}
		}
		return false;
	}

	public Accessibility(Graph<String> graph) {
		super();
		this.graph = graph;
	}

	public Accessibility() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Graph<String> getGraph() {
		return graph;
	}

	public void setGraph(Graph<String> graph) {
		this.graph = graph;
	}
}
