package dat.vo;

import java.util.List;


public class PagingBean {

	private Integer curPage = 0;
	
	private Integer pageSize = 100;
	
	private Integer totalCount;
	
	private Integer totalPage;

	private String keyword;
	
	private List<? extends Object> beans;
	
	public PagingBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PagingBean(Integer curPage, Integer pageSize) {
		super();
		this.curPage = curPage;
		this.pageSize = pageSize;
	}

	public List<? extends Object> getBeans() {
		return beans;
	}

	public void setBeans(List<? extends Object> beans) {
		this.beans = beans;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Integer getCurPage() {
		return curPage;
	}

	public void setCurPage(Integer curPage) {
		this.curPage = curPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	@Override
	public String toString() {
		return "PagingBean [curPage=" + curPage + ", pageSize=" + pageSize
				+ ", totalCount=" + totalCount + ", totalPage=" + totalPage
				+ "]";
	}
	
	
}
