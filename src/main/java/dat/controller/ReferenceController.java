package dat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dat.domain.Reference;
import dat.service.ReferenceService;
import dat.util.Constant;
import dat.vo.Response;

@RestController
@RequestMapping("/ref")
public class ReferenceController {

	@Autowired
	private ReferenceService referenceService;
	
	@PostMapping()
	public Response add(Reference ref){
		return referenceService.save(ref);
	}
	
	/**
	 * 根据外键所属的字段查询外键
	 * @param id
	 * @return
	 */
	@GetMapping("/foreign/column/{id}")
	public Response getByForeignColumnId(@PathVariable String id){
		Reference reference = referenceService.findByForeginColumnId(id);
		if(reference !=null)
			return new Response(Constant.SUCCESS_CODE,"查询成功",reference);
		else
			return new Response(Constant.ERROR_CODE,"ID为‘"+id+"’的字段不是外键");
	}
	
	/**
	 * 获取指定数据包中的数据表之间的关联信息
	 * @param id
	 * @return
	 */
	@GetMapping("/pkg/{id}")
	public Object getForeignKeyInPkg(@PathVariable String id){
		List<Reference> list = referenceService.findByPkgId(id);
		return list;
	}
}
