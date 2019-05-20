package dat.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tsc9526.monalisa.core.query.datatable.DataMap;

import dat.App;
import dat.domain.DataTable;
import dat.domain.ForeignKeyInfo;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.model.Graph;
import dat.model.Vertex;
import dat.repos.ForeignKeyInfoRepository;
import dat.util.Connectivity;

public class MyMonaliseWrapper implements MonaliseWrapper {

	@Override
	public com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> transfor(List<VirtualColumn> columns) { 
		List<TableColumn> realColumns = new ArrayList<>();
		columns.forEach(column -> {
			List<TableColumn> tableColumns = column.getRefColumns();
			realColumns.addAll(tableColumns);
		});
		List<DataTable> tables = realColumns.stream().map(real -> real.getDataTable()).distinct().collect(Collectors.toList());
		ForeignKeyInfoRepository foreignKeyInfoRepository = App.getContext().getBean(ForeignKeyInfoRepository.class);
		List<ForeignKeyInfo> list = foreignKeyInfoRepository.findAll();
		Graph<DataTable> graph = new Graph<>();
		for (ForeignKeyInfo foreignKeyInfo : list) {
			DataTable tmpt1 = foreignKeyInfo.getForeignKey().getDataTable();
			graph.addVertex(tmpt1);
			DataTable tmp2 = foreignKeyInfo.getReferencedTable();
			graph.addVertex(tmp2);
			graph.addArc(tmpt1, tmpt1);
		}
		Connectivity<DataTable> cty = new Connectivity<DataTable>(graph);
		List<Graph<DataTable>> subgraph = cty.getSubgraph();
		for (Graph<DataTable> g : subgraph) {
			List<Vertex<DataTable>> vertexs = g.getVertexs();
			vertexs.stream().filter( v -> tables.contains(v.getData()));
		}
		return null;
	}

}
