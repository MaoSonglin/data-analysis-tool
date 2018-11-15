package dat.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.controller.WorkPackageController.ExcludeTable;
import dat.domain.DataTable;
import dat.domain.PkgPageBean;
import dat.domain.Response;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.domain.WorkPackage;
import dat.repos.DataTableRepository;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.repos.WorkPackageRepository;
import dat.service.WorkPackageService;
import dat.util.Constant;

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
		vcRepos.saveAll(virtualColumnList);
		
		// 将虚拟数据表和数据包关联
		workPackage.getTables().addAll(saveAll);
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
	
	@Transactional
	public Response addTab(String pid, String tid) {
		// 获取数据包
		WorkPackage workPackage = getPkg(pid);
		// 获取数据表
		DataTable dataTable = getDataTable(tid);
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
			vc.setTypeName(column.getTypeName());
			vc.setState(Constant.ACTIVATE_SATE);
			vc.setFormula(column.getId());
			vc.getRefColumns().add(column);
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
		// 保存对象
		wpRepos.save(workPackage);
		vtRepos.save(virtualTable);
		
		// 如果虚拟数据表没有关联其他的业务包，则删除该数据包
		if(virtualTable.getPackages().isEmpty()){
			// 如果数据表中的虚拟数据列不关联其他虚拟数据表，则将其一并删除
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

	

}
