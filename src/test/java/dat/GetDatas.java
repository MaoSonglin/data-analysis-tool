package dat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetDatas {
	private static Logger logger = LoggerFactory.getLogger(GetDatas.class);
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection oracleConn() throws SQLException{
		return DriverManager.getConnection("jdbc:oracle:thin:@192.168.62.198:1521:ORCL", "DBL", "Huawei12#$");
	}
	
	public static Connection mysqlConn() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/dat_db?useUnicode=true&characterEncoding=utf-8", "root", "123456");
	}
	
	@Test
	public void test() throws SQLException{
		try(Connection conn = oracleConn();
				PreparedStatement ps = conn.prepareStatement("select DB_CODE,DB_NAME,DB_DEC from DB_INFO_REG");
				ResultSet rs = ps.executeQuery();){
			String insertSourceSql = "INSERT INTO datasource (ID,name,chinese,`desc`,driver_class,URL,u_name,pwd) VALUES(?,?,?,?,?,?,?,?)";
			logger.info(insertSourceSql);
			try (Connection mysqlConn = mysqlConn();
					PreparedStatement mysqlPs = mysqlConn.prepareStatement(insertSourceSql);) {
				mysqlConn.setAutoCommit(false);
				List<String> ids = new ArrayList<>();
				while(rs.next()){
					String DB_CODE = rs.getString("DB_CODE");
					String db_name = rs.getString("DB_NAME");
					String db_dec = rs.getString("DB_DEC");
					mysqlPs.setString(1, DB_CODE);
					mysqlPs.setString(2, db_name);
					mysqlPs.setString(3, /*rs.getString("DB_CN_NAME")*/null);
					mysqlPs.setString(4, db_dec);
					mysqlPs.setString(5, "oracle.jdbc.driver.OracleDriver");
					mysqlPs.setString(6, "jdbc:oracle:thin:@192.168.62.198:1521:ORCL");
					mysqlPs.setString(7, "DBL");
					mysqlPs.setString(8, "Huawei12#$");
					mysqlPs.addBatch();
					
					ids.add(DB_CODE);
					
				}
				mysqlPs.executeBatch();
				for (String id : ids) {
					getTables(conn,mysqlConn, id);
				}
				mysqlConn.commit();
			}
		}
	}

	/**
	 * @param mysqlConn
	 * @param DB_CODE
	 * @throws SQLException
	 */
	private void getTables(Connection conn,Connection mysqlConn, String DB_CODE)
			throws SQLException {
		try(PreparedStatement ps2 = conn.prepareStatement("select TB_CODE,TB_NAME,TB_CN_NAME,"
				+ "TB_DEC,TB_NATURE,TB_STATE,REMARKE,DB_CODE,"
				+ "SR_DB_TB_COM_CODE,TB_OPERATOR,TB_REG_TIME,"
				+ "TB_INVE from tb_info_reg where DB_CODE=?")){
			ps2.setString(1, DB_CODE);
			String sql = "insert into data_table (id,add_time,chinese,`desc`,name,state,data_source_id) values(?,now(),?,?,?,?,?)";
			logger.debug(sql);
			try(ResultSet rs2 = ps2.executeQuery();PreparedStatement ps = mysqlConn.prepareStatement(sql)){
				List<String> ids = new ArrayList<>();
				while(rs2.next()){
					String tb_code = rs2.getString("TB_CODE");
					ps.setString(1, tb_code);
					ps.setString(2, rs2.getString("TB_CN_NAME"));
					ps.setString(3, rs2.getString("TB_DEC"));
					ps.setString(4, rs2.getString("TB_NAME")+"_"+tb_code);
					ps.setInt(5, 1);
					ps.setString(6, DB_CODE);
					ids.add(tb_code);
					ps.addBatch();
				}
				ps.executeBatch();
				for (String tb_code : ids) {
					try(PreparedStatement ps3 = conn.prepareStatement("select DF_CODE,DF_NAME,DF_CN_NAME,DF_DATA_TYPE,DF_LENGTH,REMARK from DF_INFO_REG where TB_CODE=?")){
						ps3.setString(1, tb_code);
						String s = "INSERT INTO table_column (id, add_time, chinese, column_name,"
								+ " column_size,remarks, state, type_name, data_table_id)"
								+ " VALUES (?,now(),?,?,?,?,?,?,?)";
						logger.debug(s);
						try (ResultSet rs = ps3.executeQuery();PreparedStatement ps4 = mysqlConn.prepareStatement(s)){
							int i = 0;
							while(rs.next()){
								String df_code = rs.getString("DF_CODE");
								String df_name = rs.getString("DF_NAME");
								String df_cn_name = rs.getString("DF_CN_NAME");
								String df_data_type = rs.getString("DF_DATA_TYPE");
								String df_length = rs.getString("DF_LENGTH");
								String remark = rs.getString("REMARK");
								ps4.setString(1, df_code);
								ps4.setString(2, df_cn_name);
								ps4.setString(3, df_name);
								ps4.setString(4, df_length);
								ps4.setString(5, remark);
								ps4.setInt(6, 1);
								ps4.setString(7, df_data_type);
								ps4.setString(8, tb_code);
								ps4.addBatch();
								if(++i==1000){
									ps4.executeBatch();
									ps4.clearBatch();
									i=0;
								}
							}
							System.err.println();
							ps4.executeBatch();
						} 
					}
				}
			}
		}
	}

	
	@Test
	public void test2() throws SQLException{
		try (Connection oracleConn = oracleConn();
				PreparedStatement ps = oracleConn.prepareStatement("select TB_CODE,TB_NAME from tb_info_reg");
				ResultSet rs = ps.executeQuery();){
			try(Connection mysqlConn = mysqlConn();
					PreparedStatement mps = mysqlConn.prepareStatement("update data_table set `name` = ? where id=?")){
				while(rs.next()){
					mps.setString(1, rs.getString("TB_NAME")+"_"+rs.getString("TB_CODE"));
					mps.setString(2, rs.getString("TB_CODE"));
					mps.addBatch();
				}
				mps.executeBatch();
			}
		} 
	}

}
// INSERT INTO `test`.`table_column` (`id`, `add_time`, `chinese`, `column_name`, `column_size`,`remarks`, `state`, `type_name`, `data_table_id`) VALUES (?,now(),?,?,?,?,1,?);
