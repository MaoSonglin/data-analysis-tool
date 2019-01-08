package dat.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import dat.util.Constant;
import dat.util.StrUtil;
import dat.vo.EchartOptions;
import dat.vo.EchartOptions.Axis;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	
	private String parent;
	
	@Column(length=8000)
	private String options;
	

	
	private Integer state;
	
	@Column(name="`commit`",length=500)
	private String desc;
	
	@ManyToOne(targetEntity=ReportInfo.class,fetch=FetchType.LAZY)
	@JsonIgnoreProperties({"pkg","columns"})
	private ReportInfo report;
	/*
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	private List<VirtualColumn> categoryColumns;
	
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	private List<VirtualColumn> valueColumns;
	*/
	
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


	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public Float getTop() {
		return top;
	}

	public EchartOptions getOption(){
//		JSON.toJavaObject(getOptions(), EchartOptions.class);
		String text = getOptions();
		if(text == null)
			return null;
		EchartOptions opt = JSON.parseObject(text,EchartOptions.class);
		JSONObject parseObject = JSON.parseObject(text);
		JSONObject jsonObject = parseObject.getJSONObject("xAxis");
		Axis javaObject = JSON.toJavaObject(jsonObject, Axis.class);
		opt.setXaxis(javaObject);
		JSONObject object = parseObject.getJSONObject("yAxis");
		Axis y = JSON.toJavaObject(object, Axis.class);
		opt.setyAxis(y);
		return opt;
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

/*	public List<VirtualColumn> getColumns() {
		ArrayList<VirtualColumn> list = new ArrayList<>();
		List<VirtualColumn> l = getCategoryColumns();
		if(l != null){
			list.addAll(l);
		}
		List<VirtualColumn> l2 = getValueColumns();
		if(l2!=null)
			list.addAll(l2);
		return list;
	}
*/
	

	/*public List<VirtualColumn> getCategoryColumns() {
		return categoryColumns;
	}

	public void setCategoryColumns(List<VirtualColumn> categoryColumns) {
		this.categoryColumns = categoryColumns;
	}

	public List<VirtualColumn> getValueColumns() {
		return valueColumns;
	}

	public void setValueColumns(List<VirtualColumn> valueColumns) {
		this.valueColumns = valueColumns;
	}
*/
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
