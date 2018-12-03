package dat.data;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;

import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.util.FormulaParser;


public class VirtualTableResultSetAdapter extends ResultSetAdapter implements ResultSetMetaData{
	private static Logger logger = Logger.getLogger(VirtualTableResultSetAdapter.class);
	private List<ResultSet> resultSetList = new ArrayList<>();
	public Map<String, Set<String>> nameMap = new HashMap<>();
	VirtualTableConnManager tableConnManager;
	private HashMap<String, ResultSet> columnInSetMap;
	
	public VirtualTableResultSetAdapter() {
	}
	
	@Override
	public boolean next() throws SQLException {
		boolean f = true;
		for (ResultSet rs : resultSetList) {
			if(rs.next()){
//				logger.debug("");
			}else{
				f=false;
			}
		}
		return f;
	}

	public void close() throws SQLException {
		for (ResultSet resultSet : resultSetList) {
			resultSet.close();
		}
	}
	
	@Override
	public boolean isClosed() throws SQLException {
		boolean f = true;
		for (ResultSet resultSet : resultSetList) {
			f = f && resultSet.isClosed();
		}
		return f;
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		Set<Entry<String,Set<String>>> entrySet = tableConnManager.getNameMap().entrySet();
		int i = 1;
		for (Entry<String, Set<String>> entry : entrySet) {
			if(i == columnIndex){
				String key = entry.getKey();
				return getString(key);
			}
		}
		return null;
	}
	
