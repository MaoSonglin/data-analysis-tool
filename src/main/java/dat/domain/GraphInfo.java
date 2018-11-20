package dat.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
public class GraphInfo implements IdGeneratorable{
	{
		this.state = Constant.ACTIVATE_SATE;
	}
	public GraphInfo() {
		super();
	}

	public GraphInfo(String title) {
		super();
		this.title = title;
	}

	public GraphInfo(Float width, Float length, Float top, Float left) {
		super();
		this.width = width;
		this.height = length;
		this.top = top;
		this.left = left;
	}

	public GraphInfo(String title, Float width, Float length, Float top,
			Float left) {
		super();
		this.title = title;
		this.width = width;
		this.height = length;
		this.top = top;
		this.left = left;
	}

	@Id
	private String id;
	
	private String title;
	
	private Float width;
	
	@Column(name="length")
	private Float height;
	
	private Float top;
	
	@Column(name="`left`")
	private Float left;
	
	private String type;
	
	private Integer state;
	
	@ManyToOne(targetEntity=ReportInfo.class,fetch=FetchType.EAGER)
	private ReportInfo report;
	
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(table="graph_x_axis")
	private List<VirtualColumn> xAxis;
	
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(table="graph_y_axis")
	private List<VirtualColumn> yAxis;
	
	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public void generateId() {
		id = "GI"+StrUtil.generatorId().substring(2);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ReportInfo getReport() {
		return report;
	}

	public void setReport(ReportInfo report) {
		this.report = report;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}


	public Float getTop() {
		return top;
	}

	public void setTop(Float top) {
		this.top = top;
	}

	public Float getLeft() {
		return left;
	}

	public void setLeft(Float left) {
		this.left = left;
	}

	public List<VirtualColumn> getxAxis() {
		return xAxis;
	}

	public void setxAxis(List<VirtualColumn> xAxis) {
		this.xAxis = xAxis;
	}

	public List<VirtualColumn> getyAxis() {
		return yAxis;
	}

	public void setyAxis(List<VirtualColumn> yAxis) {
		this.yAxis = yAxis;
	}

	@Override
	public String toString() {
		if(this.getClass().getName().equals(GraphInfo.class.getName()))
		return "GraphInfo [id=" + id + ", title=" + title + ", width=" + width
				+ ", height=" + height + ", top=" + top + ", left=" + left
				+ ", type=" + type + ", state=" + state
				+ ", xAxis=" + xAxis + ", yAxis=" + yAxis + "]";
		else
			return "GraphInfo [id=" + id + ", title=" + title + ", width=" + width
					+ ", height=" + height + ", top=" + top + ", left=" + left
					+ "]";
	}

	
	
}
