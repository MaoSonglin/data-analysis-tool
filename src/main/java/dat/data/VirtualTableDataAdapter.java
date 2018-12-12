package dat.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.spi.RowSelection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.DataSourceService;
import dat.service.VirtualTableService;

public class VirtualTableDataAdapter implements DataAdapter,ApplicationContextAware {

	private ApplicationContext context;
	
	private RowSelection selection;

	private VirtualTable virtualTable;

	private ArrayList<VirtualColumn> virtualColumns;
	
	public VirtualTableDataAdapter(ApplicationContext context) {
		this.context = context;
	}
	
	public boolean select(VirtualColumn column){
		if(virtualColumns == null)
			virtualColumns = new ArrayList<>();
		boolean add = virtualColumns.add(column);
		return add;
	}
	
	public void from(VirtualTable table){
		this.virtualTable = table;
		VirtualTableService tableService = context.getBean(VirtualTableService.class);
		DataSourceService sourceService = context.getBean(DataSourceService.class);
		// 虚拟表包含的实体表
		List<DataTable> quoteTable = tableService.getQuoteTable(virtualTable);
		quoteTable.forEach(e->{
			DataSource dataSource = sourceService.getTemplate(e.getSource()).getDataSource();
			// 需要查询的列
			List<TableColumn> tableColumns = e.getColumns();
			List<String> columnNames = new ArrayList<>();
			// 获取列名
			tableColumns.forEach(elem->{
				String columnName = elem.getColumnName();
				columnNames.add(columnName);
			});
			try(Extractor extractor = new Extractor(dataSource)){
				// 设置要查询的表名和要查询的列名
				extractor.setExtractNames(e.getName(), columnNames);
				List<Map<String,String>> list = new ArrayList<>(20);
				for (Map<String,String> map : extractor) {
					list.add(map);
				}
			};
		});
	}
	
	@Override
	public Iterator<Map<String, String>> iterator() {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public void filter(String where) {

	}

	@Override
	public int clearFilter() {
		return 0;
	}

	@Override
	public void limit(int offset, int size) {
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.context = applicationContext;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> next() {
		// TODO Auto-generated method stub
		return null;
	}

}
