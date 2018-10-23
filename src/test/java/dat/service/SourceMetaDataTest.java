package dat.service;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.util.SourceMetaData;
import dat.util.SourceMetaData.ColumnAttrName;

public class SourceMetaDataTest {

	private Source source;

	@Before
	public void init(){
		source = new Source();
		source.setDatabaseName("MySQL");
		source.setDriverClass("com.mysql.jdbc.Driver");
		source.setUrl("jdbc:mysql://39.106.26.170:3306/iqaa");
		source.setUsername("root");
		source.setPassword("123456");
	}
	@Test
	public void testGetSourceMetaData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTables() {
		SourceMetaData sourceMetaData = SourceMetaData.getSourceMetaData(source);
		List<TableColumn> columns = sourceMetaData.getColumns();
		for (TableColumn tableColumn : columns) {
			System.out.println(tableColumn);
		}
	}

	@Test
	public void testGetColumnOfTable() {
		SourceMetaData metaData = SourceMetaData.getSourceMetaData(source);
		List<DataTable> tables = metaData.getTables();
		for (DataTable dataTable : tables) {
			List<TableColumn> columns = metaData.getColumnOfTable(dataTable);
			for (TableColumn tableColumn : columns) {
				System.out.println(tableColumn.getColumnName());
			}
			break;
		}
	}

	@Test
	public void testGetColumns() {
		ColumnAttrName x = ColumnAttrName.CHAR_OCTET_LENGTH;
		System.out.println(x);
	}
	
	@Test
	public void testSQLite() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection("jdbc:mysql://39.106.26.170:3306/iqaa","root","123456");
		DatabaseMetaData metaData = conn.getMetaData();
		
		ResultSet columns = metaData.getColumns(conn.getCatalog(), "%", "%", null);
		ResultSetMetaData md = columns.getMetaData();
		int columnCount = md.getColumnCount();
		while(columns.next()){
			for (int i = 0; i < columnCount; i++) {
				String columnLabel = md.getColumnLabel(i+1);
				System.out.println(columnLabel);
//				String value = columns.getString(columnLabel);
//				System.out.print(columnLabel+"=>"+value+"\t");
			}
			break;
//			System.out.println();
		}
		columns.close();
		conn.close();
	}
	
	@Test
	public void testMysql() throws Exception{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db3");
		DatabaseMetaData metaData = conn.getMetaData();
		
		ResultSet columns = metaData.getColumns(conn.getCatalog(), "%", "datasource", null);
		ResultSetMetaData md = columns.getMetaData();
		int columnCount = md.getColumnCount();
		while(columns.next()){
			for (int i = 0; i < columnCount; i++) {
				String columnLabel = md.getColumnLabel(i+1);
				System.out.println(columnLabel);
//				String value = columns.getString(columnLabel);
//				System.out.print((i+1)+" "+columnLabel+"=>"+value+"\t");
			}
			break;
		}
		columns.close();
		conn.close();
	}
	@Test
	public void testOracle() throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.62.88:1521:ORCL","DBL","Huawei12#$");
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet columns = metaData.getColumns(conn.getCatalog(), "%", "WWV_MIG_OLB_OLT_ITEM", null);
		ResultSetMetaData md = columns.getMetaData();
		int columnCount = md.getColumnCount();
		while(columns.next()){
			for (int i = 0; i < columnCount; i++) {
				String columnLabel = md.getColumnLabel(i+1);
				System.out.println(columnLabel);
//				String value = columns.getString(columnLabel);
//				System.out.print((i+1)+" "+columnLabel+"=>"+value+"\t");
			}
			break;
		}
		columns.close();
		conn.close();
	}

}
