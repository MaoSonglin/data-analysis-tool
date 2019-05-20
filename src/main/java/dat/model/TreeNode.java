package dat.model;

import java.io.Serializable;
import java.util.ArrayList;
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
			// TODO Auto-generated constructor stub
		}
	}
}
