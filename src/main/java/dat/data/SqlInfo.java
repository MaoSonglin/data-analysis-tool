package dat.data;

import java.util.ArrayList;
import java.util.List;

public class SqlInfo {
	
	private String sql;
	
	private List<Object> params;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getParams() {
		if(params==null)
			params = new ArrayList<>();
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}
	
	
}
