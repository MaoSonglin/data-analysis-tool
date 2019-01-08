package dat.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class UploadFile implements IdGeneratorable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5513948156395874556L;

	@Id
	private String id;

	private String fileName;
	
	private String virtualPath;
	
	private Long size;
	
	private Integer state = Constant.DELETE_STATE;
	
	private String addTime;
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getVirtualPath() {
		return virtualPath;
	}


	public void setVirtualPath(String virtualPath) {
		this.virtualPath = virtualPath;
	}


	public Long getSize() {
		return size;
	}


	public void setSize(Long size) {
		this.size = size;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	@Override
	public void generateId() {
		id = StrUtil.generatorId();
	}


	public String getAddTime() {
		return addTime;
	}


	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}


	@Override
	public String toString() {
		return "UploadFile [id=" + id + ", fileName=" + fileName
				+ ", virtualPath=" + virtualPath + ", size=" + size
				+ ", state=" + state + ", addTime=" + addTime + "]";
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
		UploadFile other = (UploadFile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
