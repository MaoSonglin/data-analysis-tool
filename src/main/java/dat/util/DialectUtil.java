package dat.util;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;

public class DialectUtil {
	
	public static Dialect getDialect(String dataBaseName){
		if("oracle".equalsIgnoreCase(dataBaseName)){
			return new Oracle10gDialect();
		}
		else if("mysql".equalsIgnoreCase(dataBaseName)){
			return new MySQLDialect();
		}
		return null;
	}
}
