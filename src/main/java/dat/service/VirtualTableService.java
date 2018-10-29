package dat.service;

import dat.domain.Response;


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
}
