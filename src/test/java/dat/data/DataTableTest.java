package dat.data;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

public class DataTableTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	@Test
	public void run1(){
		DataTable<DataMap> dataTable = new DataTable<>();
		DataMap dataMap = new DataMap();
		dataMap.put("姓名", "毛松林");
		dataMap.put("age", 10);
		dataMap.put("性别", "男");
		dataTable.add(dataMap);
		
		DataTable<DataMap> select = dataTable.select("\"姓名\",\"age\"", "", null, null);
		System.out.println(select);
	}

}
