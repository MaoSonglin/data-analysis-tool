package dat.data;

import java.util.List;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.domain.VirtualColumn;

public interface MonaliseWrapper {

	DataTable<DataMap> transfor(List<VirtualColumn> columns);
}
