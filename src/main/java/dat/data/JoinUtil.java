package dat.data;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class JoinUtil {

	private DataTable<DataMap> leftTable;
	
	private DataTable<DataMap> rightTable;
	
	private String leftColumnName;
	
	private String rightColumnName;
	
	public DataTable<DataMap> doJoint(Judgment judgment){
		DataTable<DataMap> dataTable = new DataTable<>();
		for (DataMap dataMap : leftTable) {
			for(DataMap rightMap : rightTable){
				DataMap tmpMap = new DataMap();
				tmpMap.putAll(dataMap);
				tmpMap.putAll(rightMap);
				if(judgment.compare(leftColumnName, tmpMap.get(rightColumnName))){
					dataTable.add(tmpMap);
				}
			}
		}
		
		return dataTable;
	}
}
