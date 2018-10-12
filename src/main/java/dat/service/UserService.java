package dat.service;

import java.util.List;

import dat.pojo.Response;
import dat.pojo.UserBean;

public interface UserService {
	
	/**
	 * 注册新用户
	 * @param username	新用户的用户名
	 * @param password	密码
	 * @return			是否成功的信息
	 */
	Response register(String username,String password);
	
	/**
	 * 登录
	 * @param user
	 * @return
	 */
	Response login(UserBean user);

	/**
	 * @return
	 */
	@Deprecated
	List<UserBean> listAll();

	List<UserBean> listAll(String filter);

	Response remove(String id);

	Response save(UserBean user);
}
