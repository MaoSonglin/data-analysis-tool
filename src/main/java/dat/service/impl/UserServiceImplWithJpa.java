package dat.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.User;
import dat.repos.UserRepository;
import dat.service.UserService;
import dat.util.Constant;
import dat.util.Md5Util;
import dat.util.StrUtil;
import dat.vo.Response;

@Service
@Transactional
public class UserServiceImplWithJpa implements UserService {

	@Autowired
	private UserRepository userRepos;
	
	@Override
	public Response register(String username, String password) {
		// 存放返回信息的javabean
		Response response = new Response();
		// 查看用户名是否已经存在
		boolean userBean = userRepos.findByUsername(username).isEmpty();
		if(!userBean){
			response.setCode(Constant.ERROR_CODE);
			response.setMessage("用户名已存在！");
			return response;
		}
		// 生成一个随机字符串用来加密密码
		String salt = StrUtil.randomStr(Constant.SALT_LENGTH);
		// 重新生成加密后的密码
		password = Md5Util.MD5(Md5Util.MD5(password)+salt);
		// 构造一个用户javabean
		User user = new User();
		user.setId(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
		user.setUsername(username);
		user.setPassword(password);
		user.setSalt(salt);
		// 保存到数据库中
		userRepos.save(user);
		// 设置响应消息
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("添加成功！");
		response.setData(user);
		return response;
	}

	@Override
	public Response login(User user) {
		// 根据用户名操作用户
		List<User> users = userRepos.findByUsername(user.getUsername());
		if(users.isEmpty()){
			return new Response(Constant.ERROR_CODE,"用户名不存在！");
		}
		User userBean = users.get(0);
		// 比较用户密码是否一致
		String password = Md5Util.MD5(Md5Util.MD5(user.getPassword())+userBean.getSalt());
		if(password.equals(userBean.getPassword())){
			return new Response(Constant.SUCCESS_CODE,"登录成功！",userBean);
		}else{
			return new Response(Constant.ERROR_CODE,"密码错误！");
		}
	}

	@Override
	public List<User> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> listAll(String filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response remove(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response save(User user) {
		// TODO Auto-generated method stub
		return null;
	}

}
