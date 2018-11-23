package dat.service;

import java.util.List;
import java.util.Map;

import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.vo.DataTablePaingBean;
import dat.vo.Response;
import dat.vo.TableData;

public interface DataTableService {

	/**
	 * 根据分页条件和过滤条件查询数据表记录
	 * @param pageBean 封装了分页条件和查询条件的javabean对象
	 * @return	查询结果
	 */
	Response search(DataTablePaingBean pageBean);

	/**
	 * 根据数据表的ID查询数据表类容
	 * @param id	数据表id
	 * @return		封装了数据表内容的响应对象
	 */
	Response getById(String id);

	/**
	 * 获取指定数据表的id所对应的数据源信息
	 * @param id	数据表id
	 * @return		查询结果
	 */
	Response getSource(String id);

	/**
	 * 获取指定ID的数据表中的所有字段信息
	 * @param id	数据表的id
	 * @return
	 */
	Response getTables(String id);

	List<Map<String,String>> getValues(TableData td);

	/**
	 * 获取数据表中的数据
	 * @param table
	 * @return
	 */
	List<Map<String,String>> getValues(DataTable table);
	
	List<Map<String,String>> getValues(DataTable table,List<TableColumn> columns);
}
