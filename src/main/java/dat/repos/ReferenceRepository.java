package dat.repos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.controller.ReferenceController;
import dat.domain.Reference;

//@SuppressWarnings("deprecation")
@ConditionalOnBean(ReferenceController.class)
public interface ReferenceRepository extends JpaRepository<Reference, Long>,
		JpaSpecificationExecutor<Reference> {

}
