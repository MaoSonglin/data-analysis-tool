package dat.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import dat.util.Constant;
import dat.util.StrUtil;

@Entity
public class UploadFile implements IdGeneratorable {

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
}
