package dat.repos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.DataTable;
import dat.domain.TableColumn;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DsRepositoryTest {
	
	@Autowired
	DsRepository dsRepos;
	
	@Autowired
	TableColumnRepository tabRepos;
	
	@Test
	public void testWithTestBean() {
		TableColumn column = new TableColumn();
		column.generateId();
		column.setColumnName("测试保存字段是否会自动保存数据表");
		DataTable dataTable = new DataTable();
		dataTable.generateId();
		dataTable.setName("测试数据表是否自动保存");
		column.setDataTable(dataTable);
		tabRepos.save(column);
	}

}
