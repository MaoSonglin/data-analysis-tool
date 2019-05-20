package dat.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MaoSonglin
 * 图的数据结构，使用十字链表表示的图，使用这个类来定义图的定点表结构
 */

public class Vertex<T> {
	
	/**
	 * 定点中存放的数据
	 */
	private T data;
	
	public Vertex() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Vertex(T data) {
		super();
		this.data = data;
	}

	/**
	 * 定点的邻接表
	 */
	private List<Arc> arcs = new ArrayList<>();
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public List<Arc> getArcs() {
		return arcs;
	}

	public void setArcs(List<Arc> arcs) {
		this.arcs = arcs;
	}

	/**
	 * @author MaoSonglin
	 * 表示图的定点的邻接表数据结构
	 */
	public static class Arc{
		
		private int adj;
		
		private int weight = 10000;

		public int getAdj() {
			return adj;
		}

		public void setAdj(int adj) {
			this.adj = adj;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + adj;
			result = prime * result + weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Arc other = (Arc) obj;
			if (adj != other.adj)
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}
		
	}
}
