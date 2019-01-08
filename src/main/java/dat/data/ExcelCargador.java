package dat.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dat.vo.ExcelSheet;

public class ExcelCargador implements Iterable<List<String>>, Iterator<List<String>>{
	
	private static Logger logger = LoggerFactory.getLogger(ExcelCargador.class);
	
	private Workbook workbook;
	
	private ExcelSheet sheetConfig;

	private Integer curRow;

	private Sheet sheet;

	private ArrayList<Integer> columnIndexs;

	
	
	public ExcelCargador(Workbook workbook) {
		this.workbook = workbook;
	}

	public ExcelCargador(Workbook workbook, ExcelSheet sheetConfig) {
		super();
		this.workbook = workbook;
		setSheetConfig(sheetConfig);
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	public void setSheetConfig(ExcelSheet sheetConfig) {
		this.sheetConfig = sheetConfig;
		curRow = sheetConfig.getFirstDataRow()-1;
		sheet = workbook.getSheet(sheetConfig.getSheetName());
		Integer fieldNameRow = sheetConfig.getFieldNameRow()-1;
		Row row = sheet.getRow(fieldNameRow);
		columnIndexs = new ArrayList<Integer>();
		short lastCellNum = row.getLastCellNum();
		for(int i = 0; i < lastCellNum; i++){
			Cell cell = row.getCell(i);
			String columnName = cell.toString();
			int index = sheetConfig.getColumnNames().indexOf(columnName);
			if(index < 0){
				throw new IllegalArgumentException("未找到栏位名称'"+columnName+"'");
			}
			columnIndexs.add(index);
		}
	}
	
	public String dropSql(){
		return "drop table if exists "+sheetConfig.getTableName();
	}
	
	public String createSql(){
		// 字段名称数组
		List<String> columnNames = sheetConfig.getFieldNames();sheetConfig.getColumnNames();
		// 字段类型数组
		List<String> types = sheetConfig.getTypes();
		// 主键字段名称
		String primaryKey = sheetConfig.getPrimaryKey();
		
		// 获取迭代器
		Iterator<String> iter1 = columnNames.iterator();
		Iterator<String> iter2 = types.iterator();
		// 字段长度迭代器
		Iterator<Integer> iter3 = sheetConfig.getLengths().iterator();
		// SQL语句缓存
		StringBuffer buffer = new StringBuffer("create table ");
		
		buffer.append(sheetConfig.getTableName());
		buffer.append(" ( ");
		while(iter1.hasNext() && iter2.hasNext() && iter3.hasNext()){
			buffer.append(iter1.next()).append(" ");
			buffer.append(iter2.next()).append("(").append(iter3.next()).append(") , ");
		}
		if(primaryKey != null){// 如果有主键设置主键
			buffer.append("primary key ( ").append(primaryKey).append(" ) ");
		}else{// 否则删除最后的逗号
			buffer.delete(buffer.length()-3,buffer.length());
		}
		buffer.append(" ) ");
		String sql = buffer.toString();
		logger.debug(sql);
		return sql;
	}
	
	public String insertSql(){
		// 字段名称数组
		List<String> columnNames = sheetConfig.getFieldNames();//sheetConfig.getColumnNames();
		StringBuffer buffer = new StringBuffer("insert into ");
		buffer.append(sheetConfig.getTableName());
		buffer.append(" ( ");
		for(String name : columnNames){
			buffer.append(name).append(" , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
		buffer.append(" ) values ( ");
		for(int i = 0, size = columnNames.size(); i< size; i++){
			buffer.append("? , ");
		}
		buffer.delete(buffer.length()-3, buffer.length());
		buffer.append(" )");
		String sql = buffer.toString();
		logger.debug(sql);
		return sql;
	}
	
	@Override
	public Iterator<List<String>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		int lastRowNum = sheet.getLastRowNum();
		return curRow < lastRowNum && sheet.getRow(curRow) != null;
	}

	@Override
	public List<String> next() {
		Row row = sheet.getRow(curRow++);
		List<String> list = new ArrayList<>();
		for(int index : columnIndexs){
			Cell cell = row.getCell(index);
			if(cell == null){
				list.add(null);
			}else{
				String string = cell.toString();
				list.add(string);
			}
		}
		return list;
	}
	
	
	
}
