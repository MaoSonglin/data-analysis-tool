package dat.controller;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.service.DataTableService;
import dat.vo.DataTablePaingBean;
import dat.vo.Response;

@RestController @RequestMapping("/tab")
public class DataTableController {
	
	private static Logger log = LogManager.getLogger(DataTableController.class);
	
	@Resource(name="dataTableServiceImpl")
	DataTableService tabService;
	
	/**
	 * 分页查询数据表内容，对外暴露接口：/tab，请求方式必须为GET
	 * @param pageBean	封装了了请求查询的页码（从0开始）和每页显示数据量，查询过滤条件数据表名称和数据表所属数据源名称
	 * @return	查询结果
	 */
	@RequestMapping(method=RequestMethod.GET)
	public Response list(DataTablePaingBean pageBean){
		log.info(pageBean);
		Response search = tabService.search(pageBean);
		return search;
	}
	

	/**
	 * 获取指定id的数据表，对外访问接口/tab/{id}，请求方式GET
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/{id}",method=RequestMethod.GET)
	public Response get(@PathVariable String id){
		Response byId = tabService.getById(id);
		return byId;
	}
	
	/**
	 * 获取指定ID的数据表所属的数据源信息，对外接口/tab/source/{id}，请求方式只能是GET
	 * @param id	待查询的数据表的id
	 * @return		查询结果
	 */
	@RequestMapping(value="/source/{id}",method=RequestMethod.GET)
	public Response getSource(@PathVariable String id){
		
		return tabService.getSource(id);
	}
	
	/**
	 * 获取指定ID的数据表下的所有的列
	 * @return
	 */
	@GetMapping("/{id}/columns")
	public Response getTables(@PathVariable String id){
		return tabService.getTables(id);
	}
}
