package dat.controller;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.PagingBean;
import dat.domain.Response;
import dat.domain.Source;
import dat.service.DataSourceService;

@RestController
@RequestMapping("/ds")
public class DSController {

	@Resource(name="dataSourceServiceImpl")
	private DataSourceService dsService;
	
	private static Logger log = Logger.getLogger(DSController.class);
	/**
	 * 分页查询数据源信息
	 * @param keyword	查询关键词
	 * @param curPage	当前页
	 * @param pageSize	每页显示大小
	 * @return	
	 */
	@RequestMapping(method=RequestMethod.GET,value={"","/{curPage}/{pageSize}/{keyword}"})
	public Response list(@PathVariable(value="keyword",required=false) String keyword,
			@PathVariable(value="curPage",required=false) Integer curPage,
			@PathVariable(value="pageSize",required=false) Integer pageSize){
		log.info("分页查询数据源：curPage="+curPage+"，pageSize="+pageSize+"，keywork="+keyword);
		Response response = dsService.list(new PagingBean(curPage,pageSize));
		return response;
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/{id}")
	public Response get(@PathVariable String id){
		Response response = dsService.getById(id);
		return response;
	}
	
	/**
	 * 添加数据源
	 * @param source
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST)
	public Response add(Source source){
		System.err.println(source);
		return dsService.add(source);
	}
	
	/**
	 * 修改数据源信息
	 * @param source
	 * @return
	 */
	@RequestMapping(method=RequestMethod.PUT)
	public Response update(Source source){
		return dsService.update(source);
	}
	
	/**
	 * 删除数据源信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value={"/{id}"},method=RequestMethod.DELETE)
	public Response delete(@PathVariable String id){
		log.debug(String.format("the value of id which will be deleted is %s", id));
		return dsService.delete(id);
	}
	
	/**
	 * 获取指定id的数据下所有数据表
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/{id}/tables",method=RequestMethod.GET)
	public Response getTables(@PathVariable String id){
		return dsService.getTablesById(id);
	}
	
}
