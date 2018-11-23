package dat.service;

import org.springframework.jdbc.core.JdbcTemplate;

import dat.domain.Source;
import dat.vo.PagingBean;
import dat.vo.Response;

public interface DataSourceService {

	Response list(PagingBean pagingBean);

	/**
	 * 添加新数据源
	 * @param source
	 * @return
	 */
	Response add(Source source);

	/**
	 * 修改已有数据源信息
	 * @param source
	 * @return
	 */
	Response update(Source source);

	/**
	 * 删除数据源
	 * @param id
	 * @return
	 */
	Response delete(String id);

	Response getById(String id);

	/**
	 * 获取指定id的数据源下的所有数据表
	 * @param id 数据源id
	 * @return	数据表查询结果
	 */
	Response getTablesById(String id);

	/**
	 * 获取操作数据源的jdbcTemplate对象
	 * @param source
	 * @return
	 */
	JdbcTemplate getTemplate(Source source);
}
