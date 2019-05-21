package dat.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import dat.util.Constant;
import dat.util.StrUtil;
import dat.vo.EchartOptions;
import dat.vo.EchartOptions.Axis;
import lombok.Data;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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
	
	@ManyToMany(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	private List<VirtualColumn> columns;
	
	private Integer state;
	
	@Column(name="`commit`",length=500)
	private String desc;
	
	@ManyToOne(targetEntity=ReportInfo.class,fetch=FetchType.LAZY)
	@JsonIgnoreProperties({"pkg","columns"})
	private ReportInfo report;

	public void generateId() {
		id = "GI"+StrUtil.generatorId().substring(2);
	}


	public EchartOptions getOption(){
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
	

}
