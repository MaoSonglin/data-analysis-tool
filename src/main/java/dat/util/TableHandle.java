package dat.util;

import java.util.List;
import java.util.Map;

import dat.domain.TableColumn;

public interface TableHandle {

	/**
	 * 查询原始表格中的数据
	 * @param columns		待查询的列
	 * @param offset		返回数据的偏移量
	 * @param limit			返回数据的条数
	 * @return				按列返回数据，每一列是一个map，key为该列的名称，值为该列的数据数组
	 */
	Map<String, List<String>> select(List<TableColumn> columns,int offset,int limit);
	
	
	default Map<String, List<String>> select(List<TableColumn> columns){
		return select(columns, 0, 1000);
	}
	
	Map<String, List<String>> selectByColumnNames(List<String> columnNames,int offset,int limit);
	
	default Map<String, List<String>> selectByColumnNames(List<String> columnNames){
		return selectByColumnNames(columnNames, 0, 1000);
	}
}
