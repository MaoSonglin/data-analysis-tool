package dat.service;

import java.util.List;
import java.util.Map;

import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.vo.Response;


/**
 * @author MaoSonglin
 * 虚拟数据表服务层接口
 */
public interface VirtualTableService {

	/**
	 * 获取指定ID的虚拟数据表
	 * @param id
	 * @return
	 */
	Response getById(String id);

	/**
	 * 获取指定ID的数据表中包含的虚拟字段
	 * @param id
	 * @return
	 */
	Response getVirtualColumns(String id);

	/**
	 * 添加虚拟数据表
	 * @param table
	 * @return
	 */
	@Deprecated
	Response add(VirtualTable table);
	
	/**
	 * 查询虚拟表中的数据
	 * @param t
	 * @return
	 */
	List<Map<String,String>> getData(VirtualTable t);
	
	Map<String, List<String>> listData(VirtualTable t);
	
	/**
	 * 查询数据表中的数据
	 * @param table		待查询的数据表信息
	 * @param columns	带检索的数据字段	
	 * @param offset	结果集返回时的偏移量
	 * @param limit		查询记录条数
	 * @return			结果集映射对象，key为列名称，value为每一列的数据数组
	 */
	Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns,int offset,int limit);
	
	/**
	 * 查询数据表中的数据，该方法默认偏移量为0
	 * @param table		待查询的数据表信息
	 * @param columns	带检索的数据字段	
	 * @param limit		查询记录条数
	 * @return			结果集映射对象，key为列名称，value为每一列的数据数组
	 */
	default Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns,int limit) {
		return getData(table,columns,0,limit);
	};
	/**
	 * 查询数据表中的数据，该方法默认返回1000条数据，偏移量为0
	 * @param table		待查询的数据表信息
	 * @param columns	带检索的数据字段	
	 * @return			结果集映射对象，key为列名称，value为每一列的数据数组
	 */
	default Map<String, List<String>> getData(VirtualTable table,List<VirtualColumn> columns) {
		return getData(table,columns,0,1000);
	}
}
