package dat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.Menu;
import dat.pojo.Mune;
import dat.pojo.Response;
import dat.repos.MenuRepository;
import dat.util.Constant;

@Service
@Transactional
public class MenuServiceImplWithJpa implements MenuService {
	
	@Autowired
	MenuRepository menuRepos;
	@Override
	public Response list(Integer menuid) {
		// 返回给客户端的对象
		Response response = new Response();
		if(menuid==null){
			// 查找出顶级菜单项
			List<Menu> list = menuRepos.findByParentIsNull();
			// 封装结果
			response.setCode(Constant.SUCCESS_CODE);
			response.setMessage("查询成功");
			response.setData(list);
		}else{
			try {
				// 通过id查找菜单项
				Optional<Menu> optional = menuRepos.findById(menuid);
				// 获取菜单项
				Menu menu = optional.get();
				response.setCode(Constant.SUCCESS_CODE);
				response.setMessage("查询成功");
				response.setData(menu);
			} catch (Exception e) {
				response.setCode(Constant.ERROR_CODE);
				response.setMessage(String.format("ID为“%d”的菜单项不存在！", menuid));
			}
			
		}
		return response;
	}

	@Override
	public Response addMenu(Menu menu) {
		Menu save = menuRepos.save(menu);
		return new Response(Constant.SUCCESS_CODE,"添加成功",save);
	}

	@Override
	public Response update(Menu menu) {
		Response response = new Response(Constant.ERROR_CODE,"保存成功",menuRepos.save(menu));
		return response;
	}

	@Override
	public Response delete(Integer menu) {
		
		try {
			Menu m = menuRepos.findById(menu).get();
			menuRepos.delete(m);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"ID不存在",menu);
		}
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}

	@Override
	public Response addMenu(Mune menu) {
		throw new RuntimeException("该方法没有实现");
	}

	@Override
	public Response update(Mune menu) {
		return addMenu(menu);
	}

}
