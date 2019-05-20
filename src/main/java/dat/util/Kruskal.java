package dat.util;

import java.util.ArrayList;
import java.util.List;

import dat.model.Graph;
import dat.model.TreeNode;
import dat.model.TreeNode.Tree;
import dat.model.Vertex;
import dat.model.Vertex.Arc;

/**
 * @author MaoSonglin
 * 克鲁斯卡尔算法，计算图的最小生成树
 */
public class Kruskal {
	
	public static void main(String[] args) {
		/*int i = 19, j = 24;
		System.out.printf("i = %d, j = %d\n", i, j);
		i = i ^ j;
		j = i ^ j;
		i = i ^ j;*/
		ArrayList<Edge> edges = new ArrayList<Edge>();
		edges.add(new Edge(1,2,10));
		edges.add(new Edge(3,2,10));
		edges.add(new Edge(4,3,10));
		System.out.println(edges);
		
		for(int i = 0; i < edges.size(); i++){
			Edge ei = edges.get(i);
			for(int j = 0; j < edges.size(); j++){
				if(i != j){
					Edge ej = edges.get(j);
					if(ei.end == ej.end){
						ej.start = ej.start ^ ej.end;
						ej.end = ej.start ^ ej.end;
						ej.start = ej.start ^ ej.end;
					}
				}
			}
		}
		System.out.println(edges);
		
		Graph<Integer> graph = new Graph<>();
		graph.addVertex(1);
		graph.addVertex(2);
		graph.addVertex(3);
		graph.addVertex(4);
		graph.addArc(1, 2, 1);
		graph.addArc(1, 3, 3);
		graph.addArc(1, 4, 2);
		graph.addArc(2, 3, 1);
		graph.addArc(3, 4, 2);
		graph.addArc(2, 4, 4);
		Kruskal kruskal = new Kruskal();
		Tree<Integer> tree = kruskal.minSpanningTree(graph);
		System.out.println(tree);
	}
	/**
	 * 在图graph上构建最小生成树
	 * @param graph
	 * @return
	 */
	public <T> Tree<T> minSpanningTree(Graph<T> graph){
		List<Integer> spans = new ArrayList<>();
		for(int i = 0; i < graph.count(); i++){
			spans.add(i);
		}
		List<Edge> edges = new ArrayList<>();
		int start = 0;
		for (Vertex<T> vertex : graph.getVertexs()) {
			for (Arc arc : vertex.getArcs()) {
				Edge edge = new Edge();
				edge.start = start;
				edge.end = arc.getAdj();//arc.getAdj();
				edge.weight = arc.getWeight();
				edges.add(edge);
			}
			start++;
		}
		edges.sort((arc1,arc2) -> arc1.weight - arc2.weight);
		
		List<Edge> list = new ArrayList<>();
		
		for (Edge edge : edges) {
			Integer s1 = spans.get(edge.start);
			Integer s2 = spans.get(edge.end);
			if(s1 !=  s2){
				list.add(edge);
				for(int j = 0; j < spans.size(); j++){
					if(spans.get(j) == s2){
						spans.set(j, s1);
					}
				}
			}
		}
		
		if(list.isEmpty()){
			return new Tree<>(null);
		}else{
			return new Tree<>(build(graph, list, 0));
		}
	}

	
	private <T> TreeNode<T> build(Graph<T> graph, List<Edge> edges, int index){
		TreeNode<T> root = null;
		
		for(int i = 0; i < edges.size(); i++){
			Edge ei = edges.get(i);
			for(int j = 0; j < edges.size(); j++){
				if(i != j){
					Edge ej = edges.get(j);
					if(ei.end == ej.end){
						ej.start = ej.start ^ ej.end;
						ej.end = ej.start ^ ej.end;
						ej.start = ej.start ^ ej.end;
					}
				}
			}
		}
		
		Edge edge = edges.get(index);
		TreeNode<T> n1 = new TreeNode<>(graph.getVertex(edge.start).getData());
		for (Edge e : edges) {
			if(e.start == index){
				TreeNode<T> n2 = new TreeNode<>(graph.getVertex(edge.end).getData());
				n1.addChild(n2);
				tmp(graph, edges, edge, n2);
			}
		}
		
		root = n1;
		
		return root;
	}
	private <T> void tmp(Graph<T> graph, List<Edge> edges, Edge edge, TreeNode<T> n2) {
		for (Edge tmpEdge : edges) {
			if(tmpEdge.start == edge.end){
				TreeNode<T> treeNode = new TreeNode<>(graph.getVertex(tmpEdge.end).getData());
				n2.addChild(treeNode);
				tmp(graph, edges, tmpEdge, treeNode);
			}
		}
	}
	
	static class Edge{
		int start;
		int end;
		int weight;
		public Edge() {
		}
		
		public Edge(int start, int end, int weight) {
			super();
			this.start = start;
			this.end = end;
			this.weight = weight;
		}

		@Override
		public String toString() {
			return String.format("[%d, %d]", start, end);
		}
	}
}
