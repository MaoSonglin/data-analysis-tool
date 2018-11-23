package dat.service;

import java.util.List;

import dat.domain.User;
import dat.vo.Response;

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
	Response login(User user);

	/**
	 * @return
	 */
	@Deprecated
	List<User> listAll();

	List<User> listAll(String filter);

	Response remove(String id);

	Response save(User user);
}
