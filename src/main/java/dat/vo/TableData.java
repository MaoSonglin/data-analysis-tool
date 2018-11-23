package dat.vo;

public class TableData {

	private String id;
	
	private Integer limit;
	
	private Integer offset;

	public TableData(String id, Integer limit, Integer offset) {
		super();
		this.id = id;
		this.limit = limit;
		this.offset = offset;
	}
	
	public TableData(String id) {
		this(id,1000,0);
	}

	public TableData() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	
}
