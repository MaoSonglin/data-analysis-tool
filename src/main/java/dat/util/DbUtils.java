package dat.util;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtils {

	static {
		try {
			Class.forName("org.h2.Driver");
			DriverManager.getConnection("jdbc:h2:file:~/.h2/testdb", "sa", "sa");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
}
