package dat.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Response;
import dat.domain.WorkPackage;
import dat.service.WorkPackageService;


@RestController
@RequestMapping("/pkg")
public class WorkPackageController {
	
	@Resource(name="workPackageServiceImpl")
	private WorkPackageService wpService;
	/**
	 * 根据id返回工作包
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public Response get(@PathVariable String id){
		Response response = wpService.getPackage(id);
		return response;
	}
	
	/**
	 * 获取指定ID的数据包包含的数据表
	 * @param id
	 * @return
	 */
	@GetMapping("/tab/{id}")
	public Response getTables(@PathVariable String id){
		return wpService.getTables(id);
	}
	
	/**
	 * 添加数据包
	 * @param pg
	 * @return
	 */
	@PostMapping()
	public Response add(WorkPackage pg){
		// TODO 添加工作包
		return wpService.add(pg);
	}
	
	/**
	 * 为ID为pid的数据包添加一个ID为tid的数据，不会将ID为tid的数据表指定到pid的数据包中，而是
	 * 建立一个虚拟数据表，将tid中的数据字段存放到虚拟数据表中
	 * @param pid
	 * @param tid
	 * @return
	 */
	@PutMapping("/addTab")
	public Response addTab(String pid,String tid){
		// TODO 为数据包添加一个数据表
		return wpService.addTab(pid,tid);
	}
	
	/**
	 * 为pid对应的数据包移除ID为tid的虚拟数据表
	 * @param pid	数据包id
	 * @param tid	虚拟数据表ID
	 * @return
	 */
	public Response rm(String pid,String tid){
		return wpService.remove(pid,tid);
	}
}
