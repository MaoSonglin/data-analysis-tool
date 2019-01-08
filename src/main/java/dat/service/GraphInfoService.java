package dat.service;

import java.util.List;

import dat.domain.GraphInfo;
import dat.vo.GraphDrillData;
import dat.vo.Response;
import dat.vo.TreeNode;

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
	
	List<List<String>> getData(String id) throws Exception;

	Object drill(GraphDrillData drillData) throws Exception;

	TreeNode findTree(String id);

	Response addColumn(String gpid, String vcid);

	Response getAthers(String id);
}
