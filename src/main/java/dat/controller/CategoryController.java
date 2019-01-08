package dat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dat.domain.Category;
import dat.service.CategoryService;
import dat.util.Constant;
import dat.vo.PagingBean;
import dat.vo.Response;

//@RestController
@RequestMapping("/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;
	
	@GetMapping("/{id}")
	public Response get(@PathVariable Long id){
		Category category = categoryService.get(id);
		
		return new Response(category != null ? Constant.SUCCESS_CODE : Constant.ERROR_CODE,category != null ? "查询成功":"查询失败",category);
	}
	
	@PostMapping()
	public Response save(Category category){
		return categoryService.save(category);
	}
	
	@PutMapping()
	public Response update(Category category){
		return categoryService.update(category);
	}
	
	@DeleteMapping()
	public Response delete(Category category){
		return categoryService.delete(category);
	}
	
	
	@DeleteMapping("/{id}")
	public Response delete(@PathVariable Long id){
		return categoryService.delete(id);
	}
	
	@RequestMapping("/page")
	public Response page(PagingBean page){
		return categoryService.list(page);
	}
	
	@RequestMapping("/byParentId")
	public Response getByParentId(String id){
		return categoryService.getByParentId(id);
	}
}
