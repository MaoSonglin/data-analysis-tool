package dat.data;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import dat.domain.DataTable;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.VirtualTableService;

@Deprecated
public class QueryHelper implements Serializable,ApplicationContextAware{
	
	
	private static final long serialVersionUID = -5188110890420853846L;
	
	private static Logger logger = LoggerFactory.getLogger(QueryHelper.class);

	private ApplicationContext context;

	public DataAdapter query(VirtualTable table,List<VirtualColumn> columns){
		if(logger.isDebugEnabled()){
			logger.debug("查询数据表："+table.getName());
			columns.forEach(virtualColumn->{
				logger.debug(virtualColumn.getName());
			});
			return new TableDataAdapter(columns);
		}
		// 虚拟数据表的服务层接口
		VirtualTableService virtualTableService = context.getBean(VirtualTableService.class);
		// 判断数据表table的类型
		int type = virtualTableService.getType(table);
		logger.debug("虚拟数据表‘%s’的类型是：%d",table.getName(),type);
		// 根据类型返回不同的数据适配器
		switch(type){
		case 1:// 单表类型
			List<DataTable> quoteTable = virtualTableService.getQuoteTable(table);
			DataTable mainTable = quoteTable.remove(0);
			return new SingleTableDataAdapter(mainTable, columns,mainTable.getColumns());
		case 2:// 同源多表类型
			List<DataTable> quoteTable1 = virtualTableService.getQuoteTable(table);
			SingleSourceDataAdapter dataAdapter = new SingleSourceDataAdapter(context);
			dataAdapter.setColumns(columns);
			dataAdapter.setDataTables(quoteTable1);
			return dataAdapter;
		case 3: // 异源多表类型
			return new MultipleTableAdapter(table);
		default:
			throw new RuntimeException();	
		}
	}
	

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}
	
	
}





