package dat.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.pojo.Mune;
import dat.pojo.Response;
import dat.service.MenuService;
import dat.util.Constant;

@RestController
@RequestMapping("/menu")
public class MuneController {

	@Resource(name="menuService")
	private MenuService menuService;
	
	/**
	 * 返回指定ID的菜单项以及该菜单下的子菜单，如果menuid为0，则返回所有菜单项及其子菜单
	 * @param menuid
	 * @return
	 */
	@RequestMapping(value={"/{menuid}","/"},method=RequestMethod.GET)
	public Response list(@PathVariable(required=false) Integer menuid){
		Response response;
		try {
			response = menuService.list(menuid);
		} catch (Exception e) {
			e.printStackTrace();
			response = new Response(Constant.ERROR_CODE,e.getMessage());
		}
		return response;
	}
	
	/**
	 * 添加菜单的接口
	 * @param menu
	 * @return
	 */
	@RequestMapping(value="/",method=RequestMethod.POST)
	public Response menu(@ModelAttribute Mune menu){
		Response response = menuService.addMenu(menu);
		
		return response;
	}
	
	@RequestMapping(value="/",method=RequestMethod.PUT)
	public Response update(@ModelAttribute Mune menu){
		return menuService.update(menu);
	}
	
	@RequestMapping(value="/{id}",method=RequestMethod.DELETE)
	public Response delete(@PathVariable Integer id){
		Response response = menuService.delete(id);
		return response;
	}
}
