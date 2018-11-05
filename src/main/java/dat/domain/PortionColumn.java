package dat.domain;

import javax.persistence.Column;
import javax.persistence.Id;

import dat.util.StrUtil;

/**
 * @author MaoSonglin
 * 虚拟字段的子丢按描述实体类
 */
//@Entity
public class PortionColumn implements IdGeneratorable {

	public PortionColumn() {
		super();
	}

	public PortionColumn(String id, String targetColumnId, String entityTarget) {
		super();
		this.id = id;
		this.targetColumnId = targetColumnId;
		this.entityTarget = entityTarget;
	}



	@Id @Column(length=18)
	private String id;
	

	private String targetColumnId;
	
	private String entityTarget ;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public String getTargetColumnId() {
		return targetColumnId;
	}

	public void setTargetColumnId(String targetColumnId) {
		this.targetColumnId = targetColumnId;
	}

	public String getEntityTarget() {
		return entityTarget;
	}

	public void setEntityTarget(String entityTarget) {
		this.entityTarget = entityTarget;
	}



	@Override
	public void generateId() {
		this.id = StrUtil.generatorId();
	}
	
	
}
