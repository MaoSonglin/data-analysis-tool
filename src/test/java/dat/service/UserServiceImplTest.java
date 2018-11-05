package dat.service;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import dat.domain.Response;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.VirtualColumnRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

	@Autowired
	private ApplicationContext context;
	@Autowired
	private UserService userService;
	@Test
	public void testRegister() {
		Response response = userService.register("songlin123", "980352644");
		System.out.println(response);
	}

	@Test
	public void testConnection() throws Exception{
		
		DataSource ds = context.getBean(DataSource.class);
		System.err.println(ds);
		Connection conn = ds.getConnection();
		System.err.println(conn);
		conn.close();
	}
	
	@Test
	public void test2(){
		VirtualColumn vc = new VirtualColumn();
		vc.setId();
		vc.setName("test_virtual_column");
		VirtualTable vt = new VirtualTable();
		vt.setName("test_virtual_table");
		vt.generateId();
		vc.setTable(vt);
		VirtualColumnRepository vtRepos = context.getBean(VirtualColumnRepository.class);
		vtRepos.save(vc);
	}
	
}
