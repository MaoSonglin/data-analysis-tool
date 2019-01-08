package dat.vo;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Test;

public class ClassifyFormulaTest {


	@Test
	public void setUp() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dat?useUnicode=true&CharacterEncoding=utf-8", "root", "123456");
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM virtual_column where `name` = ?");
				) {
			ps.setObject(1, "test");
			try(ResultSet rs = ps.executeQuery();){
				if(rs.next()){
					String formula = rs.getString("formula");
					boolean b = ClassifyFormula.isClassifyFormula(formula);
					if(b){
						ClassifyFormula fromString = ClassifyFormula.getFromString(formula);
						System.out.println(fromString);
					}
				}
			}
		} 
	}
	
	@After
	public void close() throws SQLException{
	}
	
	@Test
	public void testIsClassifyFormula() throws SQLException {
	}

	@Test
	public void testGetFromString() {
		fail("Not yet implemented");
	}

}
