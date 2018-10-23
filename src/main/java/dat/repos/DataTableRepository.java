package dat.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dat.domain.DataTable;
import dat.domain.Source;

public interface DataTableRepository extends JpaRepository<DataTable, String>,
		JpaSpecificationExecutor<DataTable> {

	/**
	 * 检索出指定ID的数据表所对应的数据源
	 * @param id
	 * @return
	 */
	@Query("select s from Source s join DataTable d on d.source.id=s.id where d.id=:id")
	Source findSourceById(@Param("id") String id);

}
