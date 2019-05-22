package dat.controller;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.domain.GraphInfo;
import dat.service.GraphInfoService;
import dat.util.Constant;
import dat.vo.GraphDrillData;
import dat.vo.Response;
import dat.vo.SelectVo;
import dat.vo.TreeNode;

/**
 * @author MaoSonglin
 * 图表信息控制器
 */
@RestController
@RequestMapping("/graph")
public class GraphInfoController {
	private static Logger logger = Logger.getLogger(GraphInfoController.class);
	@Resource(name="graphInfoServiceImpl")
	private GraphInfoService graphInfoService;
	
	/**
	 * 根据图表的ID，返回图表信息
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public Response get(@PathVariable String id){
		// 调用服务层接口查找图表
		GraphInfo graphInfo = graphInfoService.getById(id);
		if (logger.isDebugEnabled()) {
		}
		if(graphInfo != null){// 查找成功
			return new Response(Constant.SUCCESS_CODE,"查询成功",graphInfo);
		}
		// 查找失败
		return new Response(Constant.ERROR_CODE,"ID为"+id+"的图表不存在",id);
	}
	
	/**
	 * 添加一副图，或者修改一幅图。如果graphinfo的ID为null则添加，否则为修改
	 * @param graphInfo
	 * @return
	 */
	@PostMapping()
	public Response add(GraphInfo graphInfo){
		// 检查图表的标题是否为null或者空字符
		String title = graphInfo.getTitle();
		if(title == null || title.trim().isEmpty()){
			return new Response(Constant.ERROR_CODE,"新建图表标题不能为空",graphInfo);
		}
		// 如果图表的ID为null说明是新建的一副图表，需要生成ID
		if(graphInfo.getId()==null)
			graphInfo.generateId();
		// 调用服务层接口保存图表
		Response save = graphInfoService.save(graphInfo);
		return save;
	}
	
	/**
	 * 根据图表的ID删除图表信息
	 * @param id
	 * @return
	 */
	@DeleteMapping("/{id}")
	public Response delete(@PathVariable String id){
		Response response = graphInfoService.delete(id);
		return response;
	}
	
	/**
	 * 为图表添加数据字段
	 * @param gpid	图表id
	 * @param vcid	字段ID
	 * @param axis	维度
	 * @return	
	 * @throws Exception
	 */
	@GetMapping("/{gpid}/{vcid}")
	public Response addx(@PathVariable String gpid,
			@PathVariable String vcid) throws Exception{
		// 根据ID获取待编辑的图表
		Response response = graphInfoService.addColumn(gpid,vcid);
		return response;
	}
	
	@DeleteMapping("/{graphId}/{vcid}")
	public Response deletex(@PathVariable String graphId, @PathVariable String vcid){
		GraphInfo graphInfo = graphInfoService.dropColumn(graphId,vcid);
		return new Response(Constant.SUCCESS_CODE,"删除成功",graphInfo);
	}
	
	
	@GetMapping({"/data/{graphId}/{curPage}/{pageSize}","/data/{graphId}"})
	public Response getData(@PathVariable String graphId) throws Exception{
		try {
//			List<List<String>> data = graphInfoService.getData(graphId);
			DataTable<DataMap> dataTable = graphInfoService.getGraphData(graphId);
			return new Response(Constant.SUCCESS_CODE,"查询成功",dataTable);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,e.getMessage());
		}
	}
	
	@GetMapping("/tree/{id}")
	public Response getTree(@PathVariable String id){
		if(id == null)
			return new Response(Constant.ERROR_CODE,"请输入图表ID", id);
		TreeNode tree = graphInfoService.findTree(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",tree);
		return response;
	}
	
	@RequestMapping("/drill")
	public Object drill(GraphDrillData drillData) throws Exception{
		logger.debug(drillData);
		Object drill = graphInfoService.drill(drillData);
		return drill;
	}
	
	@RequestMapping("/source")
	public Object getSource(SelectVo selectVo){
		if(selectVo.getGraphId() == null)
			return new Response(Constant.ERROR_CODE,"请输入查询图表ID",null);
		DataTable<DataMap> data = graphInfoService.getData(selectVo);
		
		return new Response(Constant.SUCCESS_CODE,"查询成功",data);
	}
	
	
	
	/**
	 * @param id
	 * @return 获取同一个报表中的其他图
	 */
	@RequestMapping("/athers")
	public Response athers(String id){
		Response response = graphInfoService.getAthers(id);
		return response;
	}
	
}
