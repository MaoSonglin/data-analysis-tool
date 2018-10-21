package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.Menu;

public interface MenuRepository extends JpaRepository<Menu, Integer> ,JpaSpecificationExecutor<Menu>{

	List<Menu> findByParentIsNull();
}
