package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dat.domain.Relevance;

public interface RelevanceRepository extends JpaRepository<Relevance, Long>,
		JpaSpecificationExecutor<Relevance> {

}
