package dat.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="mune")
@JsonIgnoreProperties({"handler","hibernateLazyInitializer"})
public class Menu {

	@Id
	@GeneratedValue
	private Integer id;
	
	private String text;

	@Column(name="link")
    private String url;

	@JsonIgnore
    private String title;

    private String cssclass;

    @Column(name="icon")
    private String iconCls;
    
    private String state;
    
    private Integer level;

    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}

	public String getState() {
		if(state == null)
			return "closed";
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	@Column(name="parent")
    private Integer pid;

//    @ManyToOne(targetEntity=Menu.class,fetch=FetchType.EAGER)
//    @JoinColumn(name="id")
//    private Menu father;
    
    @OneToMany(targetEntity=Menu.class,mappedBy="pid",fetch=FetchType.EAGER)
    private List<Menu> children ;

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
		return url;
	}

	public void setLink(String link) {
		this.url = link;
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
		return iconCls;
	}

	public void setIcon(String icon) {
		this.iconCls = icon;
	}

	public Integer getParent() {
		return pid;
	}

	public void setParent(Integer parent) {
		this.pid = parent;
	}

//	public Menu getFather() {
//		return father;
//	}
//
//	public void setFather(Menu father) {
//		this.father = father;
//	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Menu [id=" + id + ", text=" + text + ", link=" + url
				+ ", title=" + title + ", cssclass=" + cssclass + ", icon="
				+ iconCls + ", parent=" + pid + "]";
	}
    
    
    
}
