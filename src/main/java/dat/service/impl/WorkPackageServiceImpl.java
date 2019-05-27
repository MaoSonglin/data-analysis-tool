package dat.service.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;












import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;












import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;












import dat.controller.WorkPackageController.ExcludeTable;
import dat.domain.DataTable;
import dat.domain.ForeignKeyInfo;
import dat.domain.Relevance;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.repos.DataTableRepository;
import dat.repos.ForeignKeyInfoRepository;
import dat.repos.RelevanceRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.repos.WorkPackageRepository;
import dat.service.TableColumnService;
import dat.service.VirtualTableService;
import dat.service.WorkPackageService;
import dat.util.Constant;
import dat.util.IterableUtil;
import dat.vo.PkgPageBean;
import dat.vo.Response;

@Service
public class WorkPackageServiceImpl implements WorkPackageService {

	private static Logger logger = Logger.getLogger(WorkPackageServiceImpl.class);
	
	@Autowired
	ApplicationContext context ;
	
	/**
	 * 数据包持久化接口
	 */
	@Resource(name="workPackageRepository")
	WorkPackageRepository wpRepos;

	/**
	 * 数据表持久化接口
	 */
	@Resource(name="dataTableRepository")
	DataTableRepository tabRepos;
	
	/**
	 * 数据字段持久化接口
	 */
	@Resource(name="tableColumnRepository")
	TableColumnRepository colRepos;
	
	/**
	 * 虚拟字段持久化接口
	 */
	@Resource(name="virtualColumnRepository")
	VirtualColumnRepository vcRepos;
	
	/**
	 * 虚拟数据表持久化接口
	 */
	@Resource(name="virtualTableRepository")
	VirtualTableRepository vtRepos;
	
	public Response getPackage(String id) {
		WorkPackage workPackage = wpRepos.findById(id).get();
		return new Response(Constant.SUCCESS_CODE,"查询成功",workPackage);
	}

