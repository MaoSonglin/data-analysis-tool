package dat.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dat.domain.DataTable;
import dat.domain.Response;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.TableColumnPagingBean;
import dat.repos.DataTableRepository;
import dat.repos.TableColumnRepository;
import dat.service.TableColumnService;
import dat.util.Constant;
import dat.util.StrUtil;

@Service
public class TableColumnServiceImpl implements TableColumnService {
	
	@Resource(name="tableColumnRepository")
	TableColumnRepository colRepos;
	
	@Resource(name="dataTableRepository")
	DataTableRepository tabRepos;
	
	@Override
	public Response search(TableColumnPagingBean pageBean) {
		
		Page<TableColumn> page = colRepos.findAll((root,query,cb)->{
			// 构建查询条件
			// 存放查询条件的数组，每一个predicate表示一个查询条件
			List<Predicate> predicates = new ArrayList<>();
			// 判断查询条件的条件值是否是null或者空字符串，如果不是则构建一个查询条件添加到数组predicates中
			// 一共需要判断三个查询条件:
			if(!StringUtils.isEmpty(pageBean.getColumnName())){
				predicates.add(cb.like(root.get("columnName"), StrUtil.appendLike(pageBean.getColumnName())));
			}
			if(!StringUtils.isEmpty(pageBean.getTableName())){
				predicates.add(cb.like(root.get("dataTable").get("name"), StrUtil.appendLike(pageBean.getTableName())));
			}
			if(!StringUtils.isEmpty(pageBean.getSourceName())){
				predicates.add(cb.like(root.join("dataTable").get("source").get("name"), StrUtil.appendLike(pageBean.getSourceName())));
			}
			Predicate ps[] = new Predicate[predicates.size()];
			// 只查询出结果中没有标志删除的数据
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return predicates.isEmpty()? notEqual:cb.and(cb.or(predicates.toArray(ps)),notEqual);
		},
			PageRequest.of(pageBean.getCurPage(), 
					pageBean.getPageSize(), 
					new Sort(Direction.DESC,"addTime")));
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		return response;
	}

	@Override
	public Response getById(String id) {
		// 通过id查找
		TableColumn column = this.colRepos.findById(id).get();
		return new Response(Constant.SUCCESS_CODE,"查询成功",column);
	}

	public Response getTableById2(String id) {
		
		DataTable probe = new DataTable();
		probe.setId(id);
		Example<DataTable> example = Example.of(probe);
		Response response;
		try {
			DataTable dataTable = tabRepos.findOne(example).get();
			response = new Response(Constant.SUCCESS_CODE,"查询成功",dataTable);
		} catch (Exception e) {
			e.printStackTrace();
			response = new Response(Constant.ERROR_CODE,String.format("ID为“%s”的字段所属的数据表不存在", id));
		}
		return response;
	}
	
	
	public Response getTableById(String id) {
		// 查找出id属的数据表
		List<DataTable> dataTable = this.colRepos.findDataTableById(id);
		Response response = null;
		if(dataTable.isEmpty()){
			response = new Response(Constant.ERROR_CODE,String.format("ID为“%s”的字段所属的数据表不存在!", id));
		}else{
			DataTable table = dataTable.get(0);
			Source source = table.getSource();
			System.err.println(source);
			response = new Response(Constant.SUCCESS_CODE,"查询成功",table);
		}
		return response;
	}

	@Override
	public Response getSourceById(String id) {
		// 根据id查询出所属的数据源
		Source source = this.colRepos.findDsById(id);
		return new Response(Constant.SUCCESS_CODE,"查询成功",source);
	}

}
