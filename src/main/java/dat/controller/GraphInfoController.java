package dat.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.GraphInfo;
import dat.domain.VirtualColumn;
import dat.service.GraphInfoService;
import dat.util.Constant;
import dat.vo.Response;

/**
 * @author MaoSonglin
 * 图表信息控制器
 */
@RestController
@RequestMapping("/graph")
public class GraphInfoController {
	
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
	
//	@PutMapping()
	public Response modify(GraphInfo graphInfo){
		
		return null;
	}
	
	/**
	 * 为图表添加数据字段
	 * @param gpid	图表id
	 * @param vcid	字段ID
	 * @param axis	维度
	 * @return	
	 * @throws Exception
	 */
	@GetMapping("/{gpid}/{vcid}/{axis}")
	public Response addx(@PathVariable String gpid,
			@PathVariable String vcid,@PathVariable String axis) throws Exception{
		// 根据ID获取待编辑的图表
		GraphInfo graphInfo = graphInfoService.getById(gpid);
		if(graphInfo == null){
			// 图表不存在
			return new Response(Constant.ERROR_CODE,"ID为"+gpid+"的图表不存在");
		}
		// 字段
		VirtualColumn vc = new VirtualColumn();
		vc.setId(vcid);
		if("xAxis".equals(axis)) // 添加到x轴
			graphInfo.getxAxis().add(vc);
		else if("yAxis".equals(axis))// 添加到y轴
			graphInfo.getyAxis().add(vc);
		else
			throw new IllegalArgumentException("axis should be one of xAxis or yAxis, not "+axis);
		// 保存图表信息
		Response save = graphInfoService.save(graphInfo);
		return save;
	}
	
	/**
	 * 从某个图表中删除某个字段
	 * @param pgid
	 * @param vcid
	 * @param axis
	 * @return
	 */
	@DeleteMapping("/{pgid}/{vcid}/{axis}")
	public Response rmAxis(@PathVariable String pgid,
			@PathVariable String vcid,@PathVariable String axis){
		GraphInfo graphInfo = graphInfoService.getById(pgid);
		List<VirtualColumn> list = null;
		if("xAxis".equals(axis)) {
			list = graphInfo.getxAxis();
		} else if("yAxis".equals(axis)) {
			list = graphInfo.getyAxis();
		} else
			throw new IllegalArgumentException("axis should be one of xAxis or yAxis, not "+axis);
		boolean b = list.removeIf(elem -> {
			return elem.getId().equals(vcid);
		});
		if(!b){
			return new Response(Constant.ERROR_CODE,"ID为"+vcid+"的字段不存在于图表中");
		}
		Response response = graphInfoService.save(graphInfo);
		return response;
	}
	
	@GetMapping("/data")
	public Response getData(GraphInfo g) throws Exception{
		return graphInfoService.getData(g);
	}
}
