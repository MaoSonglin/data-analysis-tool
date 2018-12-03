package dat.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dat.util.Constant;
import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 数据源实体类
 */
@Entity
@Table(name="datasource")
public class Source implements IdGeneratorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1678788268060279496L;

	@Id
	private String id;
	
	private String name;
	
	private String chinese;
	
	private String jdbcDirver;
	
	private String driverClass;
	
	private String url;
	
	@Column(name="u_name")
	private String username;
	
	@Column(name="pwd")
	private String password;
	
	private String charset;
	
	private String sortRule;
	
	private String databaseName;
	
	private String addTime;
	
	@Column(name="`desc`")
	private String desc;
	
	private Integer state = Constant.ACTIVATE_SATE;
	
	@OneToOne(targetEntity=UploadFile.class,fetch=FetchType.EAGER)
	@JoinColumn(name="file_id",referencedColumnName="id")
	private UploadFile association;

	@OneToMany(targetEntity=DataTable.class,fetch=FetchType.LAZY,mappedBy="source")
	@JsonIgnore
	private List<DataTable> tables;
	

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
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
		if(null != chinese && chinese.length() > 127){
			chinese = chinese.substring(0, 126);
		}
		this.chinese = chinese;
	}

	public String getCommit() {
		return desc;
	}

	public void setCommit(String commit) {
		this.desc = commit;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getSortRule() {
		return sortRule;
	}

	public void setSortRule(String sortRule) {
		this.sortRule = sortRule;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Source [id=" + id + ", name=" + name + ", chinese=" + chinese
				+ ", jdbcDirver=" + jdbcDirver + ", driverClass=" + driverClass
				+ ", url=" + url + ", username=" + username + ", password="
				+ password + ", charset=" + charset + ", sortRule=" + sortRule
				+ ", databaseName=" + databaseName + ", addTime=" + addTime
				+ ", state=" + state + "]";
	}

	public UploadFile getAssociation() {
		return association;
	}

	public void setAssociation(UploadFile association) {
		this.association = association;
	}

	@Override
	public void generateId() {
		String id = StrUtil.generatorId();
		setId("DB"+id.substring(2));
	}

	public String getJdbcDirver() {
		return jdbcDirver;
	}

	public void setJdbcDirver(String jdbcDirver) {
		this.jdbcDirver = jdbcDirver;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public List<DataTable> getTables() {
		return tables;
	}

	public void setTables(List<DataTable> tables) {
		for (DataTable dataTable : tables) {
			dataTable.setSource(this);
		}
		this.tables = tables;
	}

	public void setAddTime(Date date) {
		String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		setAddTime(format);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Source other = (Source) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
