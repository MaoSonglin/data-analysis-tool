package dat.vo;

public class GraphDataQueryBean extends LayuiTableParam {

	private static final long serialVersionUID = -3395351597061082259L;

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "GraphDataQueryBean [id=" + id + ", getCode()=" + getCode()
				+ ", getMsg()=" + getMsg() + ", getCount()=" + getCount()
				+ ", getData()=" + getData() + ", getPage()=" + getPage()
				+ ", getLimit()=" + getLimit() + ", toString()="
				+ super.toString() + "]";
	}
	
}
