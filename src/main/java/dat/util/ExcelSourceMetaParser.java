package dat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tsc9526.monalisa.core.query.datatable.CsvOptions;
import com.tsc9526.monalisa.core.query.datatable.DataColumn;
import com.tsc9526.monalisa.core.query.datatable.DataMap;

import dat.App;
import dat.domain.DataTable;
import dat.domain.ForeignKeyInfo;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.UploadFile;
import dat.service.UploadFileService;

class ExcelSourceMetaParser extends MySQLSourceMetaData implements MetaDataParser{
	
	public ExcelSourceMetaParser(Source source) {
		Source s = new Source();
		BeanUtil.copyAttributes(source, s);
		UploadFile asso = source.getAssociation();
		String realPath = App.getContext().getBean(UploadFileService.class).getRealPath(asso.getId());
		
		// 不含后缀名的文件名
		int lastIndexOf = realPath.lastIndexOf('.');
		String substring = realPath.substring(0, lastIndexOf);
		
		String url = "jdbc:sqlite:"+substring+".db3";
		s.setUrl(url);
		s.setDriverClass("org.sqlite.JDBC");
		s.setDatabaseName("SQLite");
		setSource(s);
	}
}
/**
 * Excel表格数据源解析器
 * @author MaoSonglin
 *
 */
class ExcelMetaParser implements MetaDataParser{
	
	Source source;
	File file;
	
	public ExcelMetaParser(Source source){
		this.source = source;
		file = App.getContext().getBean(UploadFileService.class).getFile(source.getAssociation());
	}
	
	@Override
	public boolean testConnection() {
		if(file == null)
			return false;
		return file.isFile();
	}

	@Override
	public List<DataTable> getTables() {
		try(Workbook workbook = getWorkbook()) {
			
			
			int numberOfSheets = workbook.getNumberOfSheets();
			List<DataTable> list = new ArrayList<>(numberOfSheets);
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				String sheetName = sheet.getSheetName();
				DataTable dataTable = new DataTable();
				dataTable.generateId();
				dataTable.setAddTime(new Date());
				dataTable.setChinese(sheetName);
				dataTable.setName(sheetName);
				dataTable.setDesc(sheetName);
				dataTable.setSource(source);
				dataTable.setState(Constant.ACTIVATE_SATE);
				list.add(dataTable);
			}
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Workbook getWorkbook() throws IOException, FileNotFoundException {
		String extension = FilenameUtils.getExtension(file.getName());
		Workbook workbook = null;
		if("xls".equalsIgnoreCase(extension)){
			workbook = new HSSFWorkbook(new FileInputStream(file));
		}else if("xlsx".equalsIgnoreCase(extension)){
			workbook = new XSSFWorkbook(new FileInputStream(file));
		}
		if(workbook == null){
			throw new IOException(String.format("文件‘%s’不是有效的Excel文件类型", file.getName()));
		}
		return workbook;
	}

	@Override
	public List<TableColumn> getColumnOfTable(DataTable table) {
		try (Workbook workbook = getWorkbook()) {
			Sheet sheet = workbook.getSheet(table.getName());
			Row row = sheet.getRow(0);
			List<TableColumn> columns = new ArrayList<>();
			for(int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++){
				Cell cell = row.getCell(i);
				String cellValue = cell.getStringCellValue();
				TableColumn tableColumn = new TableColumn();
				tableColumn.setColumnName(cellValue);
				tableColumn.setChinese(cellValue);
				tableColumn.setDataTable(table);
				tableColumn.setState(Constant.ACTIVATE_SATE);
				tableColumn.setAddTime(StrUtil.currentTime());
				tableColumn.setTypeName("String");
				tableColumn.generateId();
				columns.add(tableColumn);
			}
			return columns;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public List<TableColumn> getColumns() {
		List<DataTable> tables = getTables();
		List<TableColumn> c = new ArrayList<>();
		for (DataTable dataTable : tables) {
			List<TableColumn> columns = getColumnOfTable(dataTable);
			c.addAll(columns);
			dataTable.setColumns(columns);
		}
		return c;//MetaDataParser.super.getColumns();
	}

	@Override
	public List<ForeignKeyInfo> getForeignKeyInfos() {
		return new LinkedList<>();
	}
	
}

/**
 * CSV数据源解析器
 * @author MaoSonglin
 *
 */
class CSVMetaParser extends ExcelMetaParser implements MetaDataParser{

	public CSVMetaParser(Source source) {
		super(source);
		
	}

	@Override
	public boolean testConnection() {
		return super.testConnection();
	}

	@Override
	public List<DataTable> getTables() {
		// CSV文件，每个文件只包含一张表
		String name = file.getName();
		DataTable dataTable = new DataTable();
		dataTable.setName(name);
		dataTable.setChinese(name);
		dataTable.setDesc(name);
		dataTable.setSource(source);
		dataTable.setState(Constant.ACTIVATE_SATE);
		dataTable.setAddTime(new Date());
		dataTable.generateId();
		List<DataTable> list = new ArrayList<>(1);
		list.add(dataTable);
		return list;
	}

	@Override
	public List<TableColumn> getColumnOfTable(DataTable table) {
		try {
			com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> dataTable = com.tsc9526.monalisa.core.query.datatable.DataTable.fromCsv(new FileInputStream(file), new CsvOptions());
			List<DataColumn> headers = dataTable.getHeaders();
			List<TableColumn> list = headers.stream().map(column -> {
				TableColumn tableColumn = new TableColumn();
				tableColumn.setColumnName(column.getName());
				tableColumn.setTypeName(column.getTypeString());
				tableColumn.setDataTable(table);
				tableColumn.setAddTime(StrUtil.currentTime());
				tableColumn.setState(Constant.ACTIVATE_SATE);
				tableColumn.generateId();
				return tableColumn;
			}).collect(Collectors.toList());
			return list;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}


class TextMetaDataParser extends CSVMetaParser{

	public TextMetaDataParser(Source source) {
		super(source);
	}
	@Override
	public List<TableColumn> getColumnOfTable(DataTable table) {
		try {
			// 缓冲流
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			// 读取文件的第一行
			String[] split = reader.readLine().split("\t");
			
			// 将第一行设为表头
			@SuppressWarnings("resource")
			List<TableColumn> list = Stream.of(split).map( item -> {
				TableColumn column = new TableColumn();
				column.setColumnName(item);
				column.setTypeName("String");
				column.setDataTable(table);
				column.generateId();
				column.setState(Constant.ACTIVATE_SATE);
				column.setAddTime(StrUtil.currentTime());
				return column;
			}).collect(Collectors.toList());
			reader.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}
}
