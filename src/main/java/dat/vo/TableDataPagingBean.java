package dat.vo;

import dat.domain.GraphInfo;

public class TableDataPagingBean extends PagingBean {
	private GraphInfo graph;

	public GraphInfo getGraph() {
		return graph;
	}

	public void setGraph(GraphInfo graph) {
		this.graph = graph;
	}

	@Override
	public String toString() {
		return "TableDataPagingBean [graph=" + graph + ", getBeans()="
				+ getBeans() + ", getKeyword()=" + getKeyword()
				+ ", getCurPage()=" + getCurPage() + ", getPageSize()="
				+ getPageSize() + ", getTotalCount()=" + getTotalCount()
				+ ", getTotalPage()=" + getTotalPage() + "]";
	}

	
}
