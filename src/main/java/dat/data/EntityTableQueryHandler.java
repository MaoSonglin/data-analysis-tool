package dat.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.JoinType;
import org.jboss.logging.Logger;

import dat.data.QueryHandler.JoinHandler;
import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.util.DialectUtil;

@Deprecated
public class EntityTableQueryHandler implements QueryHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3609578038815289554L;

	private static Logger logger =  Logger.getLogger(EntityTableQueryHandler.class);
	
	protected DataTable table;
	
	protected Source source;

	protected StringBuffer sql;

	private RowSelection selection;

	private LimitHandler limitHandler;
	
	public EntityTableQueryHandler(DataTable table) {
		this(table,table.getSource());
	}
	
	

	public EntityTableQueryHandler(DataTable table, Source source) {
		super();
		this.table = table;
		this.source = source;
		loadDriver();
	}
	
	private void loadDriver() {
		try {
			Class.forName(source.getDriverClass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Map<String, List<String>> list() throws Exception {
		if(selection != null){
			String s = limitHandler.processSql(sql.toString(), selection);
			sql.delete(0, sql.length());
			sql.append(s);
		}
		logger.debug(sql);
		try(Connection conn = DriverManager.getConnection(source.getUrl(),
				source.getUsername(), source.getPassword());
				PreparedStatement ps = conn.prepareStatement(sql.toString())){
			if(selection != null){
				limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
			}
			try(ResultSet rs = ps.executeQuery()){
				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();
				Map<String,List<String>> map = new HashMap<>();
				for(int i = 1 ; i <= columnCount; i++){
					String columnName = metaData.getColumnName(i);
					map.put(columnName, new LinkedList<>());
				}
				while(rs.next()){
					Set<String> keySet = map.keySet();
					for (String key : keySet) {
						map.get(key).add(rs.getString(key));
					}
				}
				return map;
			}
		}
	}
	
	public void query(List<String> columnNames,List<String> aliasNames) throws Exception {
		sql = new StringBuffer();
		int size = columnNames.size();
		int size2 = aliasNames.size();
		int i = 0;
		int min = size < size2 ? size : size2;
		while(i < min){
			sql.append(columnNames.get(i)).append(' ').append(aliasNames.get(i)).append(",");
			i++;
		}
		while(i<size){
			sql.append(columnNames.get(i)).append(',');
			i++;
		}
		sql.deleteCharAt(sql.length()-1);
		sql.insert(0, "select ");
		sql.append(" from ").append(table.getName());
	}
	
	@Override
	public void query(List<String> columnNames) throws Exception {
		query(columnNames,columnNames);
	}

	public void query(Collection<TableColumn> columns) throws Exception {
		List<String> arrayList = new ArrayList<>();
		columns.forEach(elem->{
			String columnName = elem.getColumnName();
			arrayList.add(columnName);
		});
		query(arrayList,arrayList);
	}
	
	public void query(Collection<TableColumn> columns,List<String> aliasNames) throws Exception {
		List<String> arrayList = new ArrayList<>();
		columns.forEach(elem->{
			String columnName = elem.getColumnName();
			arrayList.add(columnName);
		});
		query(arrayList,aliasNames);
	}

	@Override
	public JoinHandler join(String tableName) throws Exception {
		boolean f = true;
		List<DataTable> tables = table.getSource().getTables();
		for (DataTable elem : tables) {
			if(elem.getName().equals(tableName)){
				f = false;
				break;
			}
		}
		if(f)
			throw new IllegalArgumentException("数据表"+table.getName()+"与数据表"+tableName+"不是同一数据源的数据表，不能连接");
		X x = new X(table);
		x.setAlias(tableName);
		x.setTableName(tableName);
		return x;
	}



	@Override
	public QueryHandler limit(int offset, int size) {
		Dialect dialect = DialectUtil.getDialect(source.getDatabaseName());
		limitHandler = dialect.getLimitHandler();
		selection = new RowSelection();
		selection.setFirstRow(offset);
		selection.setMaxRows(size+1);
		EntityTableQueryHandler newHandler = new EntityTableQueryHandler(table);
		newHandler.selection = selection;
		newHandler.limitHandler = limitHandler;
		return newHandler;
	}

}
@Deprecated
class X extends EntityTableQueryHandler implements JoinHandler{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1877750321171353261L;
	private List<String> pkColumnName;
	private List<String> fkColumnName;
	{
		pkColumnName = new ArrayList<>();
		fkColumnName = new ArrayList<>();
	}
	
	private String tableName;
	
	private String alias;
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	

	public void setAlias(String alias) {
		this.alias = alias;
	}


	public X(DataTable table) {
		super(table);
	}
	
	@Override
	public void query(List<String> columnNames) throws Exception {
		query(columnNames);
		Dialect dialect = DialectUtil.getDialect(source.getDatabaseName());
		JoinFragment joinFragment = dialect.createOuterJoinFragment();
		joinFragment.addJoin(tableName, alias==null?tableName:alias, pkColumnName.stream().toArray(String[]::new), fkColumnName.stream().toArray(String[]::new), JoinType.INNER_JOIN);
		String fromFragmentString = joinFragment.toFromFragmentString();
		sql.append(fromFragmentString);
	}
	
	@Override
	public JoinHandler setPkColumn(String columnName) {
		pkColumnName.add(columnName);
		return this;
	}
	
	@Override
	public JoinHandler setFkColumn(String columnName) {
		fkColumnName.add(columnName);
		return this;
	}
	
}

