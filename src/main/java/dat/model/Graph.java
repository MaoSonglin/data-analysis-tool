package dat.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dat.model.Vertex.Arc;


/**
 * @author MaoSonglin
 * 图的数据结构，使用十字链表表示法
 */
public class Graph<T> {
	 
	/**
	 * 顶点表
	 */
	private List<Vertex<T>> vertexs = new ArrayList<>();
	
	/**
	 * 或者图中顶点的个数
	 * @return
	 */
	public int count() {
		return vertexs.size();
	}
	/**
	 * 向图中添加定点，定点的数据域存放的值为value
	 * @param value
	 */
	public void addVertex(T value) {
		if(indexOf(value) > -1) return;
		Vertex<T> vertex = new Vertex<>();
		vertex.setData(value);
		vertexs.add(vertex);
	}
	
	/**
	 * 定位value在的定点表中的位置
	 * @param value
	 * @return 如果value存在于某个定点的数据域中，则返回这个定点在定点表中的下标，否则返回-1
	 */
	public int indexOf(T value) {
		for (int i = 0; i < vertexs.size(); i++) {
			Vertex<T> vertex = vertexs.get(i);
			T data = vertex.getData();
			if(data.equals(value)) return i;
		}
		return -1;
	}
	/**
	 * 返回指定下标的两个顶点之间的弧长
	 * @param v		弧尾顶点在顶点表中的下标
	 * @param v2	弧头顶点在定点表中的下标
	 * @return	如果两个顶点之间存在至少一条弧，则返回该弧的长度，否则返回10000表示这两个顶点之间不可达
	 */
	public int getWeight(int v, int v2) {
		Vertex<T> vertex = vertexs.get(v);
		List<Arc> arcs = vertex.getArcs();
		for (Arc arc : arcs) {
			if(arc.getAdj() == v2){
				return arc.getWeight();
			}
		}
		// 不可达
		return 10000;
	}
	
	/**
	 * first table that this table adjacent which index is vIndex
	 * @param vIndex 
	 * @return
	 */
	public int firstAdjacency(int vIndex) {
		Vertex<T> vertex = vertexs.get(vIndex);
		List<Arc> arcs = vertex.getArcs();
		if(arcs.size()>0) return arcs.get(0).getAdj();
		return -1;
	}
	
	/**
	 * 第v个节点相对第v2个节点的下一个邻接点
	 * @param v	节点下标
	 * @param v2	节点下标
	 * @return
	 */
	public int nextAdjacency(int v,int v2) {
		Vertex<T> vertex = vertexs.get(v);
		List<Arc> arcs = vertex.getArcs();
		Iterator<Arc> iter = arcs.iterator();
		while(iter.hasNext()) {
			Arc next = iter.next();
			if(next.getAdj() == v2) {
				if(iter.hasNext()) return iter.next().getAdj();
			}
		}
		return -1;
	}
	
	/**
	 * 指定值得顶点的第一个邻接点的下标
	 * @param value
	 * @return
	 */
	public int firstAdjacency(T value) {
		int indexOf = indexOf(value);
		return firstAdjacency(indexOf);
	}
	/**
	 * 向图中添加一条弧
	 * @param v1	弧头顶点的数据域值
	 * @param v2	弧尾顶点的数据域值
	 */
	public void addArc(T v1,T v2) {
		addArc(v1,v2,1);
	}
	
	/**
	 * 获取指定下标的顶点
	 * @param index
	 * @return
	 */
	public Vertex<T> getVertex(int index){
		return this.vertexs.get(index);
	}
	
	/**
	 * 向图中插入一条弧
	 * @param v1	弧头顶点值
	 * @param v2	弧尾顶点值
	 * @param weight	弧长
	 */
	public void addArc(T v1,T v2,int weight) {
		int index = indexOf(v1);
		int index2 = indexOf(v2);
		if(index == index2)
			throw new IllegalArgumentException("弧头和弧尾不能相同.....");
		if(index > -1 && index2 > -1) {
			Arc arc = new Arc();
			arc.setAdj(index2);
			arc.setWeight(weight);
			List<Arc> arcs = vertexs.get(index).getArcs();
			if(!arcs.contains(arc))
				arcs.add(arc);
			
			Arc arc2 = new Arc();
			arc2.setAdj(index);
			arc2.setWeight(weight);
			List<Arc> arcs2 = vertexs.get(index2).getArcs();
			if(!arcs2.contains(arc2))
				arcs2.add(arc2);
		}else {
			throw new IllegalArgumentException("不存在的点");
		}
		
	}
	
	public void removeArc(T v1, T v2){
		int index = indexOf(v1);
		int index2 = indexOf(v2);
		if(index > -1 && index2 > -1){
			Vertex<T> vertex = getVertex(index);
			List<Arc> arcs = vertex.getArcs();
			for (Iterator<Arc> iterator = arcs.iterator(); iterator.hasNext();) {
				Arc arc = iterator.next();
				if(arc.getAdj() == index2){
					iterator.remove();
					break;
				}
			}
			vertex = getVertex(index2);
			arcs = vertex.getArcs();
			for (Iterator<Arc> iterator = arcs.iterator(); iterator.hasNext();) {
				Arc arc =  iterator.next();
				if(arc.getAdj() == index){
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public void removeVertex(T v){
		int i = indexOf(v);
		if(i > -1){
			for (Vertex<T> vertex : vertexs) {
				// 遍历所有节点的所有邻接点
				List<Arc> arcs = vertex.getArcs();
				for (Iterator<Arc> iterator = arcs.iterator(); iterator.hasNext();) {
					Arc arc = iterator.next();
					// 如果某个节点的邻接点是要移除的节点v,则将这条弧移除
					if(arc.getAdj() == i)
					iterator.remove();
				}
			}
		}
	}
	public List<Vertex<T>> getVertexs() {
		return vertexs;
	}
	
}
