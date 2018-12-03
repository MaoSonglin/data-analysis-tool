package dat.service.impl;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
		 
	}

	@Test
	public void testGetJavaType() {
		fail("Not yet implemented");
	}

}
