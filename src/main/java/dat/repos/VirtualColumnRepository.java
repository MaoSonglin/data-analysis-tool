package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.VirtualColumn;

public interface VirtualColumnRepository extends JpaRepository<VirtualColumn, String>,
		JpaSpecificationExecutor<VirtualColumn> {

	List<VirtualColumn> findByName(String name);

}
