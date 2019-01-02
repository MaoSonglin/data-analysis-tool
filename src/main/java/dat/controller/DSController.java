package dat.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Source;
import dat.service.DataSourceService;
import dat.util.Constant;
import dat.vo.ExcelSheet;
import dat.vo.PagingBean;
import dat.vo.Response;

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
	@RequestMapping(method=RequestMethod.GET,value={"/page","/{curPage}/{pageSize}/{keyword}"})
	public Response list( String keyword,
			Integer curPage,
			 Integer pageSize){
		log.info("分页查询数据源：curPage="+curPage+"，pageSize="+pageSize+"，keywork="+keyword);
		Response response = dsService.list(new PagingBean(curPage-1,pageSize));
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
	 * @throws Exception 
	 */
	@RequestMapping(method=RequestMethod.POST)
	public Response add(Source source) throws Exception{
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
	
	
	@GetMapping("/excel")
	public Response getSheets(String id) throws Exception{
		List<ExcelSheet> sheets = dsService.getExcelSheet(id);
		return new Response(Constant.SUCCESS_CODE,"查询成功",sheets);
	}
	
	/**
	 * 获取指定ID的Excel数据源中表sheetName的第row行数据
	 * @param id			数据源id，该ID必须是Excel数据源的id
	 * @param sheetName		工作簿名称
	 * @param row			工作簿中的指定的数据行
	 * @return
	 * @throws IOException 
	 */
	@GetMapping("/excel/{id}/{sheetName}/{row}")
	public Response getExcelSheetRow(@PathVariable String id,@PathVariable String sheetName,@PathVariable Integer row) throws IOException{
		List<String> list = dsService.getSpecifyRow(id,sheetName,row);
		return new Response(Constant.SUCCESS_CODE,"查询成功",list);
		
	}
	
	@PostMapping("/excel")
	public Response sheet(@RequestBody List<ExcelSheet> sheets,String id){
		Source source = dsService.extract(id,sheets);
		return new Response(Constant.SUCCESS_CODE,"提取成功",source);
	}
	
}
