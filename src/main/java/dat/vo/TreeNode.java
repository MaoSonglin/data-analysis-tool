package dat.vo;

import java.io.Serializable;
import java.util.List;

public class TreeNode implements Serializable{

	private static final long serialVersionUID = -3054170061094246250L;

	private String Id;
	
	private String text;
	
	private String type;
	
	private List<TreeNode> nodes;

	public TreeNode() {
		super();
	}

	public TreeNode(String nodeId, String text, List<TreeNode> nodes) {
		super();
		this.Id = nodeId;
		this.text = text;
		this.nodes = nodes;
	}

	public TreeNode(String nodeId, String text) {
		super();
		this.Id = nodeId;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<TreeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}



}
