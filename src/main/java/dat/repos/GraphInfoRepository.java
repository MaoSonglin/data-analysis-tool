package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.GraphInfo;

public interface GraphInfoRepository extends JpaRepository<GraphInfo, String> ,JpaSpecificationExecutor<GraphInfo>{

}
