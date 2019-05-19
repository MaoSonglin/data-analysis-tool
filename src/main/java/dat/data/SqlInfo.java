package dat.data;

import java.util.ArrayList;
import java.util.List;

import dat.domain.Source;

public class SqlInfo {
	
	private String sql;
	
	private List<Object> params;

	private Source source;
	
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

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
	
	
}
