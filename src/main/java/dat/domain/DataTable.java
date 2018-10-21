package dat.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据表实体类，该实体类对应某个数据源中实际存在的数据表，表中的数据字段与该实体类中的属性一一对应
 */
@Entity
public class DataTable implements IdGeneratorable{

	@Id
	private String id;
	
	private String name;
	
	private String chinese;
	
	@ManyToOne(targetEntity=Source.class)
	@JoinColumn(name="data_source_id",referencedColumnName="id")
	private Source source;
	
	private Date addTime;
	
	private Integer state;

	@Override
	public String toString() {
		return "DataTable [id=" + id + ", name=" + name + ", chinese="
				+ chinese + ", source=" + source + ", addTime=" + addTime
				+ ", state=" + state + "]";
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

	public String getChinese() {
		return chinese;
	}

	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
	
	
	public void generateId(){
		String id = StrUtil.generatorId();
		setId(id);
	}
}