	public String getString(String columnLabel) throws SQLException {
		String formula = null;
		Map<String,String> set = new HashMap<>();
		// 获取连接中保存的数据表中的列
		for(VirtualColumn elem:tableConnManager.getColumns()){
			if(elem.getName().equals(columnLabel)){
				List<TableColumn> refColumns = elem.getRefColumns();
				for (TableColumn column : refColumns) {
					set.put(column.getColumnName(),column.getId());
				}
				formula = elem.getFormula();
			}
		}
		if(logger.isDebugEnabled()){
//			logger.debug("get column "+columnLabel+" in virtual table "+this.tableConnManager.getTable().getName());
//			logger.debug("get real columns in "+set);
		}
		if(formula == null){
			throw new SQLException("there is not a column named "+columnLabel+" !");
		}
		Map<String,String> map = new HashMap<>();
		for (Entry<String,String> columnAlias : set.entrySet()) {
			// 实体字段列所对应的结果集对象
			String key = columnAlias.getKey();
			ResultSet rs = columnInSetMap.get(key);
			// 获取值
			String values = rs.getString(key);
			// 保存为列名到值的映射
			map.put(columnAlias.getValue(), values);
		}
		FormulaParser parser = new FormulaParser(formula,map);
		String value = parser.getValue();
		return value;
	}
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return this;
	}
	@Override
	public boolean last() throws SQLException {
		return false;
	}

	@Override
	public int getRow() throws SQLException {
		return 0;
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		boolean f = true;
		for(int i = 0; i < row; i++){
			for (ResultSet rs : resultSetList) {
				f = f && rs.next();
			}
		}
		return f;
	}

	@Override
	public boolean previous() throws SQLException {
		return false;
	}

	public List<ResultSet> getResultSetList() {
		return resultSetList;
	}

	public void setResultSetList(List<ResultSet> resultSetList) {
		this.resultSetList = resultSetList;
		columnInSetMap = new HashMap<String,ResultSet>();
		// 遍历所有结果集，获取结果集中的所有列标签，保存所有列的标签到对应结果集的映射
		for (ResultSet resultSet : resultSetList) {
			try {
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();
				for(int i = 1; i <= columnCount; i++){
					String columnLabel = metaData.getColumnLabel(i);
					columnInSetMap.put(columnLabel, resultSet);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("查询所有列"+columnInSetMap.keySet());
	}

	public Map<String, Set<String>> getNameMap() {
		return nameMap;
	}

	public void setNameMap(Map<String, Set<String>> nameMap) {
		this.nameMap = nameMap;
	}

	public VirtualTableConnManager getTableConnManager() {
		return tableConnManager;
	}

	public void setTableConnManager(VirtualTableConnManager tableConnManager) {
		this.tableConnManager = tableConnManager;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return tableConnManager.getNameMap().size();
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		return false;
	}

	@Override
	public int isNullable(int column) throws SQLException {
		return 0;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		return false;
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		Set<Entry<String,Set<String>>> entrySet = tableConnManager.getNameMap().entrySet();
		int i = 1;
		for (Entry<String, Set<String>> entry : entrySet) {
			if(i == column){
				return entry.getKey();
			}
			i++;
		}
		throw new IndexOutOfBoundsException(column+" is out of range from 1 to "+getColumnCount());
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		Set<Entry<String,Set<String>>> entrySet = tableConnManager.getNameMap().entrySet();
		int i = 1;
		for (Entry<String, Set<String>> entry : entrySet) {
			if(i == column){
				return entry.getKey();
			}
			i++;
		}
		return null;
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		return null;
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return tableConnManager.getTable().getName();
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		return null;
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		return null;
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return null;
	}

}

abstract class ResultSetAdapter implements ResultSet{

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public abstract boolean next() throws SQLException ;
	public abstract void close() throws SQLException;

	public boolean wasNull() throws SQLException {
		return false;
	}

	public abstract String getString(int columnIndex) throws SQLException;

	public boolean getBoolean(int columnIndex) throws SQLException {
		return false;
	}

	public byte getByte(int columnIndex) throws SQLException {
		return 0;
	}

	public short getShort(int columnIndex) throws SQLException {
		return 0;
	}

	public int getInt(int columnIndex) throws SQLException {
		return 0;
	}

	public long getLong(int columnIndex) throws SQLException {
		return 0;
	}

	public float getFloat(int columnIndex) throws SQLException {
		return 0;
	}

	public double getDouble(int columnIndex) throws SQLException {
		return 0;
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		return null;
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return null;
	}

	public Date getDate(int columnIndex) throws SQLException {
		return null;
	}

	public Time getTime(int columnIndex) throws SQLException {
		return null;
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return null;
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return null;
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return null;
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return null;
	}

	public String getString(String columnLabel) throws SQLException {
		return null;
	}

	public boolean getBoolean(String columnLabel) throws SQLException {
		return false;
	}

	public byte getByte(String columnLabel) throws SQLException {
		return 0;
	}

	public short getShort(String columnLabel) throws SQLException {
		return 0;
	}

	public int getInt(String columnLabel) throws SQLException {
		return 0;
	}

	public long getLong(String columnLabel) throws SQLException {
		return 0;
	}

	public float getFloat(String columnLabel) throws SQLException {
		return 0;
	}

	public double getDouble(String columnLabel) throws SQLException {
		return 0;
	}

	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return null;
	}

	public byte[] getBytes(String columnLabel) throws SQLException {
		return null;
	}

	public Date getDate(String columnLabel) throws SQLException {
		return null;
	}

	public Time getTime(String columnLabel) throws SQLException {
		return null;
	}

	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return null;
	}

	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return null;
	}

	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return null;
	}
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public void clearWarnings() throws SQLException {
		
	}

	public String getCursorName() throws SQLException {
		return null;
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return null;
	}

	public Object getObject(int columnIndex) throws SQLException {
		return null;
	}

	public Object getObject(String columnLabel) throws SQLException {
		return null;
	}

	public int findColumn(String columnLabel) throws SQLException {
		return 0;
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return false;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return false;
	}

	@Override
	public boolean isFirst() throws SQLException {
		return false;
	}

	@Override
	public boolean isLast() throws SQLException {
		return false;
	}

	@Override
	public void beforeFirst() throws SQLException {
		
	}

	@Override
	public void afterLast() throws SQLException {
		
	}

	@Override
	public boolean first() throws SQLException {
		return false;
	}

	@Override
	public abstract boolean last() throws SQLException;

	@Override
	public abstract int getRow() throws SQLException ;

	@Override
	public abstract boolean absolute(int row) throws SQLException ;

	@Override
	public boolean relative(int rows) throws SQLException {
		return false;
	}

	@Override
	public abstract boolean previous() throws SQLException ;

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	@Override
	public int getType() throws SQLException {
		return 0;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return 0;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
		
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
		
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
		
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		
	}

	@Override
	public void insertRow() throws SQLException {
		
	}

	@Override
	public void updateRow() throws SQLException {
		
	}

	@Override
	public void deleteRow() throws SQLException {
		
	}

	@Override
	public void refreshRow() throws SQLException {
		
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		
	}

	@Override
	public Statement getStatement() throws SQLException {
		return null;
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return null;
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return null;
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return null;
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		
	}

	@Override
	public int getHoldability() throws SQLException {
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void updateNString(int columnIndex, String nString)
			throws SQLException {
		
	}

	@Override
	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		
	}

	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return null;
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type)
			throws SQLException {
		return null;
	}
	
}