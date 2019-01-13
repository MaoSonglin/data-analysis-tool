package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dat.domain.Menu;
import dat.domain.User;
import dat.repos.MenuRepository;
import dat.service.MenuService;
import dat.util.Constant;
import dat.vo.Response;

@Service
@Transactional
public class MenuServiceImplWithJpa implements MenuService {
	
	private static Logger logger = LoggerFactory.getLogger(MenuServiceImplWithJpa.class);
	
	@Autowired
	MenuRepository menuRepos;
	@Override
	public Response list(Integer menuid) {
		// 返回给客户端的对象
		Response response = new Response();
		if(menuid==null){
			// 查找出顶级菜单项
			List<Menu> list = menuRepos.findByPidIsNull();
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
	public List<Menu> getChildrenByPid(Integer pid) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpSession session = requestAttributes.getRequest().getSession();
		User user = (User) session.getAttribute(Constant.SESSION_USER_BEAN);
		if(user == null) {
			logger.debug("用户未登录系统");
			return new ArrayList<>();
		}
		logger.debug(user.toString());
		List<Menu> menus = menuRepos.findAll((root,query,cb)->{
			Predicate to = cb.lessThanOrEqualTo(root.get("level"), user.getRole());
			if(pid == null){
				Predicate predicate = cb.isNull(root.get("pid"));
				Predicate and = cb.and(to,predicate);
				return and;
			}else{
				Predicate equal = cb.equal(root.get("pid"), pid);
				Predicate and = cb.and(to,equal);
				return and;
			}
		});
		logger.debug(menus.toString());
		return menus;
	}

}
