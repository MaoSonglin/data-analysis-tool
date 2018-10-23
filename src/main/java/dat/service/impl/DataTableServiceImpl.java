package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dat.domain.DataTable;
import dat.domain.DataTablePaingBean;
import dat.domain.Response;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.repos.DataTableRepository;
import dat.service.DataTableService;
import dat.util.Constant;
import dat.util.StrUtil;

@Service
public class DataTableServiceImpl implements DataTableService {
	
	@Resource(name="dataTableRepository")
	private DataTableRepository tabRepos;
	
	@Override
	public Response search(DataTablePaingBean pageBean) {
		Page<DataTable> page = tabRepos.findAll((root,query,cb)->{
			List<Predicate> predicates = new ArrayList<>();
			String id = pageBean.getId();
			if(!StringUtils.isEmpty(id)){
				predicates.add(cb.like(root.get("id"), StrUtil.appendLike(id)));
			}
			String sourceName = pageBean.getSourceName();
			if(!StringUtils.isEmpty(sourceName)){// 如果数据源名称不为空
				predicates.add(cb.like(root.get("source").get("name"), StrUtil.appendLike(sourceName)));
			}
			String tableName = pageBean.getTableName();
			if(!StringUtils.isEmpty(tableName)){ // 如果数据表名称不为空
				predicates.add(cb.like(root.get("name"), StrUtil.appendLike(tableName)));
			}
			Predicate ps[] = new Predicate[predicates.size()];
			// 只查询出结果中没有标志删除的数据
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return predicates.isEmpty()? notEqual:cb.and(cb.or(predicates.toArray(ps)),notEqual);
		},
				PageRequest.of(pageBean.getCurPage(), pageBean.getPageSize(), 
						new Sort(Direction.DESC,"addTime")));
		return new Response(Constant.SUCCESS_CODE,"查询成功",page);
	}

	@Override
	public Response getById(String id) {
		Optional<DataTable> optional = tabRepos.findById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",optional.get());
		return response;
	}

	@Override
	public Response getSource(String id) {
		Source s = tabRepos.findSourceById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",s);
		return response;
	}

	@Override
	public Response getTables(String id) {
		try {
			List<TableColumn> columns = tabRepos.findById(id).get().getColumns();
			return new Response(Constant.SUCCESS_CODE,"查询成功",columns);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,e.getMessage(),e);
		}
	}

}
