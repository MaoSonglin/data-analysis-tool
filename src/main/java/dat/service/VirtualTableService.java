package dat.service;

import java.util.List;
import java.util.Map;

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
}
