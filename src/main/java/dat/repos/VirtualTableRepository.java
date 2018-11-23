package dat.repos;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;

public interface VirtualTableRepository extends JpaRepository<VirtualTable, String>,
		JpaSpecificationExecutor<VirtualTable> {

	/**
	 * 根据给定的虚拟数据表ID查询该数据表下包含的虚拟数据字段
	 * @param id
	 * @return
	 */
	@Query("select c from VirtualColumn c join VirtualTable v on c.table.id=v.id where v.id=:id and c.state=1")
	List<VirtualColumn> getColumnsWithId(@Param("id") String id);

	
	@Query("select t from VirtualTable t join VirtualColumn c on c.table.id=t.id where c.id in :ids")
	List<VirtualTable> queryByColumnIdIn(@Param("ids") Collection<String> ids);

}
