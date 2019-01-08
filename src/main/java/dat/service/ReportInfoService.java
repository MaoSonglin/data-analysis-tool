package dat.service;

import java.util.List;

import dat.domain.GraphInfo;
import dat.domain.ReportInfo;
import dat.vo.GraphDrillData;
import dat.vo.ReportPagingBean;
import dat.vo.Response;

public interface ReportInfoService {

	/**
	 * 根据pageInfo中封装的查询信息和分页信息查询
	 * @param pageInfo
	 * @return
	 */
	Response searchByPageInfo(ReportPagingBean pageInfo);

	/**
	 * 保存report
	 * @param report
	 * @return
	 */
	Response save(ReportInfo report);

	/**
	 * 根据ID查找报表信息，如果指定的ID不存在，返回null
	 * @param id	报表ID
	 * @return		id对应的报表，如果id不存在，返回null
	 */
	ReportInfo getById(String id);

	/**
	 * 获取指定ID的报表下所属的图表
	 * @param id	报表ID
	 * @return		图表信息数组对象
	 */
	List<GraphInfo> getGraphs(String id);

	/**
	 * 取消置顶ID的报表发布状态
	 * @param reportid
	 * @return
	 */
	Response unpublish(String reportid);

	Response delete(String id);

	Response getTableTree(String id);

	Response getData(GraphDrillData data);

	Response pulish(ReportInfo report);


}
