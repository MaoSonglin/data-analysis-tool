package dat.test;

public class BinaryTreeNode {
	
	public static void main(String[] args) {
//		fun(b5,22);
		
	}
	
	public BinaryTreeNode(int value) {
		super();
		this.value = value;
	}

	public BinaryTreeNode(int value, BinaryTreeNode left, BinaryTreeNode right) {
		super();
		this.value = value;
		this.left = left;
		this.right = right;
	}

	private int value;
	
	private BinaryTreeNode left;
	
	private BinaryTreeNode right;
	
	public static int fun(BinaryTreeNode node,int sum){
		if(node.left != null){
			int i = node.value + fun(node.left,sum-node.value);
			if(i == sum)
				System.out.println(node.value);
			return i;
		}
		if(node.right != null){
			int i = node.value + fun(node.right ,sum-node.value);
			if(i == sum)
				System.out.println(node.value);
			return i;
		}
		return node.value;
	}
}
