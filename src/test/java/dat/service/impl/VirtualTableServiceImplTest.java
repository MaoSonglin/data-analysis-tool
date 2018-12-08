package dat.service.impl;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dat.domain.VirtualTable;
import dat.vo.TableData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@SuppressWarnings("all")
public class VirtualTableServiceImplTest {

	@Autowired
	private DataTableServiceImpl VirtualTableService;
	
	@Autowired
	VirtualTableServiceImpl virtualTableServiceImpl;
	@Test
	public void testGetById() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVirtualColumns() {
		fail("Not yet implemented");
	}

	@Test
	public void testAdd() {
		VirtualTable t = new VirtualTable();
		t.setId("VT1811111042261129");
		System.out.println(t.getId());
		Map<String, List<String>> data = this.virtualTableServiceImpl.listData(t);
		System.out.println(data);
	}

	@Test
	public void testGetData() {
		TableData tableData = new TableData("TB1811100733036748",10,0);
		List<Map<String,String>> values = VirtualTableService.getValues(tableData);
		values.forEach(elem->{
			System.out.println(elem);
		});
	}

}
