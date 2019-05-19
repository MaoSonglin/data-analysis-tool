package dat.data;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import dat.domain.ForeignKeyInfo;
import dat.domain.TableColumn;
import dat.repos.VirtualTableRepository;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TempTableAdapterTest {

	@Autowired
	private VirtualTableRepository tableRepos;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ApplicationContext context;
	
	@Test
	public void testBuild() throws JsonProcessingException {
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select * from t_user");
		while(rowSet.next()){
			System.out.println("next");
		}
		System.out.println(rowSet.isLast());
		System.out.println(rowSet.isAfterLast());
		if( ! rowSet.isAfterLast() && rowSet.next()){
			System.out.println("success");
		}
		System.out.println("finish");
	}
	
	@Test
	public void getForeignKey() throws SQLException{
		DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
//		ResultSet rs = metaData.getExportedKeys(null, null, "virtual_column");
		ResultSet rs = metaData.getImportedKeys(null, null, "relevance");
		ResultSetMetaData metaData2 = rs.getMetaData();
		int columnCount = metaData2.getColumnCount();
		while(rs.next()){
			for(int i = 1; i <= columnCount; i++){
				String columnLabel = metaData2.getColumnLabel(i);
				System.out.print(columnLabel+"=");
				Object object = rs.getObject(i);
				System.out.print(object);
				System.out.print("\t");
			}
			System.out.println();
		}
		System.err.println("finish");
	}
	
	@Test
	public void tset2() throws SQLException{
		DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
		ResultSet rs =  metaData.getTables(null, "%", null, new String[]{"TABLE","VIEW"});
			String tableName = rs.getString("table_name");
		ResultSet resultSet = metaData.getImportedKeys(null, null, tableName);
				String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
				String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
				TableColumn column1 = new TableColumn();
				TableColumn column2 = new TableColumn();
				column1.setColumnName(fkColumnName);
				column2.setColumnName(pkColumnName);
				ForeignKeyInfo foreignKeyInfo = new ForeignKeyInfo();
				foreignKeyInfo.setForeignKey(column1);
				foreignKeyInfo.setReferencedColumn(column2);
				System.out.println(foreignKeyInfo);
	}
	

}