	public Response getTables(String id) {
		try {
			// 根据id查询
			WorkPackage workPackage = wpRepos.findById(id).get();
			List<VirtualTable> tables = workPackage.getTables();
			Response response = new Response(Constant.SUCCESS_CODE,"查询成功",tables);
			response.put("workPackage", workPackage);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"指定的ID不存在",e);
		}
	}

	@Transactional
	public Response add(WorkPackage pg) {
		logger.debug("保存业务数据包"+pg);
		// 如果数据包ID为null，说明是第一次添加，设置数据包的id
		if(pg.getId() == null){
			pg.setId();
			logger.debug("生成数据包ID："+pg.getId());
		}
		// 根据数据包的名称检查数据库中是否存在重复的数据包名称
		List<WorkPackage> optional = wpRepos.findAll((root,query,cb)->{
			List<Predicate> array = new ArrayList<>();
			array.add(cb.notEqual(root.get("id"), pg.getId()));
			if(StringUtils.isEmpty(pg.getId())){
			}
			Predicate equal = cb.equal(root.get("name"), pg.getName());
			array.add(equal);
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			array.add(notEqual);
			return cb.and(array.toArray(new Predicate[array.size()]));
		});
		try {
			// 如果获取成功，说明数据库中存在数据包相同的名称，则添加失败
			WorkPackage pkg = optional.get(0);
			logger.debug(String.format("数据包名称“%s”在数据库中已经存在",pg.getName()));
			return new Response(Constant.ERROR_CODE,"数据包名称\""+pkg.getName()+"\"已经存在!",pkg);
		} catch (Exception e) {
		}
		WorkPackage workPackage = wpRepos.save(pg);
		Response response = new Response(Constant.SUCCESS_CODE,"保存成功",workPackage);
		logger.debug("保存成功");
		return response;
	}

	@Transactional
	public Response addTab(String pid, String[] tids) {
		// 获取数据包
		WorkPackage workPackage = getPkg(pid);
		// 获取数据表
		List<DataTable> tables = tabRepos.findAll((root,query,cb)->{
			Predicate in = root.get("id").in(Arrays.asList(tids));
			return in;
		});
		// 虚拟数据表数组
		List<VirtualTable> virtualTables = new ArrayList<>();
		// 虚拟字段数组
		List<VirtualColumn> virtualColumnList = new ArrayList<>();
		
		// 遍历数据表数组
		for (DataTable dataTable : tables) {
			// 构造一个虚拟数据表
			VirtualTable virtualTable = setup(workPackage, dataTable);
			virtualTables.add(virtualTable);
			
			// 构造虚拟字段数组
			List<VirtualColumn> virtualColumns = setup(virtualTable,dataTable.getColumns());
			virtualColumnList.addAll(virtualColumns);
		}
		
		// 保存虚拟数据表
		List<VirtualTable> saveAll = vtRepos.saveAll(virtualTables);
		// 保存虚拟字段
		List<VirtualColumn> list = vcRepos.saveAll(virtualColumnList);

		// 关联关系
//		saveRelevance(list);
		
		// 将虚拟数据表和数据包关联
		workPackage.getTables().addAll(saveAll);
		workPackage.setModify(true);
		wpRepos.save(workPackage);
		
		// 返回的数据对象
		Response response = new Response();
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("添加成功");
		response.setData(saveAll);	// 携带此次请求添加的虚拟数据表信息
		response.put("workPackage", workPackage);// 数据包信息
		response.put("originalTable", tables); // 本次请求添加的数据表
		response.put("virtualColumns",virtualColumnList);// 本次请求添加的虚拟字段
		return response;
	}

	/**
	 * @param list
	 */
	private void saveRelevance(List<VirtualColumn> list) {
		List<Relevance> relevances = new ArrayList<>();
		for(int i = 0, size = list.size(); i < size-1; i++){
			for(int j = i+1; j < size; j++){
				VirtualColumn v1 = list.get(i);
				VirtualColumn v2 = list.get(j);
				
				Relevance relevance = foreignRelevance(v1, v2);
				
				if(relevance == null && 
						v1.getName().equalsIgnoreCase(v2.getName()) 
						&& ! v1.getName().equalsIgnoreCase("id")){
					relevance = new Relevance();
					relevance.setColumn1(v1);
					relevance.setColumn2(v2);
					relevance.setStrong(1000f);
				}
				if(relevance != null){
					relevances.add(relevance);
				}
			}
		}
		context.getBean(RelevanceRepository.class).saveAll(relevances);
		logger.debug("获取 "+relevances.size()+" 条关联");
	}

	/**
	 * 将v1和v2底层的字段的外键关联信息转化为为虚拟数据表之间的关联信息
	 * @param v1
	 * @param v2
	 * @return
	 */
	private Relevance foreignRelevance(VirtualColumn v1, VirtualColumn v2) {
		// 读取外键
		List<ForeignKeyInfo> f1s = getRelevancedTableColumn(v1);
		// 外键
		List<ForeignKeyInfo> f2s = getRelevancedTableColumn(v2);
		
		Relevance relevance =null; 
		for (ForeignKeyInfo f1 : f1s) {
			for (ForeignKeyInfo f2 : f2s) {
				if(f1.equals(f2)){
					relevance = new Relevance();
					relevance.setColumn1(v1);
					relevance.setColumn2(v2);
					relevance.setStrong(3000f/(v1.getRefColumns().size()+v2.getRefColumns().size()));
				}
			}
		}
		return relevance;
	}

	/**
	 * @param v1
	 * @return
	 */
	private List<ForeignKeyInfo> getRelevancedTableColumn(VirtualColumn v1) {
		logger.debug("读取虚拟字段'"+v1.getName()+"'底层的实体字段关联信息");
		List<TableColumn> columns = v1.getRefColumns();
		List<ForeignKeyInfo> list = context.getBean(ForeignKeyInfoRepository.class).findAll((root,query,cb)->{
			List<String> ids = IterableUtil.getIterableField(columns, "id");
			Predicate in = root.get("foreignKey").get("id").in(ids);
			Predicate in2 = root.get("referencedColumn").get("id").in(ids);
			return cb.or(in,in2);
		});
		return list;
	}
	
	@Transactional
	public Response addTab(String pid, String tid) {
		// 获取数据包
		WorkPackage workPackage = getPkg(pid);
		// 获取数据表
		DataTable dataTable = getDataTable(tid);
		// 查找失败
		if(workPackage == null || dataTable == null){
			return new Response(Constant.ERROR_CODE,"没有指定的业务包或者数据表");
		}
		
		// 根据数据表构建虚拟数据表
		VirtualTable virtualTable = setup(workPackage, dataTable);

		// 保存虚拟字段的数组
		List<VirtualColumn> virtualColumns =setup(virtualTable, dataTable.getColumns());
		
		// 保存虚拟数据表
		VirtualTable save = vtRepos.save(virtualTable);

		// 保存虚拟字段
		vcRepos.saveAll(virtualColumns);
		
		// 将虚拟数据表添加到数据包中
		workPackage.getTables().add(virtualTable);
		workPackage.setModify(true);
		// 保存数据包
		wpRepos.save(workPackage);
		
		// 返回对象
		Response response = new Response();
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("添加成功");
		response.setData(save);
		response.put("workPackage", workPackage);
		response.put("virtualTable", workPackage.getTables());
		response.put("originalTable", dataTable);
		response.put("virtualColumns",virtualColumns);
		return response;
	}

	/**
	 * 构建虚拟数据表
	 * @param workPackage	构建的虚拟数据表所属的业务包
	 * @param dataTable		构建的虚拟数据表参照的实体数据表
	 * @return				虚拟数据表
	 */
	private VirtualTable setup(WorkPackage workPackage, DataTable dataTable) {
		VirtualTable virtualTable = new VirtualTable();
		virtualTable.setChinese(dataTable.getChinese());
		virtualTable.setName(dataTable.getName());
		virtualTable.getPackages().add(workPackage);
		virtualTable.generateId();
		return virtualTable;
	}

	/**
	 * 构建虚拟字段数组
	 * @param virtualTable	构建的字段所属的虚拟数据表
	 * @param columns		构建的虚拟字段参考的实体字段		
	 * @return 				构建的结果
	 */
	private List<VirtualColumn> setup(VirtualTable virtualTable,
			 List<TableColumn> columns) {
		List<VirtualColumn> virtualColumns = new ArrayList<>();
		// 遍历数据字段，根据真实的数据字段构建新的虚拟字段作为虚拟数据表中的字段
		for (TableColumn column : columns) {
			// 如果数据列的状态是删除状态
			boolean isDelete = column.getState().equals(Constant.DELETE_STATE);
			if(isDelete)
				continue;
			VirtualColumn vc = new VirtualColumn();
			vc.setName(column.getColumnName());
			vc.setChinese(column.getChinese());
			if("VARCHAR".equals(column.getTypeName())){
				String typeName = context.getBean(TableColumnService.class).getTypeName(column);
				vc.setTypeName(typeName);
				if(!"VARCHAR".equalsIgnoreCase(typeName)){
					column.setTypeName(typeName);
//					colRepos.save(column);
				}
			}else{
				vc.setTypeName(column.getTypeName());
			}
			vc.setState(Constant.ACTIVATE_SATE);
			vc.setFormula(column.getId());
			List<TableColumn> refColumns = vc.getRefColumns();
			if(refColumns != null)
			refColumns.add(column);
			else{
				refColumns = new ArrayList<>();
				vc.setRefColumns(refColumns);
				refColumns.add(column);
			}
			
			vc.setId();
			vc.setTable(virtualTable);
			virtualColumns.add(vc);
		}
		return virtualColumns;
	}

	/**
	 * 根据数据表的id，从数据库中获取数据表实体类对象
	 * @param tid
	 * @return
	 */
	private DataTable getDataTable(String tid) {
		DataTable dataTable = null;
		try {
			dataTable = tabRepos.findById(tid).get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("数据表ID“%s”不存在", tid),e);
		}
		if(Constant.DELETE_STATE == dataTable.getState()){
			throw new IllegalArgumentException(String.format("ID为“%s”的数据表已被删除", tid));
		}
		return dataTable;
	}

	/**
	 * 根据数据包ID获取实体类对象
	 * @param pid
	 * @return
	 */
	private WorkPackage getPkg(String pid) {
		WorkPackage workPackage = null;
		try {
			workPackage = wpRepos.findById(pid).get();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new IllegalArgumentException(String.format("数据包ID“%s”不存在", pid),e1);
		}
		return workPackage;
	}

	@Transactional
	public Response remove(String pid, String tid) {
		// 获取数据包
		WorkPackage workPackage = wpRepos.findById(pid).get();
		// 获取虚拟数据表
		VirtualTable virtualTable = vtRepos.findById(tid).get();
		// 移除业务包实体类中关联的虚拟表对象
		boolean b = workPackage.getTables().remove(virtualTable);
		// 移除虚拟数据表实体中关联的业务包
		boolean c = virtualTable.getPackages().remove(workPackage);
		workPackage.setModify(true);
		// 保存对象
		wpRepos.save(workPackage);
		vtRepos.save(virtualTable);
		
		// 如果虚拟数据表没有关联其他的业务包，则删除该数据包
		if(virtualTable.getPackages().isEmpty()){
			// 如果数据表中的虚拟数据列不关联其他虚拟数据表，则将其一并删除
			List<VirtualColumn> columns = virtualTable.getColumns();
			RelevanceRepository bean = context.getBean(RelevanceRepository.class);
			List<Relevance> relevances = bean.findAll((root,query,cb)->{
				List<String> ids = IterableUtil.getIterableField(columns, "id");
				Predicate in = root.get("column1").get("id").in(ids);
				Predicate in2 = root.get("column2").get("id").in(ids);
				return cb.or(in,in2);
			});
			bean.deleteAll(relevances);
			vtRepos.delete(virtualTable);
		}
		
		Response response = new Response();
		response.setCode( b&&c ? Constant.SUCCESS_CODE : Constant.ERROR_CODE);
		response.setMessage( b&&c ? "移除成功":"移除失败");
		response.put("workPackage", workPackage);
		response.put("removed", virtualTable);
		return response;
	}

	@Override
	public Response getPkgs(PkgPageBean pageBean) {
		Page<WorkPackage> page = wpRepos.findAll(
				(root,query,cb) -> {
					Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
					return notEqual;
				},
				PageRequest.of(
						pageBean.getCurPage(),
						pageBean.getPageSize(),
						new Sort(Direction.DESC,"id")));
		return new Response(Constant.SUCCESS_CODE,"查询成功",page);
	}

	@Override
	public Response getTables(ExcludeTable excludeTable) {
		// 查询指定的数据源中包含的数据表
		List<DataTable> tables = tabRepos.findAll((root,query,cb)->{
			Predicate equal = cb.equal(root.get("source").get("id"), excludeTable.getDsid());
			return equal;
		});
		// 查询数据包中包含的数据表
//		List<VirtualTable> list = vtRepos.findAll((root,query,cb)->{
//			Predicate equal = cb.equal(root.get("packages").get("id"), excludeTable.getPkgid());
//			return equal;
//		});
		List<VirtualTable> list = wpRepos.findById(excludeTable.getPkgid()).get().getTables();
		// 过滤数据源中已经存在的数据表
		list.forEach(item -> {
			String name = item.getName();
			tables.removeIf(table -> {
				return table.getName().equals(name);
			});
		});
		int fromIndex = (excludeTable.getPage()-1)*excludeTable.getLimit();
		int count = tables.size();
		int toIndex = excludeTable.getPage()*excludeTable.getLimit();
		toIndex = toIndex<count?toIndex:count;
		toIndex = toIndex>fromIndex ? toIndex : fromIndex;
		List<DataTable> subList = tables.subList(fromIndex, toIndex);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",subList);
		response.put("count", count);
		return response;
	}

	@Override
	public Response updateIndex(String id) {
		// 根据数据包的ID获取数据包元数据信息
		WorkPackage pkg = wpRepos.findById(id).orElse(null);
		// 如果数据包不存在
		if(pkg == null)
		{
			String msg = String.format("ID为‘%s’的数据包不存在", id);
			return new Response(Constant.ERROR_CODE,msg);
		} 
		// 建立数据包表结构
		if(!rebuildTable(pkg)){
			return new Response(Constant.ERROR_CODE,"创建表结构失败",pkg);
		}
		// 获取数据包中的数据表
		List<VirtualTable> tables = pkg.getTables();
		// 数据表服务层接口
		VirtualTableService tableService = context.getBean(VirtualTableService.class);
		
		boolean f = true;
		
		for (VirtualTable table : tables) {
			f = f && tableService.extract(table);
		}
		
		pkg.setModify(!f);
		wpRepos.save(pkg);
		return new Response(f ? Constant.SUCCESS_CODE:Constant.ERROR_CODE,f ?"更新成功":"更新失败",pkg);
	}

 
	/**
	 * 新建数据表，新建数据包pkg中的数据表，删除原有的数据表
	 * @param pkg
	 * @return 
	 */
	private boolean rebuildTable(WorkPackage pkg) {
		// 数据表列表
		List<VirtualTable> tables = pkg.getTables();
		// 建表SQL语句
		List<String> sqlList = getCreateTableSql(tables);
		try (Connection conn = getConnection(pkg.getId())) {
			// 设置事物
			conn.setAutoCommit(false);
			try(Statement st = conn.createStatement()){
				// 遍历SQL语句，添加到批处理中
				for (String sql : sqlList) {
					logger.debug(sql);
					st.addBatch(sql);
				}
				// 执行批处理
				st.executeBatch();
				// 提交事务
				conn.commit();
			} catch(Exception e){
				// 出现异常后回滚事务
				conn.rollback();
				throw new SQLException(e);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace(); 
			return false;
		}
	}

	/**
	 *获取ID为id的数据包缓存数据库连接
	 * @param id
	 * @return 
	 */
	public Connection getConnection(String id) {
		logger.debugf("获取ID为'%s'的数据包的缓存数据库连接",id);
		// Java io 输入输出的临时目录
		String tmpdir = System.getProperty("java.io.tmpdir");
		// 程序当前工作目录
		String userdir = System.getProperty("user.dir");
		// 可以在application.propeties中配置数据库文件存放目录
		tmpdir = context.getBean(Environment.class).getProperty("db.cache.dir", tmpdir);
		// 获取当前工作目录最后一级目录的名称
		Path path = Paths.get(userdir);
		Path fileName = path.getFileName();
		// 获取数据保存文件夹，如果文件夹不存在，则新建一个目录
		File file = Paths.get(tmpdir, fileName.toString()).toFile();
		if(!file.isDirectory()){
			file.mkdirs();
		}
		// 数据文件路径
		String str = Paths.get(tmpdir, fileName.toString(),id+".db3").toString();
		logger.debugf("数据文件路径：%s",str);
		try {
			Class.forName("org.sqlite.JDBC");
			// 获取数据库连接
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+str);
			return (Connection) Proxy.newProxyInstance(
					getClass().getClassLoader(), 
					new Class<?>[]{Connection.class},
					(Object proxy, Method method, Object[] args)->{
						if(method.getName().equals("close")){
							logger.debugf("关闭来自%s的数据库连接",str);
						}
						return method.invoke(conn, args);
					});
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据虚拟数据表数组获取新建数据表的SQL语句
	 * @param tables
	 * @return
	 */
	private List<String> getCreateTableSql(List<VirtualTable> tables) {
		// 存放SQL语句的数组
		List<String> sqlList = new ArrayList<>(tables.size()*2);
		// 遍历每一个数据表信息，为每一个数据表构建两条SQL语句
		for (VirtualTable virtualTable : tables) {
			logger.debugf("构造创建数据表'%s'，中文名称'%s'的SQL语句",virtualTable.getName(),virtualTable.getChinese());
			List<VirtualColumn> columns = virtualTable.getColumns();
			// 删除已经存在的同名数据表
			StringBuffer buffer = new StringBuffer("DROP TABLE IF EXISTS ");
			buffer.append(virtualTable.getId());
			String sql = buffer.toString();
			sqlList.add(sql);
			
			// 清空buffer
			buffer.delete(0, buffer.length());
			buffer.append("CREATE TABLE ");
			buffer.append(virtualTable.getId());
			buffer.append("(");
			// 遍历字段
			for (VirtualColumn virtualColumn : columns) {
				String name = virtualColumn.getName(); 
				buffer.append(name);
				buffer.append(' ');
				buffer.append(virtualColumn.getTypeName());
				buffer.append(" , ");
			}
			buffer.delete(buffer.length()-3, buffer.length());
			buffer.append(");");
			
			sqlList.add(buffer.toString());
		}
		return sqlList;
	}

	@Override
	public List<VirtualTable> getTablesAndColumns(String id) {
		List<VirtualTable> list = vtRepos.findAll((root,query,cb)->{
			Predicate equal = cb.equal(root.join("packages").get("id"), id);
			return equal;
		});
		return list;
	}

	@Override
	public Response findTree(String id) {
//		WorkPackage pkg = this.wpRepos.findById(id).orElse(null);
//		if(pkg == null){
//			return new Response(Constant.ERROR_CODE,"ID不存在");
//		}
//		TreeNode treeNode = new TreeNode();
//		treeNode.setText(pkg.getName());
//		treeNode.setNodes(new ArrayList<>());
//		List<VirtualTable> tables = pkg.getTables();
//		for (VirtualTable virtualTable : tables) {
//			TreeNode tn = new TreeNode();
//			tn.setText(virtualTable.getChinese()!=null ? virtualTable.getChinese():virtualTable.getName());
//			List<VirtualColumn> columns = virtualTable.getColumns();
//			for (VirtualColumn virtualColumn : columns) {
//				
//			}
//		}
//		return null;
		throw new UnsupportedOperationException("该方法还没有实现");
	}
	

}
