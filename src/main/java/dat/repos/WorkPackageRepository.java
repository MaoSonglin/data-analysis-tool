package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.WorkPackage;

public interface WorkPackageRepository extends JpaRepository<WorkPackage, String>,
		JpaSpecificationExecutor<WorkPackage> {

}
