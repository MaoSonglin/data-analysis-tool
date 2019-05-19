package dat.repos;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dat.domain.ForeignKeyInfo;

public interface ForeignKeyInfoRepository extends JpaRepository<ForeignKeyInfo, Long>,
		JpaSpecificationExecutor<ForeignKeyInfo> {
	
	@Query("select f from ForeignKeyInfo f join TableColumn k on f.foreignKey.id = k.id where k.id=:id")
	ForeignKeyInfo findByForeignKeyId(@Param("id") String id);

	@Query("select f from ForeignKeyInfo f join TableColumn t on f.referencedColumn.id = t.id where t.id=:id")
	List<ForeignKeyInfo> findByReferencedColumnId(@Param("id") String id);
	
	@Query("select f from ForeignKeyInfo f join TableColumn t on f.referencedColumn.id = t.id join TableColumn c on f.foreignKey.id = c.id where t.id in (:ids) or c.id in (:ids)")
	Set<ForeignKeyInfo> findByTableColumnIds(@Param("ids") Iterable<String> ids);
}
