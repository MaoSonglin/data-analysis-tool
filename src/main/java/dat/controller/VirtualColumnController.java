package dat.controller;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.VirtualColumn;
import dat.service.VirtualColumnService;
import dat.util.Constant;
import dat.vo.ClassifyFormula;
import dat.vo.Response;
import dat.vo.VirtualColumnParam;

@RestController
@RequestMapping("/vc")
public class VirtualColumnController {
	
	private static Logger logger = Logger.getLogger(VirtualColumnController.class);
	
	@Resource(name="virtualColumnServiceImpl")
	VirtualColumnService virtualColumnService;
	
	@GetMapping("/{id}")
	public Response get(@PathVariable String id){
		VirtualColumn virtualColumn = virtualColumnService.getById(id);
		Response res = new Response(Constant.SUCCESS_CODE,"查询成功",virtualColumn);
		return res;
	}
	
	@DeleteMapping("/{id}")
	public Response del(@PathVariable String id){
		Response response = virtualColumnService.deleteById(id);
		return response;
	}
	
	@PutMapping
	public Response update(VirtualColumn column){
		Response response = virtualColumnService.save(column);
		return response;
	}
	
	@PostMapping
	public Response createField(VirtualColumn vt){
		if(logger.isDebugEnabled()){
			logger.debug(vt);
			logger.debug(vt.getRefColumns());
		}
		return virtualColumnService.createField(vt);
	}
	
	@GetMapping("/list")
	public Object list(VirtualColumnParam param){
		virtualColumnService.getByPage(param);
		return param;
	}
	
	
	@RequestMapping("/add/classify")
	public Response classifyField(VirtualColumn column,ClassifyFormula classifyFormula){
		return virtualColumnService.createField(column,classifyFormula);
	}
	
}
