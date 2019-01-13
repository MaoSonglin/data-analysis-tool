package dat.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.sqlite.JDBC;

import com.alibaba.druid.pool.DruidDataSource;

import dat.data.ExcelCargador;
import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.UploadFile;
import dat.repos.DataTableRepository;
import dat.repos.DsRepository;
import dat.repos.TableColumnRepository;
import dat.service.DataSourceService;
import dat.service.UploadFileService;
import dat.util.BeanUtil;
import dat.util.Constant;
import dat.util.MetaDataParser;
import dat.util.MetaDataParser.SourceMetaDataException;
import dat.util.StrUtil;
import dat.vo.ExcelSheet;
import dat.vo.PagingBean;
import dat.vo.Response;

@Service
public class DataSourceServiceImpl implements DataSourceService {
	
	private static Logger logger = Logger.getLogger(DataSourceServiceImpl.class);
	
	@Autowired
	private ConfigurableApplicationContext  context;
	/**
	 * DataSource没有标记为删除
	 */
	private static Specification<Source> stateNotDelete = (root,query,cb)->{
		Predicate notEqual = cb.notEqual(root.get("state"), 0);
		return notEqual;
	};
		
	@Resource(name="dsRepository")
	DsRepository dsRepos;
	
	@Resource(name="tableColumnRepository")
	TableColumnRepository colRepos;
	
	@Resource(name="dataTableRepository")
	DataTableRepository tabRepos;
	
	@Autowired
	EntityManager entityManager;
	
	public Response list(PagingBean pagingBean) {
		// 构建条件查询接口
		Specification<Source> spec = null;// CustomerSpecs.byKeyWord(Source.class,entityManager, pagingBean.getKeyword());
		spec = (root,query,cb)->{
			if(!StringUtils.isEmpty(pagingBean.getKeyword()))
			{
				Predicate like = cb.like(root.get("name"), "%"+pagingBean.getKeyword()+"%");
				Predicate like2 = cb.like(root.get("chinese"), "%"+pagingBean.getKeyword()+"%");
				return cb.or(like,like2);
			}
			
			return null;
		};
		// 连接状态查询条件，过滤掉已经标记为删除状态的数据
		Specification<Source> specification = spec.and(stateNotDelete);
		// 构造分页接口
		PageRequest pageRequest = PageRequest.of(pagingBean.getCurPage(), pagingBean.getPageSize(),new Sort(Direction.ASC,"id"));
		// 调用jpa接口查询
		Page<Source> page = dsRepos.findAll(specification,pageRequest);
//		List<Source> findAll = dsRepos.findAll(specification);
		logger.debug("查询到记录条数："+page.getNumberOfElements());
		// 返回数据类型
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		res.put("request", pagingBean);
		return res;
	}

	

	@Transactional
	public Response add(Source source) throws Exception {
		try {
			// 检查数据源的属性是否存在冲突
			checkAttribute(source);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return new Response(Constant.ERROR_CODE,e.getMessage(),e);
		}
		// 为数据源设置新的ID
		source.generateId();
		source.setAddTime(StrUtil.currentTime());
		// 保存数据源信息
		Source save = dsRepos.save(source);
		Response response = new Response();
		
		// 如果是Excel文件数据源
		if("Excel".equalsIgnoreCase(save.getDatabaseName())){
//			List<ExcelSheet> list = 解析Excel文件(save);
			save.setState(Constant.DELETE_STATE);
			response.setData(save);
//			response.put("sheets", list);
		}else{
			try {
				// 读取数据源中的元数据
				MetaDataParser sourceMetaData = MetaDataParser.getSourceMetaData(source);
				if(!sourceMetaData.testConnection()){
					return new Response(Constant.ERROR_CODE,"数据源连接失败，请检查数据源配置是否正确！");
				}
				// 保存数据源中的表格和字段
				saveTableAndColumn(sourceMetaData);
			} catch (SourceMetaDataException e) {
				logger.info(e.getMessage());
				throw new RuntimeException(e);
			}
			// TODO 2. 读取数据表之间的关联关系
//			List<DataTable> tables = save.getTables();
//			for (DataTable dataTable : tables) {
//				List<TableColumn> columns = dataTable.getColumns();
//				for (TableColumn tableColumn : columns) {
//					tableColumn
//				}
//			}
			response.setData(save);
		}
		
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("保存成功");
		
		return response;
	}



