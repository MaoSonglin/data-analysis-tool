package dat.pojo;

import java.util.List;

public class Mune {
    private Integer id;

    private String text;

    private String link;

    private String title;

    private String cssclass;

    private String icon;

    private Integer parent;

    private Integer order;
    
    private List<Mune> children;

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
        this.text = text == null ? null : text.trim();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link == null ? null : link.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getCssclass() {
        return cssclass;
    }

    public void setCssclass(String cssclass) {
        this.cssclass = cssclass == null ? null : cssclass.trim();
    }

    public void setCssClass(String cssclass){
    	this.cssclass = cssclass == null ? null : cssclass.trim();
    }
    public String getCssClass(){
    	return cssclass;
    }
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

	public List<Mune> getChildren() {
		return children;
	}

	public void setChildren(List<Mune> children) {
		this.children = children;
	}
    
}