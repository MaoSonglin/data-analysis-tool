package dat.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;

public interface TableColumnRepository extends JpaRepository<TableColumn, String>,
		JpaSpecificationExecutor<TableColumn> {
	
	/**
	 * 查询指定ID的字段所对应的数据表信息
	 * @param id	字段ID
	 * @return		字段所属的数据表
	 */
	@Query("select d from DataTable d join TableColumn c on c.dataTable.id=d.id where c.id=:id")
	List<DataTable> findDataTableById(@Param("id") String id);
	
	/**
	 * 查询指定ID的字段所属的数据源信息
	 * @param id	字段的id
	 * @return		数据源信息
	 */
	@Query("select s from Source s join DataTable d on s.id=d.source.id join TableColumn c on c.dataTable.id=d.id where c.id=:id")
	Source findDsById(@Param("id") String id);
}
