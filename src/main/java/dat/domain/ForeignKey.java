package dat.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * @author MaoSonglin
 *
 */
@Entity
public class ForeignKey implements Serializable{
	
	private static final long serialVersionUID = -6855370866755464888L;

	/**
	 * 外键所在的数据表
	 */
	@ManyToOne(targetEntity=DataTable.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	private DataTable foreignTable;
	
	/**
	 * 引用的数据表
	 */
	@ManyToOne(targetEntity=DataTable.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	private DataTable primaryTable;
	
	/**
	 * 作为外键的字段
	 */
	@Id
	@OneToOne(targetEntity=TableColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	private TableColumn foreignColumn ;
	
	/**
	 * 外键引用的字段
	 */
	@ManyToOne(targetEntity=TableColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	private TableColumn primaryColumn;

	

	/**
	 * 返回外键参考的数据表
	 * @return
	 */
	public DataTable getPrimaryTable() {
		return primaryTable;
	}

	/**
	 * 设置外键参考的数据表
	 * @param primaryTable
	 */
	public void setPrimaryTable(DataTable primaryTable) {
		this.primaryTable = primaryTable;
	}

	/**
	 * 获取外键所在的数据表
	 * @return
	 */
	public DataTable getForeignTable() {
		return foreignTable;
	}

	/**
	 * 外键所在的数据表
	 * @param foreignTable
	 */
	public void setForeignTable(DataTable foreignTable) {
		this.foreignTable = foreignTable;
	}

	/**
	 * 获取外键参考的字段
	 * @return
	 */
	public TableColumn getPrimaryColumn() {
		return primaryColumn;
	}

	/**
	 * 设置外键参考的字段
	 * @param primaryColumn
	 */
	public void setPrimaryColumn(TableColumn primaryColumn) {
		this.primaryColumn = primaryColumn;
	}

	/**
	 * 获取作为外键的字段
	 * @return
	 */
	public TableColumn getForeignColumn() {
		return foreignColumn;
	}

	/**
	 * 设置作为外键的字段
	 * @param foreignColumn
	 */
	public void setForeignColumn(TableColumn foreignColumn) {
		this.foreignColumn = foreignColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((foreignColumn == null) ? 0 : foreignColumn.hashCode());
		result = prime * result
				+ ((foreignTable == null) ? 0 : foreignTable.hashCode());
		result = prime * result
				+ ((primaryColumn == null) ? 0 : primaryColumn.hashCode());
		result = prime * result
				+ ((primaryTable == null) ? 0 : primaryTable.hashCode());
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
		ForeignKey other = (ForeignKey) obj;
		if (foreignColumn == null) {
			if (other.foreignColumn != null)
				return false;
		} else if (!foreignColumn.equals(other.foreignColumn))
			return false;
		if (foreignTable == null) {
			if (other.foreignTable != null)
				return false;
		} else if (!foreignTable.equals(other.foreignTable))
			return false;
		if (primaryColumn == null) {
			if (other.primaryColumn != null)
				return false;
		} else if (!primaryColumn.equals(other.primaryColumn))
			return false;
		if (primaryTable == null) {
			if (other.primaryTable != null)
				return false;
		} else if (!primaryTable.equals(other.primaryTable))
			return false;
		return true;
	}
	
	
}
