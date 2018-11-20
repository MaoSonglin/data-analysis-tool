package dat.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.pool.DruidDataSource;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.repos.DataTableRepository;
import dat.repos.DsRepository;
import dat.repos.TableColumnRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TurnOracleData {

	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private DsRepository dsRepos;
	
	@Autowired
	private DataTableRepository dataTableRepos;
	
	@Autowired
	private TableColumnRepository tableColumnRepos;
	
	private DruidDataSource dataSource;
	
	private QueryRunner runner;

	@Before
	public void init() throws Exception{
		dataSource = new DruidDataSource();
		dataSource.setUrl("jdbc:oracle:thin:@192.168.62.198:1521:ORCL");
		dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		dataSource.setUsername("DBL");
		dataSource.setPassword("Huawei12#$");
		dataSource.getConnection();
		
		runner = new QueryRunner(dataSource);
	}
	
	@Test
	@Transactional
	public void test() throws Exception {
		String sql = "select DB_CODE,DB_NAME,DB_DEC from DB_INFO_REG where rownum < 2";
		logger.info("execute sql => "+sql);
		List<Source> sources = runner.query(sql, rs->{
			List<Source> list = new ArrayList<>();
			while(rs.next()){
				Source source = new Source();
				source.setName(rs.getString("DB_NAME"));
				source.setChinese(rs.getString("DB_CN_NAME"));
				source.setDesc(rs.getString("DB_DEC"));
				source.setUrl(dataSource.getUrl());
				source.setDriverClass(dataSource.getDriverClassName());
				source.setUsername(dataSource.getUsername());
				source.setPassword(dataSource.getPassword());
				source.setDatabaseName("Oracle");
				source.setTables(listTables(rs.getString("DB_CODE")));
				source.setAddTime(new Date());
				source.generateId();
				logger.info("get database information ");
				list.add(source);
			}
			return list;
		});
		dsRepos.saveAll(sources);
	}
	
	public List<DataTable> listTables(String db_code) throws SQLException{
		logger.info("list all the tables in the datasource which DB_CODE is "+db_code);
		String sql = "select TB_CODE,TB_NAME,TB_CN_NAME,TB_DEC from TB_INFO_REG where DB_CODE = ? ";
		logger.debug("execute sql "+sql);
		List<DataTable> query = runner.query(sql, rs->{
			List<DataTable> list = new LinkedList<>();
			while(rs.next()){
				DataTable dataTable = new DataTable();
				dataTable.setName(rs.getString("TB_NAME")+"_"+rs.getString("TB_CODE"));
				dataTable.setChinese(rs.getString("TB_CN_NAME"));
				dataTable.setDesc(rs.getString("TB_DEC"));
				dataTable.setColumns((listColumns(rs.getString("TB_CODE"))));
				dataTable.generateId();
				dataTable.setAddTime(new Date());
				list.add(dataTable);
//				tableColumnRepos.saveAll(dataTable.getColumns());
			}
			return list;
		},db_code);
		
		List<DataTable> list = dataTableRepos.saveAll(query);
		return list;
	}

	private List<TableColumn> listColumns(String string) throws SQLException {
		logger.info("list the columns int the table which TB_CODE = "+string);
		String sql = "select DF_NAME,DF_CN_NAME,DF_DATA_TYPE,DF_LENGTH,REMARK from DF_INFO_REG where TB_CODE=?";
		List<TableColumn> list = runner.query(sql, rs->{
			List<TableColumn> columns = new ArrayList<>();
			while(rs.next()){
				TableColumn tableColumn = new TableColumn();
				tableColumn.setColumnName(rs.getString("DF_NAME"));
				tableColumn.setChinese(rs.getString("DF_CN_NAME"));
				tableColumn.setTypeName(rs.getString("DF_DATA_TYPE"));
				tableColumn.setColumnSize(rs.getInt("DF_LENGTH"));
				tableColumn.setRemarks(rs.getString("REMARK"));
				tableColumn.generateId();
				columns.add(tableColumn);
			}
			return columns;
		},string);
		return tableColumnRepos.saveAll(list);
	}

}
