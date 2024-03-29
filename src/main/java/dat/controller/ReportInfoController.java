package dat.controller;

import java.util.List;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.GraphInfo;
import dat.domain.ReportInfo;
import dat.service.ReportInfoService;
import dat.util.Constant;
import dat.vo.GraphDrillData;
import dat.vo.ReportPagingBean;
import dat.vo.Response;

@RestController
@RequestMapping("/report")
public class ReportInfoController {
	
	private static Logger logger = Logger.getLogger(ReportInfoController.class);
	
	@Resource(name="reportInfoServiceImpl")
	ReportInfoService reportInfoService;
	
	/**
	 * 获取指定ID的报表
	 * @param id
	 * @return
	 */
	@GetMapping()
	public Response get(String id){
		if(!id.matches("^RP\\d{16}$")){
			return new Response(Constant.ERROR_CODE,"ID格式错误",id);
		}
		ReportInfo reportInfo = reportInfoService.getById(id);
		return new Response(Constant.SUCCESS_CODE,"查询成功",reportInfo);
	}
	
	/**
	 * 分页查询
	 * @param pageInfo
	 * @return
	 */
	@GetMapping("/list")
	public Response list(ReportPagingBean pageInfo){
		logger.debug(pageInfo);
		Response response = reportInfoService.searchByPageInfo(pageInfo);
		return response;
	}
	
	/**
	 * 获取指定ID的报表中的所有图表信息
	 * @param id
	 * @return
	 */
	@GetMapping("/graphs/{id}")
	public Response getGraphs(@PathVariable String id){
		// 根据报表ID查询出报表信息
		List<GraphInfo> graphs = reportInfoService.getGraphs(id);
		if(logger.isDebugEnabled()){
		}
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",graphs);
		return res;
	}
	
	/**
	 * 修改或者添加报表
	 * @param report
	 * @return
	 */
	@PostMapping()
	public Response save(ReportInfo report){
		logger.debug(report);
		String name = report.getName();
		if(name == null || name.trim().isEmpty()){
			return new Response(Constant.ERROR_CODE,"报表名称不能为空",report);
		}
		if(report.getId() == null)
			report.generateId();
		Response save = reportInfoService.save(report);
		return save;
	}
	
//	@RequestMapping("publish/")
//	public Response publish(@PathVariable String reportid,@PathVariable  Integer menuid){
//		Response response = reportInfoService.pulish(reportid,menuid);
//		return response;
//	}
	
	@RequestMapping("/publish")
	public Response publish(ReportInfo report){
		Response response = reportInfoService.pulish(report);
		return response;
	}
	
	@RequestMapping("unpublish/{reportid}")
	public Response unpublish(@PathVariable String reportid){
		return reportInfoService.unpublish(reportid);
	}
	
	
	@DeleteMapping()
	public Response delete(String id){
		Response response = reportInfoService.delete(id);
		return response;
	}
	
	
	@GetMapping("/tree/{id}")
	public Response tree(@PathVariable String id){
		Response response = reportInfoService.getTableTree(id);
		return response;
	}
	
	@RequestMapping("/data")
	public Response data(GraphDrillData data){
		return reportInfoService.getData(data);
	}
	
}