	/**
	 * @param save
	 * @return 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private List<ExcelSheet> 解析Excel文件(Source save) throws IOException,
			FileNotFoundException {
		
		try (Workbook workbook = context.getBean(UploadFileService.class).getWorkbook(save.getAssociation());){
			
			List<ExcelSheet> list = new ArrayList<>();
			for(int i = 0 , number = workbook.getNumberOfSheets(); i < number; i++){
				// 获取工作簿
				Sheet sheetAt = workbook.getSheetAt(i);
				// 工作簿名称
				String sheetName = sheetAt.getSheetName();
				
				// 保存工作簿内容的javabean
				ExcelSheet excelSheet = new ExcelSheet();
				int lastRowNum = sheetAt.getLastRowNum();
				// 如果工作簿中的数据量小于等于0
				if(lastRowNum <= 0)
					continue;
				// 获取第一行数据
				Row row = sheetAt.getRow(0);
				
				// 存放栏位名称的数组
				excelSheet.setColumnNames(new ArrayList<>());
				// 存放栏位类型的数组
				excelSheet.setTypes(new ArrayList<>());
				excelSheet.setLengths(new ArrayList<>());
				for(short index = row.getFirstCellNum(), lastCellNum = row.getLastCellNum(); index < lastCellNum; index++){
					// 获取单元格
					Cell cell = row.getCell(index);
					if(cell != null){
						String value = cell.toString();
						excelSheet.getColumnNames().add(value);
					}else{
						excelSheet.getColumnNames().add(null);
					}
					excelSheet.getTypes().add("varchar");
					excelSheet.getLengths().add(255);
				}
				
				excelSheet.setSheetName(sheetName);
				excelSheet.setFieldNameRow(1);
				excelSheet.setFirstDataRow(2);
				excelSheet.setDatePattern("YMD");
				excelSheet.setDateSegmentation("/");
				excelSheet.setTimeSegmentation(":");
				excelSheet.setDateTimeSort("0");
				list.add(excelSheet);
			}
			return list;
		} finally {
		}
	}


	/**
	 * 保存数据源中的数据表和字段列
	 * @param sourceMetaData
	 */
	private void saveTableAndColumn(MetaDataParser sourceMetaData) {
		// 读取数据源中包含的数据表
		//List<DataTable> tables = sourceMetaData.getTables();
		// 保存数据表信息
		//tabRepos.saveAll(tables);
		
		// 读取数据表中包含的数据字段
		//List<TableColumn> list = new ArrayList<>();
		//for (DataTable dataTable : tables) {
		//	List<TableColumn> columns = sourceMetaData.getColumnOfTable(dataTable);
		//	list.addAll(columns);
		//}
		List<TableColumn> list = sourceMetaData.getColumns();
		// 保存数据字段信息
		colRepos.saveAll(list);
	}



