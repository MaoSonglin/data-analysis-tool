package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.Source;

public interface DsRepository extends JpaRepository<Source, String>,
		JpaSpecificationExecutor<Source> {
	List<Source> findByName(String name);
	
	boolean existsByName(String name);
}
