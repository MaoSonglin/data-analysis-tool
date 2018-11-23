package dat.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.service.TableColumnService;
import dat.vo.Response;
import dat.vo.TableColumnPagingBean;

@RestController
@RequestMapping("/col")
public class ColumnController {
	
	@Resource(name="tableColumnServiceImpl")
	private TableColumnService tabColService;
	
	/**
	 * 对外查询接口，有分页查询的功能，pageBean中封装查询条件
	 * @param pageBean 封装了curPage：要显示的页面，下标从0开始，pageSize：每页显示记录条数 ，
	 * columnName：匹配条件列名称，tableName：匹配条件字段所属数据表名称，sourceName：匹配条件字段所属数据源名称
	 * @return	查询结果
	 */
	@RequestMapping(method=RequestMethod.GET)
	public Response list(TableColumnPagingBean pageBean){
		Response response = tabColService.search(pageBean);
		return response;
	}
	
	
	/**
	 * 获取指定ID的数据字段信息
	 * @param id 数据字段的ID
	 * @return	查询结果
	 */
	@RequestMapping(value="/{id}",method=RequestMethod.GET)
	public Response get(@PathVariable String id){
		Response response = tabColService.getById(id);
		return response;
	}
	
	/**
	 * 获取指定ID的字段所属的数据表
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/tab/{id}",method=RequestMethod.GET)
	public Response table(@PathVariable String id){
		Response reponse = tabColService.getTableById(id);
		return reponse;
	}
	
	/**
	 * 指定ID的字段所属的数据源
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/source/{id}",method=RequestMethod.GET)
	public Response source(@PathVariable String id){
		Response response = tabColService.getSourceById(id);
		return response;
	}
}
