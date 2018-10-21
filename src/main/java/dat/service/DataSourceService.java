package dat.service;

import dat.domain.PagingBean;
import dat.domain.Source;
import dat.pojo.Response;

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

}
