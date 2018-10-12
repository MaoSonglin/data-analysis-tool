package dat.service;

import dat.pojo.Mune;
import dat.pojo.Response;

public interface MenuService {

	/**
	 * 列出指定菜单的ID下的所有子孙菜单
	 * @param menuid
	 * @return
	 */
	Response list(Integer menuid);

	/**
	 * 添加新菜单
	 * @param menu
	 * @return
	 */
	Response addMenu(Mune menu);

	/**
	 * 修改菜单内容
	 * @param menu
	 * @return
	 */
	Response update(Mune menu);

	/**
	 * 删除菜单项
	 * @param menu
	 * @return
	 */
	Response delete(Integer menu);

}
