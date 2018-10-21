package dat.service;

import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.PagingBean;
import dat.domain.Source;
import dat.pojo.Response;
import dat.repos.CustomerSpecs;
import dat.repos.DsRepository;
import dat.util.BeanUtil;
import dat.util.Constant;
import dat.util.SourceMetaData.SourceType;
import dat.util.StrUtil;

@Service
public class DataSourceServiceImpl implements DataSourceService {
	
	/**
	 * DataSource没有标记为删除
	 */
	private static Specification<Source> stateNotDelete = new Specification<Source>(){
		private static final long serialVersionUID = 5425423914749818457L;
		public Predicate toPredicate(Root<Source> root,
				CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
			Predicate notEqual = criteriaBuilder.notEqual(root.get("state"), 0);
			return notEqual;
		}};
		
	@Resource(name="dsRepository")
	DsRepository dsRepos;
	@Autowired
	EntityManager entityManager;
	@Override
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

	

	@Override
	@Transactional
	public Response add(Source source) {
		String name = source.getName();
		// 检查数据源的名称是否已经存在
		boolean existsByName = dsRepos.existsByName(name);
		if(existsByName){
			return new Response(Constant.ERROR_CODE,String.format("数据源名称“%s”已经存在", name),source);
		}
		// 为数据源设置新的ID
		source.generateId();
		source.setAddTime(StrUtil.currentTime());
		Source save = dsRepos.save(source);
		// TODO 1. 读取数据源中的数据表
		// TODO 2. 读取数据表之间的关联关系
		return new Response(Constant.SUCCESS_CODE,"添加成功！",save);
	}

	@Override
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

	@Override
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

}
