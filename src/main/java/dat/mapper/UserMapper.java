package dat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import dat.pojo.UserBean;

@Mapper
public interface UserMapper {

	public void insert(UserBean user);
	
	public UserBean findById(String id);
	
	public UserBean findByUsername(String username);
	
	/**
	 * 更新指定ID的用户，在user参数中的封装了更新后的user对象，但是user的id不应该改变
	 * @param user
	 */
	public void updateById(UserBean user);
	
	/**
	 * 删除指定id的用户
	 * @param id
	 */
	public void deleteById(String id);
	
	/**
	 * 列出所有的用户
	 * @return
	 */
	public List<UserBean> listAll();

	/**
	 * 通过一个过滤条件查询用户，filter可以是用户ID，用户名等信息，查询方式采用模糊匹配
	 * @param filter
	 * @return
	 */
	public List<UserBean> selectByFilter(String filter);
}
