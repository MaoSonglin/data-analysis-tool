package dat.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.repos.DataTableRepository;
import dat.repos.TableColumnRepository;
import dat.service.TableColumnService;
import dat.util.Constant;
import dat.util.StrUtil;
import dat.vo.Response;
import dat.vo.TableColumnPagingBean;

@Service
public class TableColumnServiceImpl implements TableColumnService {
	private static Logger logger = LoggerFactory.getLogger(TableColumnServiceImpl.class);
	@Resource(name="tableColumnRepository")
	TableColumnRepository colRepos;
	
	@Resource(name="dataTableRepository")
	DataTableRepository tabRepos;
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
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

	@Override
	public List<TableColumn> getColumnsByVirtualColumns(
			List<VirtualColumn> columns) {
		// 保存虚拟字段的id的集合
		Set<String> ids  = new HashSet<>();
		columns.forEach(virtualColumn->{
			String id = virtualColumn.getId();
			ids.add(id);
		});
		String sql ="\nselect \n"+
			    "    tablecolum1_.id as id \n"+
			    "from \n"+
			    "    virtual_column_ref_columns refcolumns0_ \n"+ 
			    "inner join \n"+
			    "    table_column tablecolum1_ \n"+ 
			    "        on refcolumns0_.ref_columns_id=tablecolum1_.id \n"+ 
			    "where \n"+
			    "    refcolumns0_.virtual_column_id in \n";
		StringBuffer sb = new StringBuffer(sql);
		sb.append('(');
		ids.forEach(elem->{
			sb.append('?').append(',');
		});
		sb.deleteCharAt(sb.length()-1);
		sb.append(')');
		logger.debug(sb+ids.toString());
		List<String> list = jdbcTemplate.query(sb.toString(), (rs,i)->{
			return rs.getString("id");
		}, ids.toArray());
		// 待查询的底层实体字段
		Set<TableColumn> tableColumns = colRepos.findByIdIn(list);
		return new ArrayList<>(tableColumns);
	}

}
