package dat.data;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.App;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.service.UploadFileService;

public class ExcelMonalise extends MonaliseDataSource {

	protected ExcelMonalise(Source source) {
		super(source);
	}

	@Override
	public DataTable<DataMap> getDataTableBody(dat.domain.DataTable table, List<TableColumn> columns) throws Exception {
		File file = App.getContext().getBean(UploadFileService.class).getFile(getSource().getAssociation());
		if(file == null)
			throw new Exception("Excel文件不存在");
		
		String extension = FilenameUtils.getExtension(file.getName());
		Workbook workbook = null;
		if("xls".equalsIgnoreCase(extension)){
			workbook = new HSSFWorkbook(new FileInputStream(file));
		}
		else if("xlsx".equalsIgnoreCase(extension)){
			workbook = new XSSFWorkbook(new FileInputStream(file));
		}
		if(workbook == null){
			throw new Exception("文件"+file.getName()+"不是有效的Excel文件");
		}
		// 工作簿
		Sheet sheet = workbook.getSheet(table.getName());
		// 第一行作为表头
		Row row = sheet.getRow(sheet.getFirstRowNum());
		// 
		Map<Integer,TableColumn> map = new HashMap<>();
		for(int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++){
			String columnName = row.getCell(i).getStringCellValue();
			for (TableColumn column : columns) {
				if(StringUtils.equalsIgnoreCase(column.getColumnName(), columnName))
					map.put(i, column);
			}
		}
		
		DataTable<DataMap> dataTable = new DataTable<>();
		// 数据从第二行开始
		for(int i = sheet.getFirstRowNum()+1; i < sheet.getLastRowNum(); i++){
			Row tmp = sheet.getRow(i);
			Set<Entry<Integer,TableColumn>> entrySet = map.entrySet();
			DataMap dataMap = new DataMap();
			for (Entry<Integer, TableColumn> entry : entrySet) {
				Cell cell = tmp.getCell(entry.getKey());
				String value = cell.getStringCellValue();
				String columnName = entry.getValue().getColumnName();
				dataMap.put(columnName, value);
			}
			dataTable.add(dataMap);
		}
		
		workbook.close();
		
		return dataTable;
	}

}
