package dat.controller;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Response;
import dat.domain.VirtualColumn;
import dat.service.VirtualColumnService;

@RestController
@RequestMapping("/vc")
public class VirtualColumnController {
	
	private static Logger logger = Logger.getLogger(VirtualColumnController.class);
	
	@Resource(name="virtualColumnServiceImpl")
	VirtualColumnService virtualColumnService;
	
	@DeleteMapping("/{id}")
	public Response del(@PathVariable String id){
		Response response = virtualColumnService.deleteById(id);
		return response;
	}
	
	@PutMapping("")
	public Response update(VirtualColumn column){
		logger.debug(column);
		Response response = virtualColumnService.save(column);
		return response;
	}
}
