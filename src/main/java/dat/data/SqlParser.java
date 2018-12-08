package dat.data;

import java.util.List;

@Deprecated
public interface SqlParser {

	default List<Renameable> getColumns(){return null;}
	
	default Renameable getTable(){return null;}
	
	default List<JoinAble> getJoinTables(){return null;}
	
	default int getOffset(){return 0;}
	
	default int getMax(){return 1000;}

	default List<FilterCondition> getWhere(){return null;}
}
