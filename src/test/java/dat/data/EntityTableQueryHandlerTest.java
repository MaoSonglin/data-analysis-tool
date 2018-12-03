package dat.data;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.DataTable;
import dat.domain.TableColumn;
import dat.repos.DataTableRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EntityTableQueryHandlerTest {
	
	@Autowired
	DataTableRepository dataTableRepos;
	private DataTable dataTable;
	private EntityTableQueryHandler queryHandler;
	
	@Before
	public void setUp(){
		dataTable = dataTableRepos.findById("TB1811100733201760").get();
		queryHandler = new EntityTableQueryHandler(dataTable);
	}
	
	@Test
	public void testEntityTableQueryHandlerDataTable() {
		fail("Not yet implemented");
	}

	@Test
	public void testEntityTableQueryHandlerDataTableSource() {
		fail("Not yet implemented");
	}

	@Test
	public void testList() throws Exception {
		List<TableColumn> list = dataTable.getColumns();
		queryHandler.query(list);
		Map<String, List<String>> result = queryHandler.list();
		System.out.println(result.size());
	}

	@Test
	public void testQueryListOfString() {
		fail("Not yet implemented");
	}

	@Test
	public void testQueryCollectionOfTableColumn() {
		fail("Not yet implemented");
	}

	@Test
	public void testJoin() {
		fail("Not yet implemented");
	}

	@Test
	public void testLimit() throws Exception {
//		List<TableColumn> list = dataTable.getColumns();
//		queryHandler.query(list);
//		Map<String, List<String>> result = queryHandler.list();
//		System.out.println(result.size());
//		QueryHandler limitHandler = queryHandler.limit(10, 29);
//		limitHandler.query(list);
//		Map<String, List<String>> map = limitHandler.list();
//		Object json = JSON.toJSON(map);
//		FileOutputStream fos = new FileOutputStream("map.json");
//		fos.write(json.toString().getBytes());
//		fos.close();
	}

}
