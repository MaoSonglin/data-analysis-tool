package dat.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author MaoSonglin
 * 虚拟数据表virtualtable之间的关联关系
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Data
public class Reference implements Serializable {

	private static final long serialVersionUID = -7020506078796556889L;

	@Id
	@GeneratedValue
	private Long id;
	
	private String joinClassName;
	
	// 该外键所在的数据表
	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","columns", "packages"})
	private VirtualTable primaryTable;
	
	// 引用的的字段
	@ManyToOne(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","table","graphs","refColumns"})
	private VirtualColumn referencedColumn;
	
	// 作为外键的字段
	@OneToOne(targetEntity=VirtualColumn.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","table","graphs","refColumns"})
	private VirtualColumn foreignColumn;
	
	// 引用的数据表
	@ManyToOne(targetEntity=VirtualTable.class,fetch=FetchType.LAZY)
	@JoinColumn(referencedColumnName="id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler","columns", "packages"})
	private VirtualTable referencedTable;

	@Override
	public String toString() {
		return "Reference [id=" + id + ", joinClassName=" + joinClassName + ", primaryTable=" + primaryTable
				+ ", referencedColumn=" + referencedColumn + ", foreignColumn=" + foreignColumn + ", referencedTable="
				+ referencedTable + "]";
	}

}
