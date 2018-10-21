package dat.repos;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import dat.domain.Menu;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MenuRepositoryTest {

	@Autowired
	MenuRepository menuRepos;
	
	@Test
	public void testFindAll() {
		List<Menu> menus = menuRepos.findAll();
		for (Menu mune : menus) {
			System.out.println(mune);
			Set<Menu> children = mune.getChildren();
			System.out.println("子菜单个数："+children.size());
		}
	}

	@Test
	public void testFindAllSort() {
		Specification<Menu> specification = new Specification<Menu>(){

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Menu> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.equal(root.get("text"), "配置");
				return predicate;
			}};
		this.menuRepos.findAll(specification);
	}

	@Test
	public void testFindAllByIdIterableOfID() {
		ExampleMatcher matcher = ExampleMatcher.matching();
		matcher.withMatcher("text", ExampleMatcher.GenericPropertyMatchers.contains());
		Menu m = new Menu();
		m.setText("2018");
		List<Menu> list = menuRepos.findAll(Example.of(m,matcher));
		for (Menu menu : list) {
			System.err.println(menu);
		}
	}

	@Test
	public void testSaveAllIterableOfS() {
		fail("Not yet implemented");
	}

	@Test
	public void testFlush() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveAndFlush() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteInBatch() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteAllInBatch() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOne() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindAllExampleOfS() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindAllExampleOfSSort() {
		fail("Not yet implemented");
	}

}
