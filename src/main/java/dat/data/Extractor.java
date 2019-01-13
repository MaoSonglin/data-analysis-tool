package dat.data;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class Extractor implements Iterable<Map<String,String>>,Closeable,Iterator<Map<String,String>>{
	
	private static Logger logger = LoggerFactory.getLogger(Extractor.class);
	
	private List<String> columnNames;

	private DataSource dataSource;

	private String sql;

	private Connection conn;

	private PreparedStatement ps;

	private ResultSet rs;
 
	public Extractor(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}
	
	public void setExtractNames(String tableName, List<String> columnNames){
		this.columnNames = columnNames;
		StringBuffer sb = new StringBuffer("SELECT ");
		for (String columName : columnNames) {
			sb.append(columName).append(" , ");
		}
		sb.delete(sb.length()-3, sb.length());
		sb.append(" FROM ");
		sb.append(tableName);
		sql = sb.toString();
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		try {
			if(conn == null)
				initQuery();
			return this;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @throws SQLException
	 */
	private void initQuery() throws SQLException {
		conn = dataSource.getConnection();
		ps = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		logger.debug(sql);
		rs = ps.executeQuery();
	}
	public boolean hasNext() {
		try {
			if(conn == null)
				initQuery();
			boolean last = rs.isLast();
			return !last;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> next() {
		try {
			if(conn == null)
				initQuery();
			if(rs.next()){
				Map<String,String> map = new HashMap<>();
				for(String name : columnNames){
					String value = rs.getString(name);
					map.put(name, value);
				}
				return map;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
		try {
			ps.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}




