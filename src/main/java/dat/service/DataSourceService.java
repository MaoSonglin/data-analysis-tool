package dat.service;

import dat.domain.PagingBean;
import dat.domain.Response;
import dat.domain.Source;

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

}
