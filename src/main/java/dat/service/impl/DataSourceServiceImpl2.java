package dat.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.data.MonaliseDataSource;
import dat.domain.Source;
import dat.repos.DataTableRepository;
import dat.repos.DsRepository;
import dat.util.Constant;
import dat.util.MetaDataParser;
import dat.vo.Response;

@SuppressWarnings("deprecation")
@Service("dataSourceServiceImpl")
public class DataSourceServiceImpl2 extends DataSourceServiceImpl {

	@Autowired
	DsRepository dsRepos;
	
	@Autowired
	ApplicationContext context;
	
	@Override
	@org.springframework.transaction.annotation.Transactional
	public Response add(Source source) throws Exception {
		
		checkAttribute(source);
		source.generateId();
		source.setAddTime(new Date().toLocaleString());
		
		// 保存数据源信息
		Source save = dsRepos.save(source);
		Response response = new Response();
		
		// 解析数据源
		MetaDataParser sourceMetaData = MetaDataParser.getSourceMetaData(save);
		if(sourceMetaData.testConnection()){
			saveTableAndColumn(sourceMetaData);
		}
		else{
			response.setCode(Constant.ERROR_CODE);
			response.setData(save);
			response.setMessage("连接数据库失败");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		
		response.setCode(Constant.SUCCESS_CODE);;
		response.setMessage("添加成功");
		response.setData(save);
		return response;
	}

	@Override
	public DataTable<DataMap> getDataTableBody(String id) {
		// 获取数据表
		dat.domain.DataTable dataTable = context.getBean(DataTableRepository.class).findById(id).orElse(null);
		if(dataTable == null)
			throw new IllegalArgumentException("数据表不存在");
		Source source = dataTable.getSource();
		try {
			return MonaliseDataSource.from(source).getDataTableBody(dataTable, dataTable.getColumns());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
