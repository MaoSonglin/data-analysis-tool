package dat.vo;

import java.io.Serializable;

import dat.vo.EchartOptions.ItemStyle;

public class Serie implements Serializable {

	private static final long serialVersionUID = -869530856370072439L;

	private String type;
	
	private String name;
	
	private String columnId;
	
	private String seriesLayoutBy;
	
	private ItemStyle itemStyle;

	public Serie() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Serie(String type, String name, String columnId,
			String seriesLayoutBy) {
		super();
		this.type = type;
		this.name = name;
		this.columnId = columnId;
		this.seriesLayoutBy = seriesLayoutBy;
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

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public String getSeriesLayoutBy() {
		return seriesLayoutBy;
	}

	public void setSeriesLayoutBy(String seriesLayoutBy) {
		this.seriesLayoutBy = seriesLayoutBy;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public ItemStyle getItemStyle() {
		return itemStyle;
	}

	public void setItemStyle(ItemStyle itemStyle) {
		this.itemStyle = itemStyle;
	}

	@Override
	public String toString() {
		return "Serie [type=" + type + ", name=" + name + ", columnId="
				+ columnId + ", seriesLayoutBy=" + seriesLayoutBy
				+ ", itemStyle=" + itemStyle + "]";
	}
	
	
}
