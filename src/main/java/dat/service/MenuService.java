package dat.service;

import java.util.List;

import dat.domain.Menu;
import dat.vo.Response;

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
	Response addMenu(Menu menu);

	/**
	 * 修改菜单内容
	 * @param menu
	 * @return
	 */
	Response update(Menu menu);

	/**
	 * 删除菜单项
	 * @param menu
	 * @return
	 */
	Response delete(Integer menu);

	List<Menu> getChildrenByPid(Integer pid);
	
}
