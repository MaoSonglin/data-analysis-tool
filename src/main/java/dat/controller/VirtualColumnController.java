package dat.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Response;
import dat.domain.VirtualColumn;
import dat.service.VirtualColumnService;

@RestController("/vc")
public class VirtualColumnController {

	@Resource(name="virtualColumnServiceImpl")
	VirtualColumnService virtualColumnService;
	
	@DeleteMapping("/{id}")
	public Response del(@PathVariable String id){
		virtualColumnService.deleteById(id);
		return null;
	}
	
	@PutMapping("")
	public Response update(VirtualColumn column){
		return null;
	}
}
