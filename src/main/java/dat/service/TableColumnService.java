package dat.service;

import java.util.List;

import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.vo.Response;
import dat.vo.TableColumnPagingBean;

public interface TableColumnService {

	/**
	 * 根据pageBean中封装的查询条件，分页标准查询
	 * @param pageBean
	 * @return
	 */
	Response search(TableColumnPagingBean pageBean);

	/**
	 * 查询出指定ID的字段信息
	 * @param id	待查询的ID标识符
	 * @return	封装了查询结果的响应对象
	 */
	Response getById(String id);

	/**
	 * 查询指定ID的字段所属的数据表信息
	 * @param id	字段的ID
	 * @return		封装了数据表的响应信息
	 */
	Response getTableById(String id);

	/**
	 * 查询指定ID的字段所属的数据源信息
	 * @param id	字段的ID
	 * @return		封装了数据源的响应信息
	 */
	Response getSourceById(String id);

	
	/**
	 * 根据虚拟字段获取实体字段
	 * @param columns
	 * @return
	 */
	List<TableColumn> getColumnsByVirtualColumns(List<VirtualColumn> columns);

	String getTypeName(TableColumn column);
}
