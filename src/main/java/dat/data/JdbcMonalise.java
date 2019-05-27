package dat.data;

import java.sql.ResultSetMetaData;
import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.App;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.service.DataSourceService;
import dat.util.SqlHelper;

@Deprecated
class JdbcMonalise extends MonaliseDataSource{
	private static Logger logger = Logger.getLogger(JdbcMonalise.class);
	public JdbcMonalise(Source source) {
		super(source);
	}

	@Override
	public DataTable<DataMap> getDataTableBody(dat.domain.DataTable table, List<TableColumn> columns) throws Exception {
		// jdbc 操作模板
		JdbcTemplate jdbcTemplate = App.getContext().getBean(DataSourceService.class).getTemplate(getSource());
		// 数据表
		String tableName = table.getName();
		// SQL语句
		StringBuffer sBuffer = SqlHelper.selectList(columns, "columnName");
		sBuffer.append(" from ").append(tableName);
		String sql = sBuffer.toString();
		logger.debug(sql);
		DataTable<DataMap> map = new DataTable<>();
		jdbcTemplate.query(sql, (rs, i) -> {
			DataMap dataMap = new DataMap();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			for(int index = 1; index <= columnCount; index++){
				Object object = rs.getObject(index);
				String columnName = metaData.getColumnName(index);
				dataMap.put(columnName, object);
			}
			dataMap.put("rownum", i);
			logger.trace(dataMap);
			map.add(dataMap);
			return dataMap;
		});
		
		
		return map;
	}
	
}