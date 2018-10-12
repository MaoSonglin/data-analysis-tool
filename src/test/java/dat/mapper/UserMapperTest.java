package dat.mapper;

import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import dat.pojo.UserBean;
import dat.util.Md5Util;
import dat.util.StrUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest {
	private static Logger log = Logger.getLogger(UserMapperTest.class);
	@Autowired
	private UserMapper userMapper;
	@Test
	public void testInsert() {
		UserBean userBean = new UserBean();
		userBean.setUsername("123456");
		String randomStr = StrUtil.randomStr(10);
		String pwd = Md5Util.MD5(Md5Util.MD5("123456")+randomStr);
		userBean.setPassword(pwd);
		userBean.setSalt(randomStr);
		userBean.setId(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
		log.info(userBean);
		userMapper.insert(userBean);
	}

	@Test
	public void testFindById() {
		UserBean userBean = userMapper.findById("20180925115009");
		log.info(userBean);
	}

	@Test
	public void testFindByUsername() {
		UserBean userBean = userMapper.findByUsername("123456");
		log.info(userBean);
	}

	@Test
	public void testUpdateById() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteById() {
		fail("Not yet implemented");
	}

	@Test
	public void testListAll(){
		List<UserBean> listAll = userMapper.listAll();
		for (UserBean userBean : listAll) {
			System.err.println(userBean);
		}
	}
}
