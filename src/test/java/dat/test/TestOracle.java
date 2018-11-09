package dat.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;

import org.junit.Test;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.util.OracleSourceMetaData;

public class TestOracle {

	@Test
	public void run1() throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.62.198:1521:ORCL", "DBL", "Huawei12#$");
		
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, null, null, new String[]{"TABLE"});
		int i  = 0;
		while(rs.next()){
			i++;
		}
		System.err.println(i);
		rs.close();
		conn.close();
	}
	@Test
	public void run2(){
		OracleSourceMetaData sourceMetaData = init();
		boolean b = sourceMetaData.testConnection();
		if(b){
			System.err.println("连接成功");
		}else{
			System.err.println("连接失败");
		}
	}
	/**
	 * @return
	 */
	private OracleSourceMetaData init() {
		Source source = new Source();
		source.setDriverClass("oracle.jdbc.driver.OracleDriver");
		source.setUrl("jdbc:oracle:thin:@192.168.62.198:1521:ORCL");
		source.setUsername("DBL");
		source.setPassword("Huawei12#$");
		OracleSourceMetaData sourceMetaData = new OracleSourceMetaData(source);
		return sourceMetaData;
	}
	
	@Test
	public void run3(){
		OracleSourceMetaData oracleSourceMetaData = init();
		List<DataTable> tables = oracleSourceMetaData.getTables();
		System.err.println(tables.size());
	}
	
	@Test
	public void run4(){
		OracleSourceMetaData sourceMetaData = init();
		List<TableColumn> columns = sourceMetaData.getColumns();
		int size = columns.size();
		System.err.println(size);
	}
	
}
