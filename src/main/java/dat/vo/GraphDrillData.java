package dat.vo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;


public class GraphDrillData {
	
	private String graphId;
	
	private String columnId;
	
	private String itemName;
	
	private String oldName;
	
	private List<String> seriesName;
	
	private List<String> wheres;
	
	private List<Serie> series;
	
	private Boolean tag;

	public Boolean getTag() {
		return tag;
	}

	public void setTag(Boolean tag) {
		this.tag = tag;
	}

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
		return JSON.toJSONString(this);
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
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

	public List<Serie> getSeries() {
		return series;
	}

	public void setSeries(List<Serie> series) {
		this.series = series;
	}
}
