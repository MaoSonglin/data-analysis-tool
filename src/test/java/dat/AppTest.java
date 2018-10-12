package dat;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

import dat.util.Md5Util;
import dat.util.StrUtil;



/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     * @throws Exception 
     */
	@Test
	public void testSqlitejdbc() throws Exception{
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db3");
		System.out.println(conn);
	}
	@Test
	public void testMd5Util(){
		String md5 = Md5Util.MD5("123456");
		System.out.println(md5);
		md5 = "E10ADC3949BA59ABBE56E057F20F883E";
		md5 = "E10ADC3949BA59ABBE56E057F20F883E";
	}
	
	/**
	 * 
	 */
	@Test
	public void testStrUtil(){
		for(int i = 0; i < 10; i++){
			String str = StrUtil.randomStr(10);
			System.out.println(str);
		}
	}
	
	@Test
	public void wirteClassPath(){
		String classpath = System.getProperty("CLASSPATH");
		System.err.println(classpath);
	}
}
