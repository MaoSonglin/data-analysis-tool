package dat.controller;

import java.util.List;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;

import dat.domain.TableColumn;
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
		try {
			VirtualColumn update = virtualColumnService.update(column);
			return new Response(Constant.SUCCESS_CODE,"修改成功",update);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,e.getMessage());
		}
	}
	
	@PostMapping
	public Response createField(VirtualColumn vt){
		if(logger.isDebugEnabled()){
			logger.debug(vt);
			logger.debug(vt.getRefColumns());
		}
		return virtualColumnService.createField(vt);
	}
	
	@PostMapping("/add")
	public Response createField2(VirtualColumn column){
		column.setId();
		Response save = virtualColumnService.save(column);
		return save;
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
	
	/**
	 * 获取指定ID的虚拟字段引用的实体字段
	 * @param id
	 * @return
	 */
	@RequestMapping("/tab/{id}")
	public Response column(@PathVariable String id){
		VirtualColumn column = virtualColumnService.getById(id);
		List<TableColumn> columns = column.getRefColumns();
		return new Response(Constant.SUCCESS_CODE,"查询成功",columns);
	}
	
	@RequestMapping("/formula")
	public Response validateFormula(String formula){
		if(StringUtils.isEmpty(formula)){
			return new Response(Constant.ERROR_CODE,"请输入待验证的公式",formula);
		}
		boolean validate = virtualColumnService.validate(formula);
		return new Response(validate ? Constant.SUCCESS_CODE:Constant.ERROR_CODE);
	}
}
