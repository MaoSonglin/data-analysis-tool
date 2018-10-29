package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.VirtualColumn;

public interface VirtualColumnRepository extends JpaRepository<VirtualColumn, String>,
		JpaSpecificationExecutor<VirtualColumn> {

}
