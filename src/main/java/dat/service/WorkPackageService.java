package dat.service;

import dat.domain.Response;
import dat.domain.WorkPackage;

public interface WorkPackageService {

	/**
	 * 获取指定ID的工作包
	 * @param id
	 * @return
	 */
	Response getPackage(String id);

	/**
	 * 获取指定ID的工作包包含的数据表
	 * @param id	工作包的id
	 * @return		数据表列表
	 */
	Response getTables(String id);

	/**
	 * 添加数据包
	 * @param pg
	 * @return
	 */
	Response add(WorkPackage pg);

	/**
	 * 为ID为pid的数据包添加一个ID为tid的数据表，该方法并不会将id为tid的数据表指定为业务包的一个子表
	 * 而是根据tid的数据表构建一个虚拟的数据表，将tid的数据表中的字段全都建立虚拟字段保存到新建的虚拟数据
	 * 表中，然后让业务包与虚拟数据表关联
	 * @param pid 业务包ID
	 * @param tid 数据表ID
	 * @return
	 */
	Response addTab(String pid, String tid);

	/**
	 * 将ID为pid的数据包的包含的ID为tid的数据表移除
	 * @param pid
	 * @param tid
	 * @return
	 */
	Response remove(String pid, String tid);

}
