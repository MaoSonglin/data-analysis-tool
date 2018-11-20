package dat.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
public class ReportInfo implements IdGeneratorable,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -159925811358260539L;

	public ReportInfo() {
		super();
	}

	public ReportInfo(String name) {
		super();
		this.name = name;
	}
	
	{
		this.state = Constant.ACTIVATE_SATE;
	}

	@Id
	private String id;
	
	private String name;
	
	private Float width;
	
	private Float height;
	
	@Column(name="`comment`")
	private String comment;
	
	@OneToMany(targetEntity=GraphInfo.class,fetch=FetchType.LAZY,mappedBy="report")
	@JsonIgnore
	private List<GraphInfo> graphs;
	
	private Integer state = Constant.ACTIVATE_SATE;
	
	@Override
	public void generateId() {
		id = "RP"+StrUtil.generatorId().substring(2);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GraphInfo> getGraphs() {
		return graphs;
	}

	public void setGraphs(List<GraphInfo> graphs) {
		this.graphs = graphs;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "ReportInfo [id=" + id + ", name=" + name + ", width=" + width
				+ ", height=" + height + ", comment=" + comment + ", graphs="
				+ graphs + ", state=" + state + "]";
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

}
