package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.domain.Response;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.repos.TableColumnRepository;
import dat.repos.VirtualColumnRepository;
import dat.repos.VirtualTableRepository;
import dat.service.VirtualColumnService;
import dat.util.Constant;

@Service
public class VirtualColumnServiceImpl implements VirtualColumnService {
	
	private static Logger logger = Logger.getLogger(VirtualColumnServiceImpl.class);
	
	@Autowired
	VirtualColumnRepository vcRepos;
	
	@Autowired
	TableColumnRepository tcRepos;
	
	@Autowired
	VirtualTableRepository vtRepos;
	
	@Override
	public Response deleteById(String id) {
		vcRepos.deleteById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"删除成功");
		return response;
	}
	
	// 保存虚拟字段，如果虚拟字段的ID在数据库中已经存在，name就保存，否则就添加
	@Transactional
	public Response save(VirtualColumn column) {
		Response response = null;
		try {
			// 根据column的ID查找出数据库中的对象
			VirtualColumn vc = vcRepos.findById(column.getId()).get();
			
			// 跟新数据库中对象的相关属性
			vc.setChinese(column.getChinese());
			vc.setFormula(column.getFormula());
			vc.setName(column.getName());
			vc.setState(column.getState());
			vc.setTypeName(column.getTypeName());
			vc.setRemask(column.getRemask());
			vcRepos.save(vc);
			response = new Response(Constant.SUCCESS_CODE,"修改成功",vc);
		} catch (NoSuchElementException e) {
			// 如果捕获这个异常，说明column在数据库中不存在，那么执行插入操作
			VirtualColumn save = vcRepos.save(column);
			response = new Response(Constant.ERROR_CODE,"添加成功",save);
		} catch (Exception e){
			e.printStackTrace();
			response = new Response(Constant.ERROR_CODE,e.getMessage(),e);
		}
		response.put("oldVal", column);
		return response;
	}

	@Transactional
	public Response createField(VirtualColumn virtualColumn) {
		// 新建字段引用的其他字段
		List<TableColumn> refColumns = virtualColumn.getRefColumns();
		// 引用子弹的id数组
		List<String> ids = new ArrayList<>();
		for (TableColumn tableColumn : refColumns) {
			ids.add(tableColumn.getId());
		}
		
		// 查询出新建字段需要引用的所有字段
		List<VirtualColumn> findAll = vcRepos.findAll((root,query,cb)->{
			Predicate in = root.get("id").in(ids);
			return in;
		});
		// 新间字段应用的字段所对应的原始字段数组
		List<TableColumn> columns = new ArrayList<>();
		for (VirtualColumn vc : findAll) {
			columns.addAll(vc.getRefColumns());
		}
//		List<TableColumn> columns = tcRepos.findAll((root,query,cb)->{
//			root.join("re")
//			Root<VirtualColumn> from = query.from(VirtualColumn.class);
//			Join<VirtualColumn, TableColumn> join = from.join("refColumns",JoinType.LEFT);
//			Predicate in = join.get("id").in(ids);
//			return in;
//		});
		logger.debug(columns);
		virtualColumn.setRefColumns(columns);
		virtualColumn.setId();
		virtualColumn.setState(Constant.ACTIVATE_SATE);
		// TODO  在保存之前：1、检测新建字段名称在字段所在数据表中是否重复；2、检测新建字段的公式是否合法
		// 检测字段是否重复
		checkField(virtualColumn);
		vcRepos.save(virtualColumn);	// 保存新建的字段
		return new Response(Constant.SUCCESS_CODE,"保存成功",virtualColumn);
	}

	/**
	 * @param virtualColumn
	 */
	private void checkField(VirtualColumn virtualColumn) {
		String chinese = virtualColumn.getChinese();
		String name = virtualColumn.getName();
		if(logger.isDebugEnabled()){
			logger.debug(String.format("检测在数据表%s中是否存在数据表%s，%s",virtualColumn.getTable().getId(),name,chinese));
		}
		List<VirtualColumn> findAll2 = vcRepos.findAll((root,query,cb)->{
			List<Predicate> list = new ArrayList<>();
			list.add(cb.equal(root.get("table").get("id"), virtualColumn.getTable().getId()));
			if(!StringUtils.isEmpty(name))
				list.add(cb.equal(root.get("name"), name));
			if(!StringUtils.isEmpty(chinese))
				list.add(cb.equal(root.get("chinese"), chinese));
			return cb.and(list.toArray(new Predicate[list.size()]));
		});
		if(logger.isDebugEnabled()){
			logger.debug("查询到数据条数："+findAll2.size());
		}
		if(!findAll2.isEmpty()){
			for (VirtualColumn v : findAll2) {
				if(v.getName().equals(name)){
					throw new IllegalArgumentException(String.format("字段名称\"%s\"已经存在", name));
				}
				if(null!=v.getChinese()&&v.getChinese().equals(chinese)){
					throw new IllegalArgumentException(String.format("字段中文名称\"%s\"已经存在",chinese));
				}
			}
		}
	}

	
}
