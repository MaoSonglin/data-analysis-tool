package dat.repos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.controller.CategoryController;
import dat.domain.Category;

@ConditionalOnBean(CategoryController.class)
public interface CategoryRepository extends JpaRepository<Category, Long>,
		JpaSpecificationExecutor<Category> {

	Category findByName(String name);
	
	boolean existsByName(String name);

}
