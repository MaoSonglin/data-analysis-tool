package dat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dat.vo.EchartOptions;
import dat.vo.EchartOptions.Axis;
import dat.vo.EchartOptions.Encode;
import dat.vo.Serie;

public class EchartOptionUtil {
	
	protected EchartOptions option;
	
	public EchartOptionUtil(EchartOptions option) {
		super();
		this.option = option;
	}

	public List<QueryUnit> getQueryUnits(){
		Map<String,QueryUnit> map = new HashMap<>();
		List<Serie> series = option.getSeries();
		
		for (Serie serie : series) {
			Encode encode = serie.getEncode();
			if(encode == null){
				throw new IllegalArgumentException("系列'"+serie.getName()+"'没有设置维度解析'encode'");
			}
			String type = serie.getType();
			String itemName = null;
			String value = null;
			switch(type){
			case "pie":
				itemName = encode.getItemName();
				value = encode.getValue();
				break;
			case "line":
			case "bar":
				itemName = getCategoryName(encode);
				value = getValueName(encode);
				break;
			default:
				throw new IllegalArgumentException("不支持的图表类型'"+type+"'");
			}
			if(value == null || itemName == null){
				throw new IllegalArgumentException("未能从系列'"+serie.getName()+"'中解析出分类");
			}
			QueryUnit queryUnit = map.get(itemName);
			if(queryUnit == null){
				queryUnit = new QueryUnit();
				queryUnit.setItems(new ArrayList<>());
				queryUnit.setCategory(itemName);
				map.put(itemName, queryUnit);
			}
			queryUnit.getItems().add(value);
		}
		return new ArrayList<>(map.values());
	}

	public List<QueryUnit> getQueryUnits(String newCategory,String oldCategory){
		List<QueryUnit> queryUnits = getQueryUnits();
		for (QueryUnit queryUnit : queryUnits) {
			if(queryUnit.getCategory().equals(oldCategory)){
				queryUnit.setCategory(newCategory);
			}
		}
		return queryUnits;
	}
	
	/**
	 * @param encode
	 * @return
	 */
	private String getValueName(Encode encode) {
		String value;
		Axis yAxis = option.getyAxis();
		String yType = yAxis.getType();
		yType = (yType == null || yType.isEmpty()) ? "value" : yType;
		value = "value".equalsIgnoreCase(yType) ? encode.getY() : encode.getX();
		return value;
	}

	/**
	 * @param encode
	 * @return
	 */
	private String getCategoryName(Encode encode) {
		String itemName;
		Axis xAxis = option.getxAxis();
		String xType = xAxis.getType();
		xType = (xType == null || xType.isEmpty()) ? "category" : xType;
		itemName = "category".equalsIgnoreCase(xType) ? encode.getX():encode.getY();
		return itemName;
	}

	public static class QueryUnit {
		
		//分类标准
		private String category;
		
		// 查询项
		private List<String> items;

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public List<String> getItems() {
			return items;
		}

		public void setItems(List<String> items) {
			this.items = items;
		}
		
		public List<String> getNames(){
			ArrayList<String> arrayList = new ArrayList<>(items);
			arrayList.add(0, category);
			return arrayList;
		}
		
	}
}
