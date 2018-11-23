package dat.domain;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import dat.repos.DataTableRepository;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
public class DataTableTest {

	@Autowired
	private DataTableRepository dataTableRepos;
	
	@Test
	public void test() {
		List<DataTable> list = dataTableRepos.findAll((root,query,cb)->{
			Predicate equal = cb.equal(root.get("id"), "TB1811100733050563");
			Join<DataTable, TableColumn> join = root.join("columns");
			Predicate in = join.get("id").in("FD1810220248517223");
			return cb.or(equal,in);
		});
		
		for (DataTable dataTable : list) {
			System.err.println(dataTable.getName());
		}
	}

	@Autowired
	private JdbcTemplate template;
	@Test
	public void test2() throws Exception{
		Connection conn = getConnection();
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet tableTypes = metaData.getTableTypes();
		showResultSet(tableTypes);
		
		conn.close();
	}
	
	@Test
	public void test3() throws Exception{
		Connection conn = getConnection();
		ResultSet rs = conn.getMetaData().getExportedKeys(null, null, "data_table");
		showResultSet(rs);
		rs.close();
		conn.close();
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection getConnection() throws ClassNotFoundException,
			SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql:///test", "root", "123456");
		return conn;
	}
	
	/**
	 * @param rs
	 * @throws SQLException
	 */
	public void showResultSet(ResultSet rs) throws SQLException {
		int columnCount = rs.getMetaData().getColumnCount();
		while(rs.next()){
			for(int i = 1; i <= columnCount; i++){
				String columnValue = rs.getString(i);
				String columnLabel = rs.getMetaData().getColumnLabel(i);
				System.out.print(columnLabel+"="+columnValue+"\t");
			}
			System.out.println();
		}
		System.out.println("一共"+columnCount);
	}
}
