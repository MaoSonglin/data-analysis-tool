package dat.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GraphData2 implements Serializable {

	private static final long serialVersionUID = -4762333908873723334L;

	private List<String> labels ;
	
	private List<? extends Object> values;
	
	private String legends ;
	
	{
		labels = new ArrayList<>();
		values = new ArrayList<>();
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<? extends Object> getValues() {
		return values;
	}

	public void setValues(List<? extends Object> values) {
		this.values = values;
	}

	public String getLegends() {
		return legends;
	}

	public void setLegends(String legends) {
		this.legends = legends;
	}
	
	
	
}




















































































