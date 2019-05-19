package dat.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.ForeignKeyInfo;
import dat.domain.TableColumn;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ForeignKeyInfoRepositoryTest {

	@Autowired
	ApplicationContext context;
	
	@Autowired
	ForeignKeyInfoRepository foreignKeyInfoRepos;
	
	@Autowired
	TableColumnRepository tableColumnRepos;

	
	@Test
	public void testExc(){
		boolean existsById = foreignKeyInfoRepos.existsById(10l);
		System.out.println(existsById);
	}
	
	@Test
	public void testFindById() {
		Page<TableColumn> list = tableColumnRepos.findAll(PageRequest.of(0, 10));
		List<String> ids = new ArrayList<>();
		for (TableColumn tableColumn : list) {
			ids.add(tableColumn.getId());
		}
		
		Set<ForeignKeyInfo> set = foreignKeyInfoRepos.findByTableColumnIds(ids);
		System.out.println(set);
		
//		List<ForeignKeyInfo> list2 = foreignKeyInfoRepos.findAllById(list);
//		System.out.println(list2);
	}

}
