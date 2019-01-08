package dat.vo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;


public class GraphDrillData {
	
	private String graphId;
	
	private String columnId;
	
	private String itemName;
	
	private List<String> seriesName;
	
	private List<String> wheres;

	public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public List<String> getWheres() {
		if(wheres == null)
			wheres = new ArrayList<>();
		return wheres;
	}

	public void setWheres(List<String> wheres) {
		this.wheres = wheres;
	}

	@Override
	public String toString() {
		String jsonString = JSON.toJSONString(this);
		return jsonString;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public List<String> getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(List<String> seriesName) {
		this.seriesName = seriesName;
	}

	
}
