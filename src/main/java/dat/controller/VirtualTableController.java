package dat.controller;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Response;
import dat.domain.VirtualTable;
import dat.service.VirtualTableService;

/**
 * @author MaoSonglin
 * 虚拟数据表对外接口
 */
@RestController
@RequestMapping("/vt")
public class VirtualTableController {
	
	private static Logger logger = Logger.getLogger(VirtualTableController.class);
	/**
	 * 虚拟数据表服务成接口
	 */
	@Resource(name="virtualTableServiceImpl")
	VirtualTableService vtService;
	
	/**
	 * 获取指定ID的虚拟数据表
	 * @return
	 */
	@GetMapping("/{id}")
	public Response get(@PathVariable String id){
		return vtService.getById(id);
	}
	
	/**
	 * 获取指定ID的虚拟数据表中的虚拟字段
	 * @param id
	 * @return
	 */
	@GetMapping("/vc/{id}")
	public Response getCols(@PathVariable String id){
		return vtService.getVirtualColumns(id);
	}
	
	@Deprecated
	@PostMapping()
	public Response add(VirtualTable table){
		logger.debug(table);
		return vtService.add(table);
	}
	
}
