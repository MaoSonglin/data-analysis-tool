package dat.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
public class ForeignKeyInfo implements Serializable{

	private static final long serialVersionUID = -3561223615104443999L;

	@Id
	@GeneratedValue
	private Long id;
	
	@OneToOne(targetEntity=TableColumn.class,fetch=FetchType.EAGER)
	private TableColumn foreignKey;

	@ManyToOne(targetEntity=TableColumn.class,fetch=FetchType.EAGER)
	private TableColumn referencedColumn;

	public TableColumn getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(TableColumn foreignKey) {
		this.foreignKey = foreignKey;
	}
	
	public TableColumn getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(TableColumn referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public DataTable getDataTable(){
		TableColumn tableColumn = getForeignKey();
		if(tableColumn != null)
			return tableColumn.getDataTable();
		else
			return null;
	}
	
	public DataTable getReferencedTable(){
		TableColumn tableColumn = getReferencedColumn();
		if(tableColumn != null)
			return tableColumn.getDataTable();
		else
			return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
		ForeignKeyInfo other = (ForeignKeyInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String str = objectMapper.writeValueAsString(this);
			return str;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "ForeignKeyInfo [id=" + id + ", foreignKey=" + foreignKey
				+ ", referencedColumn=" + referencedColumn + "]";
	}
	
}
