package dat.repos;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.DataTable;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class Data {

	@Autowired
	DataTableRepository repos;
	@Test
	public void test() {
		List<String> ids = new ArrayList<>();
		ids.add("VT1811120143233449");
		List<DataTable> tableIds = repos.findByVirtualTableIds(ids);
		for (DataTable dataTable : tableIds) {
			System.out.println(dataTable);
		}
	}

}
