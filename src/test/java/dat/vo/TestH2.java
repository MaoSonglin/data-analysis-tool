package dat.vo;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import lombok.Cleanup;

public class TestH2 {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void run1(){
		try {
			Class.forName("org.h2.Driver");
//			@Cleanup Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:test2","sa","");
			@Cleanup Connection conn = DriverManager.getConnection("jdbc:h2:file:~/.h2/testdb");
			
			String sql = "create table t_user (id Integer, userName varchar(20), userPwd varchar(32))";
			@Cleanup Statement statement = conn.createStatement();
			statement.addBatch("drop table if exists t_user");
			statement.addBatch(sql);
			statement.addBatch("insert into t_user values(1, 'songlin', 'songlin')");
			statement.executeBatch();
			@Cleanup ResultSet rs = statement.executeQuery("select * from t_user");
			if(rs.next()){
				int int1 = rs.getInt(1);
				String username = rs.getString(2);
				String userPwd = rs.getString(3);
				System.out.println(int1+"\t"+username+"\t"+userPwd);
			} 
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void run2()
		    throws Exception {
		   Class.forName("org.h2.Driver");
		   Connection conn = DriverManager.
		       getConnection("jdbc:h2:tcp://localhost/mem:test2", "sa", "");
		   // add application code here
		   Statement stmt = conn.createStatement();
		  
		   stmt.executeUpdate("CREATE TABLE TEST_MEM(ID INT PRIMARY KEY,NAME VARCHAR(255));");
		   stmt.executeUpdate("INSERT INTO TEST_MEM VALUES(1, 'Hello_Mem');");
		   ResultSet rs = stmt.executeQuery("SELECT * FROM TEST_MEM");   
		    while(rs.next()) {   
		     System.out.println(rs.getInt("ID")+","+rs.getString("NAME"));
		    }
		   conn.close();
		 }
}
