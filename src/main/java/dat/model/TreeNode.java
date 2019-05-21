package dat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode<T> implements Serializable {

	public TreeNode(T data, TreeNode<T> parent, List<TreeNode<T>> items) {
		super();
		this.data = data;
		this.parent = parent;
		this.items = items;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}

	public List<TreeNode<T>> getItems() {
		return items;
	}

	public void setItems(List<TreeNode<T>> items) {
		this.items = items;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5340285534412103410L;
	
	public TreeNode() {
	}
	
	public TreeNode(T data){
		this.data = data;
	}
	
	public TreeNode(T data, TreeNode<T> parent){
		this.data = data;
		this.parent = parent;
		if(this.parent != null && !this.parent.items.contains(this))
			this.parent.addChild(this);
	}
	
	private T data;

	private TreeNode<T> parent;
	
	private List<TreeNode<T>> items = new ArrayList<>();
	
	public void addChild(TreeNode<T> node){
		this.items.add(node);
	}
	
	public static class Tree<T> implements Serializable{
		private static final long serialVersionUID = 5015848806014397684L;
		private TreeNode<T> root;
		public TreeNode<T> getRoot() {
			return root;
		}
		public void setRoot(TreeNode<T> root) {
			this.root = root;
		}
		public Tree(TreeNode<T> root) {
			super();
			this.root = root;
		}
		public Tree() {
			super();
		}
		
		
		public boolean isEmpty(){
			return root == null;
		}
	}
	/**
	 * 花匠，对树进行操作
	 * @author MaoSonglin
	 *
	 */
	public static class Gardener{
		/**
		 * 剪枝，将树tree中没有用的节点减掉，比如tree中的某个叶子节点，这个叶子节点即不是vertexs中的某个表，也
		 * 不连接vertexs中的某两个节点
		 * @param tree	待剪枝的树
		 * @param vertexs	待比较的节点数据
		 */
		public <T> void pruning(Tree<T> tree, List<T> vertexs){
			boolean trim = trim(tree.getRoot(), vertexs);
			if(trim){
				tree.setRoot(null);
			}
		}

		private <T> boolean trim(TreeNode<T> root, List<T> vertexs) {
			boolean f = true; // 标记root是否应该移除
			T data2 = root.getData();
			if(vertexs.contains(data2)){
				f = false;	// 节点root不移除
			}
			// 判断items2的子节点是否应该移除
			List<TreeNode<T>> items2 = root.getItems();
			for (Iterator<TreeNode<T>> iterator2 = items2.iterator(); iterator2.hasNext();) {
				TreeNode<T> treeNode = iterator2.next();
				// 是否应该移除treeNode
				if(trim(treeNode, vertexs)){
					// 移除treeNode
					iterator2.remove();
				}else{
					// 不移除treeNode, 那么也不应该移除root
					f = false;
				}
			}
			return f;
		}

		
	}
}
