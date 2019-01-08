package dat.domain;

import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

//@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Category {

	@Id
	@GeneratedValue
	private Long id;
	
	private String name;
	
	@JsonIgnoreProperties({"parents","children"})
	@ManyToMany(targetEntity=Category.class)
	List<Category> children;
	
	@JsonIgnoreProperties({"parents","children"})
	@ManyToMany(targetEntity=Category.class,mappedBy="children")
	List<Category> parents;
	
	@ManyToMany(targetEntity=VirtualColumn.class,mappedBy="categories",fetch=FetchType.LAZY)
	@JsonIgnoreProperties({"table","categories"})
	private List<VirtualColumn> columns;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<VirtualColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<VirtualColumn> columns) {
		this.columns = columns;
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children;
	}

	public List<Category> getParents() {
		return parents;
	}

	public void setParents(List<Category> parents) {
		this.parents = parents;
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
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
