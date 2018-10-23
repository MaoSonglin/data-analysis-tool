package dat.service.impl;

import java.io.Serializable;

import dat.service.UserService;

//@Service
@Deprecated
public abstract class UserServiceImpl implements UserService, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -446077888220956770L;
	/*
	
	@Resource(name="userMapper")
	private UserMapper userMapper;

	@Transactional
	public Response register(String username, String password) {
		// 存放返回信息的javabean
		Response response = new Response();
		// 查看用户名是否已经存在
		UserBean userBean = userMapper.findByUsername(username);
		if(userBean != null){
			response.setCode(Constant.ERROR_CODE);
			response.setMessage("用户名已存在！");
			return response;
		}
		// 生成一个随机字符串用来加密密码
		String salt = StrUtil.randomStr(Constant.SALT_LENGTH);
		// 重新生成加密后的密码
		password = Md5Util.MD5(Md5Util.MD5(password)+salt);
		// 构造一个用户javabean
		UserBean user = new UserBean();
		user.setId(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
		user.setUsername(username);
		user.setPassword(password);
		user.setSalt(salt);
		// 保存到数据库中
		userMapper.insert(user);
		// 设置响应消息
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("添加成功！");
		response.setData(user);
		return response;
	}

	public Response login(UserBean user) {
		// 根据用户名操作用户
		UserBean userBean = userMapper.findByUsername(user.getUsername());
		if(userBean==null){
			return new Response(Constant.ERROR_CODE,"用户名不存在！");
		}
		// 比较用户密码是否一致
		String password = Md5Util.MD5(Md5Util.MD5(user.getPassword())+userBean.getSalt());
		if(password.equals(userBean.getPassword())){
			return new Response(Constant.SUCCESS_CODE,"登录成功！",userBean);
		}else{
			return new Response(Constant.ERROR_CODE,"密码错误！");
		}
	}

	public List<UserBean> listAll1() {
		List<UserBean> all = userMapper.listAll();
		return all;
	}

	public List<UserBean> listAll1( String filter) {
		if(filter!=null){
			if(!filter.startsWith("%"))
				filter = "%"+filter;
			if(!filter.endsWith("%"))
				filter = filter + "%";
		}
		List<UserBean> list = userMapper.selectByFilter(filter);
		return list;
	}

	@Transactional
	public Response remove(String id) {
		UserBean userBean = userMapper.findById(id);
		if(userBean == null)
			return new Response(Constant.ERROR_CODE,String.format("ID为‘%d’的用户不存在！", id),id);
		userMapper.deleteById(id);
		return new Response(Constant.SUCCESS_CODE,"删除成功！",id);
	}

	public Response save(UserBean user) {
		String id = user.getId();
		UserBean userBean = userMapper.findById(id);
		if(userBean == null) {
			return new Response(Constant.ERROR_CODE,String.format("ID为‘%d’的用户不存在！", id),id);
		}
		// 生成一个随机字符串用来加密密码
		String salt = StrUtil.randomStr(Constant.SALT_LENGTH);
		String password = user.getPassword();
		// 重新生成加密后的密码
		password = Md5Util.MD5(Md5Util.MD5(password)+salt);
		user.setPassword(password);
		userMapper.updateById(user);
		return new Response(Constant.SUCCESS_CODE,String.format("修改成功"),user);
	}

	@Override
	public Response login(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response save(User user) {
		// TODO Auto-generated method stub
		return null;
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
*/
}
