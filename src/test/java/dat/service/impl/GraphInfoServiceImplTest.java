package dat.service.impl;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.Source;
import dat.repos.DsRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GraphInfoServiceImplTest {
	
	@Autowired
	DsRepository dsRepos;
	
	@Autowired
	GraphInfoServiceImpl graphInfoService;
	
	@Test
	public void testSave() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetById() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTemplate() {
		List<Source> findByName = dsRepos.findByName("测试读取字段");
		for (Source source : findByName) {
			JdbcTemplate tempate = graphInfoService.getTemplate(source);
			System.err.println(tempate);
			tempate.query("select count(1) from t_doc_info", (rs,i)->{
				Object object = rs.getObject(1);
				System.out.println(object);
				return null;
			});
		}
	}

	@Test
	public void testGetJavaType() {
		fail("Not yet implemented");
	}

}
