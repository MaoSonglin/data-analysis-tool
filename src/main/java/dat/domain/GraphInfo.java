package dat.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
public class GraphInfo implements IdGeneratorable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7380042617493135574L;

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
	
	@Column(length=800)
	private String options;
	
	private Integer state;
	
	@ManyToOne(targetEntity=ReportInfo.class,fetch=FetchType.EAGER)
	private ReportInfo report;
	
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	private List<VirtualColumn> columns;
	
	
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

	public List<VirtualColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<VirtualColumn> columns) {
		this.columns = columns;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "GraphInfo [id=" + id + ", title=" + title + ", width=" + width
				+ ", height=" + height + ", top=" + top + ", left=" + left
				+ ", type=" + type + ", options=" + options + ", state="
				+ state + "]";
	}
}
