package dat.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.domain.User;
import dat.repos.UserRepository;
import dat.service.UserService;
import dat.util.Constant;
import dat.util.Md5Util;
import dat.util.StrUtil;
import dat.vo.PagingBean;
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
		user.setCreateTime(new Date());
		user.setState(Constant.ACTIVATE_SATE);
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
		User user = userRepos.findById(id).orElse(null);
		if(user!=null){
			userRepos.deleteById(id);
			return new Response(Constant.SUCCESS_CODE,"删除成功");
		}
		return new Response(Constant.ERROR_CODE,"ID不存在");
	}

	@Override
	public Response save(User user) {
		// 1.用户ID为null说明user是新建对象还没有保存到数据库
		// 2.验证用户名是否在数据库中已经存在
		boolean existsById = user.getId() != null && userRepos.existsById(user.getUsername());
		if(existsById){
			return new Response(Constant.ERROR_CODE,"用户名已存在");
		}
		// 获取用户密码
		String password = user.getPassword();
		// 生成一个随机字符串用来加密密码
		String salt = StrUtil.randomStr(Constant.SALT_LENGTH);
		// 重新生成加密后的密码
		password = Md5Util.MD5(Md5Util.MD5(password)+salt);
		if(user.getId() == null){
			// 为新用户生成一个ID
			String id = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
			user.setId(id);
		}
		// 设置用户的相关属性
		user.setSalt(salt);
		user.setPassword(password);
		user.setState(Constant.ACTIVATE_SATE);
		
		// 保存用户
		User save = userRepos.save(user);
		
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

	@Override
	public Response findByPage(PagingBean bean) {
		PageRequest pageRequest = PageRequest.of(bean.getCurPage()-1, bean.getPageSize());
		Page<User> page = userRepos.findAll((root,query,cb)->{
			Path<Integer> path = root.get("state");
			Predicate notEqual = cb.notEqual(path, Constant.DELETE_STATE);
			String keyword = bean.getKeyword();
			if(!StringUtils.isEmpty(keyword)){
				Predicate like = cb.like(root.get("username"), "%"+bean.getKeyword()+"%");
				return cb.and(notEqual,like);
			}
			return notEqual;
		},pageRequest);
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		return res;
	}

}
