package dat.vo;

import java.io.Serializable;

public class LayuiTableParam implements Serializable {
	
	private static final long serialVersionUID = 1606098335872224931L;

	private Integer code ;
	
	private String msg;
	
	/**
	 * 数据长度
	 */
	private Integer count;
	
	private Object data;
	
	/**
	 * 页码参数
	 */
	private Integer page;
	
	/**
	 * 每页数据量
	 */
	private Integer limit;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "LayuiTableParam [code=" + code + ", msg=" + msg + ", count="
				+ count + ", data=" + data + ", page=" + page + ", limit="
				+ limit + "]";
	}
	
	
}
