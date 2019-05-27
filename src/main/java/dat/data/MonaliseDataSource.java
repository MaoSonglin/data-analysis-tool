package dat.data;

import java.io.FileInputStream;
import java.util.List;

import com.tsc9526.monalisa.core.query.datatable.CsvOptions;
import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.App;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.service.UploadFileService;
import dat.util.Constant;

public abstract class MonaliseDataSource {

	@SuppressWarnings("deprecation")
	public static MonaliseDataSource from(Source source){
		String databaseName = source.getDatabaseName();
		switch(databaseName){
		case Constant.ORACLE:
		case Constant.MYSOL:
		case Constant.SQL_SERVER:
		case Constant.SQLITE:
			return new JdbcMonalise(source);	// jdbc数据源
		case Constant.EXCEL:					// Excel数据源
			return new ExcelMonalise(source);
		case Constant.CSV_FILE:					// CSV数据源
			return new MonaliseDataSource(source){
				public DataTable<DataMap> getDataTableBody(dat.domain.DataTable table, List<TableColumn> columns)
						throws Exception {
					return DataTable.fromCsv(new FileInputStream(App.getContext().getBean(UploadFileService.class).getFile(source.getAssociation())), new CsvOptions());
				}};
		case Constant.TXT_FILE:
			
		}
		throw new IllegalArgumentException("不支持的数据源类型："+databaseName);
	}
	
	private Source source;

	protected MonaliseDataSource(Source source) {
		super();
		this.setSource(source);
	}
	/**
	 * 获取数据表table中的的columns中的字段的数据内容
	 * @param table		数据表信息
	 * @param columns	字段信息
	 * @return			可以获取数据内容的对象
	 */
	public abstract DataTable<DataMap> getDataTableBody(dat.domain.DataTable table, List<TableColumn> columns) throws Exception;

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
	
}




