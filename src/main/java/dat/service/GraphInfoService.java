package dat.service;

import dat.domain.GraphInfo;
import dat.vo.Response;
import dat.vo.TableDataPagingBean;

public interface GraphInfoService {

	/**
	 * 添加或者修改一副图表
	 * @param graphInfo
	 * @return
	 */
	Response save(GraphInfo graphInfo);

	/**
	 * 根据图表的ID获取图表信息
	 * @param id
	 * @return
	 */
	GraphInfo getById(String id);

	/**
	 * 删除图表信息
	 * @param id
	 * @return
	 */
	Response delete(String id);

	/**
	 * 获取绘制图表需要的数据
	 * @param g
	 * @return
	 * @throws Exception
	 */
	Response getData(TableDataPagingBean g) throws Exception;
}
