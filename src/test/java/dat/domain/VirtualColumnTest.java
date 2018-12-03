package dat.domain;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.repos.TableColumnRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class VirtualColumnTest {
	
	@Autowired
	TableColumnRepository repos;
	@Test
	public void test() {
		Set<String> s = new HashSet<>();
		s.add("12323");
	}

}
