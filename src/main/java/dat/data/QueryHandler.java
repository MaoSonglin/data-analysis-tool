package dat.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface QueryHandler extends Serializable {
	
	Map<String,List<String>> list() throws Exception;
	
	void query(List<String> columnNames) throws Exception;
	
	void query(List<String> columnNames,List<String> aliasNames) throws Exception ;
	
	default void query(String...strings) throws Exception{
		List<String> asList = Arrays.asList(strings);
		query(asList);
	}
	
	default void query(String name,String alias) throws Exception{
		List<String> names = new ArrayList<>(1);
		names.add(name);
		List<String> aliaNames = new ArrayList<String>();
		aliaNames.add(alias);
		query(names,aliaNames);
	}
	
	@Deprecated
	default JoinHandler join(String table) throws Exception{return null;};
	
	default QueryHandler join(QueryHandler handler){
		return null;
	}
	
	
	QueryHandler limit(int offset,int size);
	
	
	
	interface JoinHandler extends QueryHandler{
		JoinHandler setPkColumn(String columnName);
		
		JoinHandler setFkColumn(String columnName);
	}
	
}
