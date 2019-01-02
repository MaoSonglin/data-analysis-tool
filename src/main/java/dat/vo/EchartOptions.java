package dat.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EchartOptions implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7413945254673723800L;

	private Title title;
	
	@JsonProperty("xAxis")
	private Axis xAxis;
	
	@JsonProperty("yAxis")
	private Axis yAxis;
	
	private Tooltip tooltip;
	
	private Legend legend;
	
	private String category;
	
	private Toolbox toolbox;
	
	private Grid grid;
	
	private List<Serie> series;
	
	private DataSet dataset;
	
	private DataZoom dataZoom;
	
	
	public Title getTitle() {
		return title;
	}

	public void setTitle(Title title) {
		this.title = title;
	}

	public Axis getxAxis() {
		return xAxis;
	}

	public void setxAxis(Axis xAxis) {
		this.xAxis = xAxis;
	}

	public Axis getyAxis() {
		return yAxis;
	}

	public void setyAxis(Axis yAxis) {
		this.yAxis = yAxis;
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public DataZoom getDataZoom() {
		return dataZoom;
	}

	public void setDataZoom(DataZoom dataZoom) {
		this.dataZoom = dataZoom;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public Legend getLegend() {
		return legend;
	}

	public void setLegend(Legend legend) {
		this.legend = legend;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setXaxis(Axis axis){
		this.xAxis = axis;
	}
	
	public void setYaxis(Axis axis){
		this.yAxis = axis;
	}
	
	public List<Serie> getSeries() {
		if(series == null){
			series = new ArrayList<>();
		}
		return series;
	}

	public DataSet getDataset() {
		return dataset;
	}

	public void setDataset(DataSet dataset) {
		this.dataset = dataset;
	}

	public void setSeries(List<Serie> series) {
		this.series = series;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	
	public Toolbox getToolbox() {
		return toolbox;
	}

	public void setToolbox(Toolbox toolbox) {
		this.toolbox = toolbox;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public static class Title{
		
		public Title() {
			super();
		}

		public Title(String text) {
			super();
			this.text = text;
		}

		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	public static class Axis{
		private Boolean show;
		private String type;
		private String name;
		private String columnId;
		public Axis() {
			super();
		}
		
		public Axis(String type) {
			super();
			this.type = type;
		}

		public Axis(String type, String name) {
			super();
			this.type = type;
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public Boolean getShow() {
			return show;
		}

		public void setShow(Boolean show) {
			this.show = show;
		}

		public String getColumnId() {
			return columnId;
		}

		public void setColumnId(String columnId) {
			this.columnId = columnId;
		}
		
	}

	public static class YAxis extends Axis{}
	
	public static class XAxis extends Axis{}
	
	public static class Tooltip{
		boolean show = true;

		public boolean isShow() {
			return show;
		}

		public void setShow(boolean show) {
			this.show = show;
		}
		
	}
	
	public static class Grid {
		private Boolean show;
		
		private Tooltip tooltip;

		public Boolean getShow() {
			return show;
		}

		public void setShow(Boolean show) {
			this.show = show;
		}

		public Tooltip getTooltip() {
			return tooltip;
		}

		public void setTooltip(Tooltip tooltip) {
			this.tooltip = tooltip;
		}
		
		
	}
	
	public static class Toolbox{
		
		private Boolean show;
		
		private String orient;
		
		private Map<String,Feature> feature;

		public Boolean getShow() {
			return show;
		}

		public void setShow(Boolean show) {
			this.show = show;
		}

		public String getOrient() {
			return orient;
		}

		public void setOrient(String orient) {
			this.orient = orient;
		}

		public Map<String, Feature> getFeature() {
			return feature;
		}

		public void setFeature(Map<String, Feature> feature) {
			this.feature = feature;
		}
		
	}
	
	public static class Legend{
		boolean show = true;
		
		private String type;
		
		private String orient = "horizontal";
		
		public boolean isShow() {
			return show;
		}

		public void setShow(boolean show) {
			this.show = show;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getOrient() {
			return orient;
		}

		public void setOrient(String orient) {
			this.orient = orient;
		}
		
	}
	
	public static class DataSet{
		
		public DataSet() {
			super();
		}

		public DataSet(List<List<String>> source) {
			super();
			this.source = source;
		}

		public DataSet(List<? extends Object> dimensions,
				List<List<String>> source) {
			super();
			this.dimensions = dimensions;
			this.source = source;
		}

		private List<? extends Object> dimensions;
		
		private List<List<String>> source;
		
		private Boolean sourceHeader;

		public List<List<String>> getSource() {
			return source;
		}

		public void setSource(List<List<String>> list) {
			this.source = list;
		}

		public List<? extends Object> getDimensions() {
			return dimensions;
		}

		public void setDimensions(List<? extends Object> dimensions) {
			this.dimensions = dimensions;
		}

		public Boolean getSourceHeader() {
			return sourceHeader;
		}

		public void setSourceHeader(Boolean sourceHeader) {
			this.sourceHeader = sourceHeader;
		}
		
	}
	
	public static class DataZoom{
		
		private Boolean show;
		
		private String type = "slider";
		private float start = 0;
		private float end = 50;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public float getStart() {
			return start;
		}
		public void setStart(float start) {
			this.start = start;
		}
		public float getEnd() {
			return end;
		}
		public void setEnd(float end) {
			this.end = end;
		}
		public Boolean getShow() {
			return show;
		}
		public void setShow(Boolean show) {
			this.show = show;
		}
		
	}
	
	public static class ItemStyle{
		
		public ItemStyle(String color) {
			super();
			this.color = color;
		}
		public ItemStyle() {
			super();
		}
		String color;
		String borderColor;
		float borderWidth = 0f;
		String borderType = "solid";
		String shadowBlur;
		String shadowColor;
		float shadowOffsetX = 0f;
		float shadowOffsetY = 0f;
		float barBorderRadius = 0f;
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		public String getBorderColor() {
			return borderColor;
		}
		public void setBorderColor(String borderColor) {
			this.borderColor = borderColor;
		}
		public float getBorderWidth() {
			return borderWidth;
		}
		public void setBorderWidth(float borderWidth) {
			this.borderWidth = borderWidth;
		}
		public String getBorderType() {
			return borderType;
		}
		public void setBorderType(String borderType) {
			this.borderType = borderType;
		}
		public String getShadowBlur() {
			return shadowBlur;
		}
		public void setShadowBlur(String shadowBlur) {
			this.shadowBlur = shadowBlur;
		}
		public String getShadowColor() {
			return shadowColor;
		}
		public void setShadowColor(String shadowColor) {
			this.shadowColor = shadowColor;
		}
		public float getShadowOffsetX() {
			return shadowOffsetX;
		}
		public void setShadowOffsetX(float shadowOffsetX) {
			this.shadowOffsetX = shadowOffsetX;
		}
		public float getShadowOffsetY() {
			return shadowOffsetY;
		}
		public void setShadowOffsetY(float shadowOffsetY) {
			this.shadowOffsetY = shadowOffsetY;
		}
		public float getBarBorderRadius() {
			return barBorderRadius;
		}
		public void setBarBorderRadius(float barBorderRadius) {
			this.barBorderRadius = barBorderRadius;
		}
		
	}

	public static class DimensionObject{
		String name;
		String type;
		String displayName;
		
		public DimensionObject() {
			super();
			// TODO Auto-generated constructor stub
		}
		public DimensionObject(String name, String type, String displayName) {
			super();
			this.name = name;
			this.type = type;
			this.displayName = displayName;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}

	public static class Feature{
		
		private Boolean show;
		
		private String title;
		
		private String icon;

		public Boolean getShow() {
			return show;
		}

		public void setShow(Boolean show) {
			this.show = show;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}
		
		
		
	}
}
