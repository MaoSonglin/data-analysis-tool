package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.ReportInfo;

public interface ReportInfoRepository extends JpaRepository<ReportInfo, String>,
		JpaSpecificationExecutor<ReportInfo> {

}
