package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.DataTable;
import dat.domain.PagingBean;
import dat.domain.Response;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.repos.CustomerSpecs;
import dat.repos.DataTableRepository;
import dat.repos.DsRepository;
import dat.repos.TableColumnRepository;
import dat.service.DataSourceService;
import dat.util.BeanUtil;
import dat.util.Constant;
import dat.util.SourceMetaData;
import dat.util.StrUtil;

@Service
public class DataSourceServiceImpl implements DataSourceService {
	
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
		Specification<Source> spec = CustomerSpecs.byKeyWord(Source.class,entityManager, pagingBean.getKeyword());
		// 连接状态查询条件，过滤掉已经标记为删除状态的数据
		Specification<Source> specification = spec.and(stateNotDelete);
		// 构造分页接口
		PageRequest pageRequest = PageRequest.of(pagingBean.getCurPage(), pagingBean.getPageSize(),new Sort(Direction.ASC,"id"));
		// 调用jpa接口查询
		Page<Source> page = dsRepos.findAll(specification,pageRequest);
		// 返回数据类型
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		return res;
	}

	

	@Transactional
	public Response add(Source source) {
		String name = source.getName();
		// 检查数据源的名称是否已经存在
		boolean existsByName = dsRepos.existsByName(name);
		if(existsByName){
			return new Response(Constant.ERROR_CODE,String.format("数据源名称“%s”已经存在", name));
		}
		if( source.getUrl() != null ){
			List<Source> list = dsRepos.findByUrl(source.getUrl());
			if(!list.isEmpty()){
				Source s = list.get(0);
				return new Response(Constant.ERROR_CODE,
						String.format("URL:\"%s\"对应的数据源已存在与数据源\"%s\"中", 
								source.getUrl(),s.getName()));
			}
		}
		// 为数据源设置新的ID
		source.generateId();
		source.setAddTime(StrUtil.currentTime());
		// TODO 1. 读取数据源中的数据表
		SourceMetaData sourceMetaData = SourceMetaData.getSourceMetaData(source);
		if(!sourceMetaData.testConnection()){
			return new Response(Constant.ERROR_CODE,"数据源连接失败，请检查数据源配置是否正确！");
		}
		Source save = dsRepos.save(source);
		List<DataTable> tables = sourceMetaData.getTables();
		tabRepos.saveAll(tables);
		List<TableColumn> list = new ArrayList<>();
		for (DataTable dataTable : tables) {
			List<TableColumn> columns = sourceMetaData.getColumnOfTable(dataTable);
			list.addAll(columns);
		}
		colRepos.saveAll(list);
		// TODO 2. 读取数据表之间的关联关系
		
		Response response = new Response(Constant.SUCCESS_CODE,"添加成功！",save);
		return response;
	}

	@Transactional
	public Response update(Source source) {
		// 根据数据源的ID号查找到要修改的对象
		try {
			Optional<Source> optional = dsRepos.findById(source.getId());
			Source s = optional.get();
			BeanUtil.copyAttributes(source, s);
			// 如果对象对象存在
			return new Response(Constant.SUCCESS_CODE,"修改成功！",dsRepos.save(source));
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"修改失败",e.getMessage());
		}
	}

	@Transactional
	public Response delete(String id) {
		Optional<Source> optional = dsRepos.findById(id);
		try {
			Source source = optional.get();
			source.setState(Constant.DELETE_STATE);
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
			return new Response(Constant.SUCCESS_CODE,"查询成功",tables);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,"查询失败",e);
		}
	}

}
