package dat.domain;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@Entity
@Deprecated
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Reference implements Serializable {

	private static final long serialVersionUID = -7020506078796556889L;
//
//	@Id
//	@GeneratedValue
	private Long id;
	
	// 该外键所在的数据表
//	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
//	@JoinColumn(referencedColumnName="id")
	private VirtualTable primaryTable;
	
	// 引用的的字段
	@ManyToOne(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","table"})
	private VirtualColumn referencedColumn;
	
	// 作为外键的字段
	@OneToOne(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","table"})
	private VirtualColumn foreignColumn;
	
	// 引用的数据表
	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	private VirtualTable referencedTable;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public VirtualTable getPrimaryTable() {
		return primaryTable;
	}

	public void setPrimaryTable(VirtualTable primaryTable) {
		this.primaryTable = primaryTable;
	}

	public VirtualColumn getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(VirtualColumn referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public VirtualColumn getForeignColumn() {
		return foreignColumn;
	}

	public void setForeignColumn(VirtualColumn foreignColumn) {
		this.foreignColumn = foreignColumn;
	}

	public VirtualTable getReferencedTable() {
		return referencedTable;
	}

	public void setReferencedTable(VirtualTable referencedTable) {
		this.referencedTable = referencedTable;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
