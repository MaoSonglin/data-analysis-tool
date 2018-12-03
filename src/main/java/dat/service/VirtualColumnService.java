package dat.service;

import java.util.List;

import dat.domain.VirtualColumn;
import dat.vo.Response;
import dat.vo.VirtualColumnParam;

public interface VirtualColumnService {

	Response deleteById(String id);

	/**
	 * 保存虚拟字段column，如果column中封装的ID在数据库中已经存在了，就修改数据库中的对象，否则就添加
	 * @param column
	 * @return
	 */
	Response save(VirtualColumn column);

	/**
	 * 新建字段
	 * @param vt
	 * @return
	 */
	Response createField(VirtualColumn vt);

	VirtualColumn getById(String id);

	List<VirtualColumn> getByPage(VirtualColumnParam param);

}
