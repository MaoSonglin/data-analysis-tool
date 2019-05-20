package dat.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import com.tsc9526.monalisa.core.query.datatable.DataMap;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.vo.ExcelSheet;
import dat.vo.PagingBean;
import dat.vo.Response;

public interface DataSourceService {

	Response list(PagingBean pagingBean);

	/**
	 * 添加新数据源
	 * @param source
	 * @return
	 * @throws Exception 
	 */
	Response add(Source source) throws Exception;

	/**
	 * 修改已有数据源信息
	 * @param source
	 * @return
	 */
	Response update(Source source);

	/**
	 * 删除数据源
	 * @param id
	 * @return
	 */
	Response delete(String id);

	Response getById(String id);

	/**
	 * 获取指定id的数据源下的所有数据表
	 * @param id 数据源id
	 * @return	数据表查询结果
	 */
	Response getTablesById(String id);

	/**
	 * 获取操作数据源的jdbcTemplate对象
	 * @param source
	 * @return
	 */
	JdbcTemplate getTemplate(Source source);

	Set<Source> findSourceContain(List<DataTable> quoteTable);

	/**
	 * 获取Excel数据源中的工作簿信息
	 * @param id	数据源id
	 * @return		工作簿信息的数组
	 * @throws Exception 
	 */
	List<ExcelSheet> getExcelSheet(String id) throws Exception;

	/**
	 * 提取指定的Excel数据源中的数据
	 * @param id
	 * @param sheets
	 * @return
	 */
	@Deprecated
	Source extract(String id,List<ExcelSheet> sheets);

	/**
	 * 获取指定id的Excel数据源中指定的工作簿sheetName中的第row行数据
	 * @param id
	 * @param sheetName
	 * @param row
	 * @return
	 * @throws IOException 
	 */
	@Deprecated
	List<String> getSpecifyRow(String id, String sheetName, Integer row) throws IOException;

	@Deprecated
	Response append(List<ExcelSheet> sheets, String id, String fileId) throws Exception;
	
	/**
	 * 获取指定数据表中的数据
	 * @param id	数据表ID
	 * @return		保存了数据表数据的内容
	 */
	default com.tsc9526.monalisa.core.query.datatable.DataTable<DataMap> getDataTableBody(String id){
		return null;
	}
}
