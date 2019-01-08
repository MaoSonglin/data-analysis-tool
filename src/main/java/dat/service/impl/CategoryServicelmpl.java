package dat.service.impl;

import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.Category;
import dat.repos.CategoryRepository;
import dat.service.CategoryService;
import dat.util.Constant;
import dat.vo.PagingBean;
import dat.vo.Response;

//@Service
public class CategoryServicelmpl implements CategoryService{

	@Autowired
	private CategoryRepository categoryRepos;
	
	@Override
	@Transactional
	public Response save(Category category) {
//		Category c = categoryRepos.findByName(category.getName());
		if(category.getId() == null && categoryRepos.existsByName(category.getName())){
			return new Response(Constant.ERROR_CODE,"分类名已存在",category);
		}
		Category save = categoryRepos.save(category);
		List<Category> parents = category.getParents();
		save(save, parents);
		List<Category> children = category.getChildren();
		save(save,children);
		return new Response(Constant.SUCCESS_CODE,"添加成功",save);
	}

	/**
	 * @param save
	 * @param parents
	 */
	private void save(Category save, List<Category> parents) {
		if(parents != null){
			for (Category category2 : parents) {
				Category c = categoryRepos.findById(category2.getId()).get();
//				save.setParents(parents);
				c.getChildren().add(save);
				categoryRepos.save(c);
			}
		}
	}
	
	@Transactional
	public Response update(Category category){
//		Category c = categoryRepos.findById(category.getId()).orElse(null);
		if(!categoryRepos.existsById(category.getId())){
			return new Response(Constant.ERROR_CODE,"分类ID不存在");
		}
		List<Category> parents = category.getParents();
		Category save = categoryRepos.save(category);
		save(save,parents);
		save(save,category.getChildren());
		Response response = new Response(Constant.SUCCESS_CODE,"修改成功",save);
		
		return response;
	}

	@Override
	@Transactional
	public Response delete(Category category) {
		if(!categoryRepos.existsById(category.getId())){
			return new Response(Constant.ERROR_CODE,"分类ID不存在");
		}
		categoryRepos.delete(category);
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}

	@Override
	@Transactional
	public Response delete(Long id) {
		if(!categoryRepos.existsById(id)){
			return new Response(Constant.ERROR_CODE,"分类ID不存在");
		}
		categoryRepos.deleteById(id);
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}

	@Override
	public Response list(PagingBean pageBean) {
		String keyword = pageBean.getKeyword();
		PageRequest request = PageRequest.of(pageBean.getCurPage()-1, pageBean.getPageSize());
		Page<Category> page = categoryRepos.findAll((root,query,cb)->{
			if(keyword != null && ! keyword.trim().isEmpty()){
				return cb.like(root.get("name"), "%"+keyword+"%");
			}else{
				return cb.isNotNull(root.get("name"));
			}
		},request);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",page);
		return response;
	}

	@Override
	public Category get(Long id) {
		return categoryRepos.findById(id).orElse(null);
	}

	@Override
	public Response getByParentId(String id) {
		List<Category> list = categoryRepos.findAll((root,query,cb)->{
			
			Join<Category, Category> join = root.join("parents",JoinType.LEFT);
			Path<Object> path = join.get("id");
			if(id != null && !id.trim().isEmpty())
				return cb.equal(path, id);
			else
				return cb.isNull(path);
		});
		return new Response(Constant.SUCCESS_CODE,"查询成功",list);
	}
	
}
