package dat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

import dat.util.Md5Util;
import dat.util.StrUtil;



/**
 * Unit test for simple App.
 */
public class AppTest 
{
	
	@Test
	public void testGetOracleConntion() throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		try(Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.62.88:1521:ORCL", "DBL", "Huawei12#$")){
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from ( select row_.*, rownum rownum_ from ( select QB03TWO,QD01_1,QC230,QI19_2,QC18,QI24,QC25,QJ23ZC2016,QJ56_1,QJ85_1,QC14,QC20,QD06,QJ88_5,QB15_3,SMANCHECKTIME,QI01,QC227,QD18,QA20,QB07_1,QB14_1,QB15_5,QC07,QC05_0,QJ101,QA15,QC16,SCITY,QB06,QJ77,QI25,QB08,SRETURNTIME,QC06_1,QJ59,QJ33_3,QC61,QJ12,SBELONGWHEREXQ,QJ87,QB07,QI28_1,QD14,QA19,QC220,QJ83_1,QC06_2,QJ09,QJ55_1,QC38,QC11,QC02,QB16V,SINCOME,QB22,QJ252,QJ90,QJ67,QI07,SPRO,QJ33_1,QB15_2,QJ70,QC62,QB16,QJ33_4,SUSERNAME9,QC65,QA03,QD27,QD28,QB101_1,QD08,QD09,QC06,QC04,QJ80_1,QI27_1,QJ73,QB03XNEW2,QJ61,QC06_3,QI23,SQNQW,QB03ONE,QD25,QA09,QA04,QJ86_1,QJ71,QC223_1,QD29,QA07,QC63,QB06V,QC11_1,QA18,QJ88_2,QC51,QC229,QJ98_1,SCOUNT,QC30,QC221,QC27,QD05,QC50,QC20_2,SPERSON,QB14_2,QC60,QA17,QA01ADD,QC120,QJ58,QC17,QA05,SBWGX,QJ101_1,QJ20,QJ85,QB12,QJ82,QB15A,QB11NUM,QJ251,QC39,QC09,QJ09_1,QJ23_5,QJ11,QJ75,QC224,ZCQ2016TWO,QJ57_1,QC41,QJ99,QD19,QJ80,QJ23_2,QJ88_4,QB18,QA10,TB_TIME,QC225,QB13,QJ33_2,QJ23_1,QB14,QC12,QJ250,ID,QB09,QC31,QD01,QD24,QB10,QB03,QC55,QD21,SNAME,QC10,SBELONGWHERE,QB1610,QC49,QJ79,QC13,QC34,QC06_4,QJ87_1,QA12,QA14,SID,QJ72,QB21,QB11,QC222,QJ100,QC228,QJ57,SPROS,QI19_1,QB101,QB20,QC20_3,QJ73_1,QJ100_1,QJ88_1,QA11,QC52,QJ33,QJ56,QA17_1,QJ92,SBELONGWHERE6,QJ88_3,QD22,QJ23_4,QJ74,QD07,QJ23,QA08,QB15,TB_REGION,QD26,QJ55,QD20,QJ55_2,QI19_4,QJ86,SAREA,QB1630,QA06,QI19_3,QJ79_1,QJ62,QI14,QB04_0,QB15_4,QC24,QA13,QJ13,QB15_1,QB04,QJ83,QC223,QC20_1,QC40,SADDRESS,QC226 from qytj_2016_TB11522018 ) row_ where rownum <= 100) where rownum_ > 1");
			while(rs.next()){
				int int1 = rs.getInt("count");
				System.out.println(int1);
			}
		};
	}
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
		String classpath = System.getProperty("user.dir");
		System.err.println(classpath);
	}
}
