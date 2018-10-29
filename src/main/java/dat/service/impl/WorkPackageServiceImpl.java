package dat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.DataTable;
import dat.domain.PortionColumn;
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
import dat.util.StrUtil;

@Service
public class WorkPackageServiceImpl implements WorkPackageService {

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
		pg.setId();
		WorkPackage workPackage = wpRepos.save(pg);
		Response response = new Response(Constant.SUCCESS_CODE,"保存成功",workPackage);
		return response;
	}

	@Transactional
	public Response addTab(String pid, String tid) {
		// 获取数据包
		WorkPackage workPackage = null;
		try {
			workPackage = wpRepos.getOne(pid);
		} catch (Exception e1) {
			e1.printStackTrace();
			return new Response(Constant.ERROR_CODE,String.format("数据包ID“%s”不存在", pid),e1);
		}
		// 获取数据表
		DataTable dataTable = null;
		try {
			dataTable = tabRepos.getOne(tid);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,String.format("数据表ID“%s”不存在", tid),e);
		}
		if(Constant.DELETE_STATE == dataTable.getState()){
			return new Response(Constant.ERROR_CODE,String.format("ID为“%s”的数据表已被删除", tid),dataTable);
		}
		// 根据数据表构建虚拟数据表
		VirtualTable virtualTable = new VirtualTable();
		virtualTable.setChinese(dataTable.getChinese());
		virtualTable.setName(dataTable.getName());
		virtualTable.generateId();
		
		// 获取数据表中数据字段
		List<TableColumn> columns = dataTable.getColumns();
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
			vc.setFormula("="+column.getId());
			PortionColumn pc = new PortionColumn(StrUtil.generatorId(),column.getId(),column.getClass().getSimpleName());
			vc.getRefColumns().add(pc);
			virtualTable.getColumns().add(vc); // 将虚拟字段添加到虚拟数据表中
		}
		// 将虚拟数据表添加到数据包中
		workPackage.getTables().add(virtualTable);
		// 保存虚拟字段
		vcRepos.saveAll(virtualTable.getColumns());
		// 保存虚拟数据表
		vtRepos.saveAll(workPackage.getTables());
		// 保存数据包
		wpRepos.save(workPackage);
		
		Response response = new Response();
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("添加成功");
		response.put("workPackage", workPackage);
		response.put("virtualTable", workPackage.getTables());
		response.put("add", dataTable);
		return response;
	}

	@Transactional
	public Response remove(String pid, String tid) {
		// 获取数据包
		WorkPackage workPackage = wpRepos.getOne(pid);
		// 获取虚拟数据表
		VirtualTable virtualTable = vtRepos.getOne(tid);
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

}
