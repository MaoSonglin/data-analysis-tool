package dat.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
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
	
	@OneToOne(targetEntity=Menu.class,fetch=FetchType.EAGER)
	@JoinColumn(referencedColumnName="id")
	private Menu publish;
	
	@Column(name="`comment`")
	private String comment;
	
	@ManyToOne(targetEntity=WorkPackage.class,fetch=FetchType.EAGER)
	@JoinColumn(referencedColumnName="id")
	private WorkPackage pkg;
	
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

	public WorkPackage getPkg() {
		return pkg;
	}

	public void setPkg(WorkPackage pkg) {
		this.pkg = pkg;
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
	
	public String getPkgName(){
		if(pkg != null)
			return pkg.getName();
		else
			return null;
	}

	

	public Menu getPublish() {
		return publish;
	}

	public void setPublish(Menu publish) {
		this.publish = publish;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
