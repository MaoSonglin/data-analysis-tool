package dat.service;

import dat.domain.Category;
import dat.vo.PagingBean;
import dat.vo.Response;

public interface CategoryService {

	Response save(Category category);

	Response update(Category category);
	
	Response delete(Category category);
	
	Response delete(Long id);
	
	Response list(PagingBean pageBean);

	Category get(Long id);

	Response getByParentId(String id);
}


