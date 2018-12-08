package dat.repos;

import java.util.Collection;
import java.util.List;

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

	@Query("select tb from DataTable tb ")//join TableColumn col on tb.id=col.dataTable.id where col.id in (select vc.refColumns.id from VirtualColumn vc join VirtualTable vt on vc.table.id=vt.id where vt.id in :ids)")
//	@Query("select tb from DataTable tb join TableColumn col on tb.id=col.dataTable.id join VirtualColumn vc join fetch vc.refColumns as c2 on col.id=c2.id join VirtualTable vt on vc.table.id=vt.id where vt.id in :ids")
	List<DataTable> findByVirtualTableIds(@Param("ids") Collection<String> ids);
	
	
	List<DataTable> findByIdIn(Collection<String> ids);
}
