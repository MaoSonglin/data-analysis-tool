package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import dat.util.MetaDataParser;
import dat.util.MetaDataParser.SourceMetaDataException;
import dat.util.StrUtil;

@Service
public class DataSourceServiceImpl implements DataSourceService {
	
	private static Logger log = Logger.getLogger(DataSourceServiceImpl.class);
	
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
		log.debug(pagingBean);
		// 构建条件查询接口
		Specification<Source> spec = CustomerSpecs.byKeyWord(Source.class,entityManager, pagingBean.getKeyword());
		// 连接状态查询条件，过滤掉已经标记为删除状态的数据
		Specification<Source> specification = spec.and(stateNotDelete);
		// 构造分页接口
		PageRequest pageRequest = PageRequest.of(pagingBean.getCurPage(), pagingBean.getPageSize(),new Sort(Direction.ASC,"id"));
		// 调用jpa接口查询
		Page<Source> page = dsRepos.findAll(specification,pageRequest);
//		List<Source> findAll = dsRepos.findAll(specification);
		log.debug("查询到记录条数："+page.getNumberOfElements());
		// 返回数据类型
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		return res;
	}

	

	@Transactional
	public Response add(Source source) {
		try {
			// 检查数据源的属性是否存在冲突
			checkAttribute(source);
		} catch (Exception e) {
			log.info(e.getMessage());
			return new Response(Constant.ERROR_CODE,e.getMessage(),e);
		}
		// 为数据源设置新的ID
		source.generateId();
		source.setAddTime(StrUtil.currentTime());
		
		Response response = new Response();
		try {
			// 读取数据源中的元数据
			MetaDataParser sourceMetaData = MetaDataParser.getSourceMetaData(source);
			if(!sourceMetaData.testConnection()){
				return new Response(Constant.ERROR_CODE,"数据源连接失败，请检查数据源配置是否正确！");
			}
			// 保存数据源中的表格和字段
			saveTableAndColumn(sourceMetaData);
		} catch (SourceMetaDataException e) {
			log.info(e.getMessage());
			response.put("tableInfo", e.getMessage());
		}
		// TODO 2. 读取数据表之间的关联关系
		
		// 保存数据源信息
		Source save = dsRepos.save(source);
		
		response.setCode(Constant.SUCCESS_CODE);
		response.setMessage("保存成功");
		response.setData(save);
		
		return response;
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

}
