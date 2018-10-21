package dat.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="mune")
public class Menu {

	@Id
	@GeneratedValue
	private Integer id;
	
	private String text;

    private String link;

    private String title;

    private String cssclass;

    private String icon;

    private Integer parent;

//    @ManyToOne(targetEntity=Menu.class,fetch=FetchType.EAGER)
//    @JoinColumn(name="id")
//    private Menu father;
    
    @OneToMany(targetEntity=Menu.class,mappedBy="parent",fetch=FetchType.EAGER)
    private Set<Menu> children ;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCssclass() {
		return cssclass;
	}

	public void setCssclass(String cssclass) {
		this.cssclass = cssclass;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

//	public Menu getFather() {
//		return father;
//	}
//
//	public void setFather(Menu father) {
//		this.father = father;
//	}

	public Set<Menu> getChildren() {
		return children;
	}

	public void setChildren(Set<Menu> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Menu [id=" + id + ", text=" + text + ", link=" + link
				+ ", title=" + title + ", cssclass=" + cssclass + ", icon="
				+ icon + ", parent=" + parent + "]";
	}
    
    
    
}
