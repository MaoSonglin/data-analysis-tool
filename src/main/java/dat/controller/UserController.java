package dat.controller;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.User;
import dat.service.UserService;
import dat.util.Constant;
import dat.util.Md5Util;
import dat.vo.PagingBean;
import dat.vo.Response;

@RestController
@RequestMapping("/user")
public class UserController implements Serializable {

	private static final long serialVersionUID = 8366351540866097315L;
	private static final Logger log = LogManager.getLogger(UserController.class);
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public Response add(User user){
		log.info(user);
		return userService.save(user);
	}
	
	@PostMapping("/login")
	public Response login(@ModelAttribute User user,String validateCode,
			Boolean autologin,HttpServletRequest req,HttpServletResponse res ,
			HttpSession session){
		Response response = userService.login(user);
		if(response.getCode() == Constant.SUCCESS_CODE){
			session.setAttribute(Constant.SESSION_USER_BEAN, response.getData());
			log.debug("用户'"+user.getUsername()+"'登录系统");
		}
		return response;
	}

	@RequestMapping("/logout")
	public Response logout(HttpServletRequest req,HttpServletResponse response,HttpSession session){
		// 让session失效
		User user = (User) session.getAttribute(Constant.SESSION_USER_BEAN);
		if(user != null){
			log.debug("用户"+user.getUsername()+"登出系统");
			session.invalidate();
		}
		// 消除自动登录的cookie
//		setAutoLoginCookie(null,req,response,0);
		return new Response(Constant.SUCCESS_CODE);
	}
	
	/**
	 * 添加自动登录的cookie
	 * @param value		cookie值
	 * @param res		发送cookie的响应对象
	 * @param expiry	cookie保存的时长
	 */
	protected void setAutoLoginCookie(String value, HttpServletRequest req,
			HttpServletResponse res,int expiry) {
//		Cookie cookie = new Cookie(Constant.AUTO_LOGIN_COOKIE_NAME,value);
//		cookie.setPath(req.getContextPath());
//		cookie.setDomain(req.getServerName());
//		cookie.setMaxAge(expiry);
//		cookie.setVersion(0);
//		res.addCookie(cookie);
	}
	
	@RequestMapping("/state")
	public Response state(HttpSession session){
		Object user = session.getAttribute(Constant.SESSION_USER_BEAN);
		return new Response(Constant.SUCCESS_CODE,user != null ? "用户已经登录":"用户没有登录",user);
	}
	
	
	@RequestMapping("/page")
	public Response page(PagingBean bean){
		Response response = userService.findByPage(bean);
		return response;
	}

	@RequestMapping(value={"/{id}"},method=RequestMethod.DELETE)
	public Response remove(@PathVariable String id){
		return userService.remove(id);
	}
	
	@RequestMapping(value={"/update"},method=RequestMethod.PUT)
	public Response update(User user){
		log.info(user);
		Response response = userService.save(user);
		return response;
	}
	
	@RequestMapping("/modify")
	public Response modify(String oldpwd,String password,HttpSession session,HttpServletResponse response){
		User user = (User) session.getAttribute(Constant.SESSION_USER_BEAN);
		if(user != null){
			String md5 = Md5Util.MD5(Md5Util.MD5(oldpwd)+user.getSalt());
			if(md5.equals(user.getPassword())){
				user.setPassword(password);
				Response save = userService.save(user);
				return save;
			}else{
				return new Response(Constant.ERROR_CODE,"原密码错误");
			}
		}
		return new Response(Constant.ERROR_CODE,"您还没有登录");
	}
}
