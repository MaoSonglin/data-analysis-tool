package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.Reference;

public interface ReferenceRepository extends JpaRepository<Reference, Long>,
		JpaSpecificationExecutor<Reference> {

}