	/**
	 * 检查数据源source中的名称或者URL是否在数据库中已经存在了
	 * @param source
	 */
	private void checkAttribute(Source source) {
		List<Source> list = dsRepos.findAll((root,query,cb)->{
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("name"), source.getName()));
			predicates.add(cb.equal(root.get("url"), source.getUrl()));
			Predicate predicate = cb.or(predicates.toArray(new Predicate[predicates.size()]));
			predicates.clear();
			predicates.add(cb.notEqual(root.get("state"), Constant.DELETE_STATE));
			if(!StringUtils.isEmpty(source.getId())){
				predicates.add(cb.notEqual(root.get("id"), source.getId()));
			}
			predicates.add(predicate);
			// 最终过滤条件： where id <> :id and state <> :id and (name = :name or url = :url)
			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		});
		for (Source s : list) {
			if(s.getName().equals(source.getName())){
				throw new IllegalArgumentException("数据源名称\""+source.getName()+"\"已存在!");
			}
			if(s.getUrl().equals(source.getUrl())){
				throw new IllegalArgumentException("URL\""+source.getUrl()+"\"在数据源\""+s.getName()+"\"中已经存在");
			}
		}
	}

	@Transactional
	public Response update(Source source) {
		try {
			// 检查数据源属性是否存在冲突
			checkAttribute(source);
			// 根据数据源的ID号查找到要修改的对象
			Optional<Source> optional = dsRepos.findById(source.getId());
			Source s = optional.get();
			BeanUtil.copyAttributes(source, s);
			// 如果对象对象存在
			return new Response(Constant.SUCCESS_CODE,"修改成功！",dsRepos.save(source));
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"修改失败:"+e.getMessage(),e);
		}
	}

	@Transactional
	public Response delete(String id) {
		Optional<Source> optional = dsRepos.findById(id);
		try {
			Source source = optional.get();
			source.setState(Constant.DELETE_STATE);
			source.getTables().forEach(table->{
				table.setState(Constant.DELETE_STATE);
				table.getColumns().forEach(column->{
					column.setState(Constant.DELETE_STATE);
				});
			});
		} catch (Exception e) {
			return new Response(Constant.ERROR_CODE,"删除失败",e.getMessage());
		}
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}



	public Response getById(String id) {
		Optional<Source> optional = dsRepos.findById(id);
		Source source = optional.get();
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",source);
		return res;
	}



	public Response getTablesById(String id) {
		try {
			Source source = dsRepos.findById(id).get();
			List<DataTable> tables = source.getTables();
			Response response = new Response(Constant.SUCCESS_CODE,"查询成功",tables);
			response.put("datasource", source);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"查询失败",e);
		}
	}

	public JdbcTemplate getTemplate(Source source) {
		JdbcTemplate jdbcTemplate;
		// the name of the JdbcTemplate instance
		String beanName = source.getName()+"jdbcTemplate";
		try {
			// try to find the instance in container
			jdbcTemplate = context.getBean(beanName,JdbcTemplate.class);
		} catch (NoSuchBeanDefinitionException e) {
			// if there is not the instance name 'baseName' in container
			// create a new instance and pull it into the container
			
			// the four variables needed to connection the database.
			String driverClass = source.getDriverClass();
			String url = source.getUrl();
			String username = source.getUsername();
			String password = source.getPassword();
			if(url == null){
				String realPath = context.getBean(UploadFileService.class).getRealPath(source.getAssociation().getId());
				url = "jdbc:sqlite:"+realPath;
				driverClass = "org.sqlite.JDBC";
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("jdbcTemplate instance '"+beanName+"' is not exist, attempt created new one");
				logger.debug("get database connection with url "+url+" and username="+username+" and password="+password);
			}
			// define a data source the jdbcTemplate needed 
			DruidDataSource ds = new DruidDataSource();
			ds.setDriverClassName(driverClass);
			ds.setUrl(url);
			ds.setUsername(username);
			ds.setPassword(password);
			
			// the next steps is to add a java bean to spring container required
			BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
			beanDefinitionBuilder.addConstructorArgValue(ds);
			AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
			BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) context.getBeanFactory();
			beanFactory.registerBeanDefinition(beanName, beanDefinition);
			// get java bean 
			jdbcTemplate = context.getBean(beanName, JdbcTemplate.class);
		}
		return jdbcTemplate;
	}



	@Override
	public Set<Source> findSourceContain(List<DataTable> quoteTable) {
		Set<String> ids = new HashSet<>();
		quoteTable.forEach(elem->{ids.add(elem.getId());});
		Specification<Source> spec = (root,query,cb)->{
			Join<Source, DataTable> join = root.join("tables");
			Predicate predicate = join.get("id").in(ids);
			return predicate;
		};
		List<Source> list = dsRepos.findAll(spec);
		return new HashSet<>(list);
	}



	@Override
	public List<ExcelSheet> getExcelSheet(String id) throws Exception {
		Source source = dsRepos.findById(id).get();
		List<ExcelSheet> list = 解析Excel文件(source);
		return list;
	}



	@Override
	@Transactional
	public Source extract(String id, List<ExcelSheet> sheets) {
		// 查找到对应的数据源
		Source source = dsRepos.findById(id).get();
		
		// 获取Excel文件操作对象
		try(Workbook workbook = context.getBean(UploadFileService.class).getWorkbook(source.getAssociation());
				Connection conn = getConnection(source)){
			conn.setAutoCommit(false);
			// 将Excel的中数据转化为SQL语句的对象
			ExcelCargador cargador = new ExcelCargador(workbook);
			// 遍历所有的工作簿配置对象
			for(ExcelSheet sheet : sheets){
				// 设置工作簿配置
				cargador.setSheetConfig(sheet);
				// 创建数据库中的数据表
				try(Statement st = conn.createStatement()){
					st.addBatch(cargador.dropSql());
					st.addBatch(cargador.createSql());
					st.executeBatch();
				}
				// 插入数据
				try(PreparedStatement ps = conn.prepareStatement(cargador.insertSql())){
					for(List<String> params : cargador){
						for(int i = 0, size = params.size(); i< size; i++){
							ps.setString(i+1, params.get(i));
						}
						ps.addBatch();
					}
					ps.executeBatch();
				}
				
				saveMetadata(source, sheet);
//				dataTable.set
			}
			
			source.setState(Constant.ACTIVATE_SATE);
			Source save = dsRepos.save(source);			
			
			// 删除Excel文件
			File file = context.getBean(UploadFileService.class).getFile(save.getAssociation());
			file.delete();
			// 更新数据源文件信息
			UploadFile association = save.getAssociation();
			String fileName = association.getFileName();
			int lastIndexOf = fileName.lastIndexOf('.');
			String dbFileName = fileName.substring(0, lastIndexOf);
			String replace = association.getVirtualPath().replace(fileName, dbFileName);
			association.setFileName(dbFileName+".db3");
			association.setVirtualPath(replace+".db3");
			context.getBean(UploadFileService.class).save(association);
			
			conn.commit();
			return save;
		} catch (IOException | SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}



	/**
	 * @param source
	 * @param sheet
	 */
	private DataTable saveMetadata(Source source, ExcelSheet sheet) {
		String tableName = sheet.getTableName();
		DataTable dataTable = new DataTable();
		dataTable.setName(tableName);
		dataTable.generateId();
		dataTable.setChinese(sheet.getChineseName());
		dataTable.setDesc(sheet.getDesc());
		dataTable.setState(Constant.ACTIVATE_SATE);
		dataTable.setSource(source);
		context.getBean(DataTableRepository.class).save(dataTable);
		List<String> columnNames = sheet.getColumnNames();
		List<String> types = sheet.getTypes();
		List<String> chineseNames = sheet.getFieldChineseNames();
		List<String> fieldNames = sheet.getFieldNames();
		List<Integer> lengths = sheet.getLengths();
		ArrayList<TableColumn> columns = new ArrayList<>();
		for(int i = 0, size = columnNames.size(); i < size; i++){
			String fieldName = fieldNames.get(i);
			Integer length = lengths.get(i);
			String chineseName = chineseNames.get(i);
			String typeName = types.get(i);
			TableColumn tableColumn = new TableColumn();
			tableColumn.setColumnName(fieldName);
			tableColumn.setChinese(chineseName);
			tableColumn.setColumnSize(length);
			tableColumn.setTypeName(typeName == null ? "String": typeName);
			tableColumn.setDataTable(dataTable);
			tableColumn.generateId();
			tableColumn.setState(Constant.ACTIVATE_SATE);
			columns.add(tableColumn);
		}
		context.getBean(TableColumnRepository.class).saveAll(columns);
		dataTable.setColumns(columns);
		return dataTable;
	}



	private Connection getConnection(Source source) throws ClassNotFoundException, SQLException {
		if(!source.getDatabaseName().equals("Excel")){
			throw new IllegalArgumentException("must be excel data source");
		}
		Class.forName(JDBC.class.getName());
		UploadFile asso = source.getAssociation();
		String realPath = context.getBean(UploadFileService.class).getSavePath();
		// 文件名
		String fileName = asso.getVirtualPath();
		
		// 不含后缀名的文件名
		int lastIndexOf = fileName.lastIndexOf('.');
		String substring = fileName.substring(0, lastIndexOf);
		
		// sqlite文件名称
		String string = Paths.get(realPath, substring).toAbsolutePath().toString();
		
		String url = "jdbc:sqlite:"+string+".db3";
		logger.debug(url);
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}



	@Override
	public List<String> getSpecifyRow(String id, String sheetName, Integer row) throws IOException {
		Source source = dsRepos.findById(id).get();
		Workbook workbook = context.getBean(UploadFileService.class).getWorkbook(source.getAssociation());
		Sheet sheet = workbook.getSheet(sheetName);
		Row row2 = sheet.getRow(row-1);
		List<String> list = new ArrayList<>();
		for (Cell cell : row2) {
			String value = cell.toString();
			list.add(value);
		}
		return list;
	}
	
	@Override
	public Response append(List<ExcelSheet> sheets, String id, String fileId) throws Exception{
		// 获取Excel表文件
		UploadFileService uploadFileService = context.getBean(UploadFileService.class);
		Workbook workbook = uploadFileService.getWorkbook(fileId);
		// 数据源对象
		Source source = dsRepos.findById(id).orElse(null);
		// 数据源到jdbc操作模板对象
		JdbcTemplate template = getTemplate(source);
		// 自定义Excel文件解析工具类对象
		ExcelCargador cargador = new ExcelCargador(workbook);
		try(Connection conn = template.getDataSource().getConnection();){
			conn.setAutoCommit(false);
			for(ExcelSheet sheet : sheets){
				// 设置Excel解析配置对象
				cargador.setSheetConfig(sheet);
				if(!sheet.getAppend()){
					// 如果是覆盖
					try(Statement st = conn.createStatement();){
						String dropSql = cargador.dropSql();
						String createSql = cargador.createSql();
						st.addBatch(dropSql);
						st.addBatch(createSql);
						st.executeBatch();
					};
				}
				// 插入SQL语句
				String sql = cargador.insertSql();
				try(PreparedStatement ps = conn.prepareStatement(sql);){
					// 存放数据的列
					int i = 1000;
					int sum = 0;
					
					// 遍历Excel中的数据，将数据插入到数据中
					for (List<String> list : cargador) {
						int index = 1;
						for (String str : list) {
							ps.setString(index++, str);
						}
						ps.addBatch();
						if(--i < 0){
							i = 1000;
							int[] batchUpdate = ps.executeBatch();
							ps.clearBatch();
							for (int j : batchUpdate) {
								sum += j;
							}
						}
					}
					int[] batchUpdate = ps.executeBatch();
					for (int j : batchUpdate) {
						sum += j;
					}
					logger.debug("数据表'"+sheet.getTableName()+"'插入"+sum+"条数据");
				}
			}
			conn.commit();
		}
		workbook.close();
		uploadFileService.getFile(fileId).delete();
		return new Response(Constant.SUCCESS_CODE,"更新成功");
	}
	
}
