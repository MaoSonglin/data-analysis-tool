package dat.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GraphData implements Serializable {

	private static final long serialVersionUID = -4531957087878600442L;

	{
		xDatas = new ArrayList<>();
		yDatas = new ArrayList<>();
	}
	

	private List<String> xDatas;
	
	private List<List<String>> yDatas;

	public List<String> getxDatas() {
		return xDatas;
	}

	public void setxDatas(List<String> xDatas) {
		this.xDatas = xDatas;
	}

	

	public List<List<String>> getyDatas() {
		return yDatas;
	}

	public void setyDatas(List<List<String>> yDatas) {
		this.yDatas = yDatas;
	}
}
