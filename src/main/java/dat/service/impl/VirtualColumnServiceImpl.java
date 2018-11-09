package dat.service.impl;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.Response;
import dat.domain.VirtualColumn;
import dat.repos.VirtualColumnRepository;
import dat.service.VirtualColumnService;
import dat.util.Constant;

@Service
public class VirtualColumnServiceImpl implements VirtualColumnService {

	@Autowired
	VirtualColumnRepository vcRepos;
	@Override
	public Response deleteById(String id) {
		vcRepos.deleteById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"删除成功");
		return response;
	}
	
	// 保存虚拟字段，如果虚拟字段的ID在数据库中已经存在，name就保存，否则就添加
	@Transactional
	public Response save(VirtualColumn column) {
		Response response = null;
		try {
			// 根据column的ID查找出数据库中的对象
			VirtualColumn vc = vcRepos.findById(column.getId()).get();
			
			// 跟新数据库中对象的相关属性
			vc.setChinese(column.getChinese());
			vc.setFormula(column.getFormula());
			vc.setName(column.getName());
			vc.setState(column.getState());
			vc.setTypeName(column.getTypeName());
			vc.setRemask(column.getRemask());
			vcRepos.save(vc);
			response = new Response(Constant.SUCCESS_CODE,"修改成功",vc);
		} catch (NoSuchElementException e) {
			// 如果捕获这个异常，说明column在数据库中不存在，那么执行插入操作
			VirtualColumn save = vcRepos.save(column);
			response = new Response(Constant.ERROR_CODE,"添加成功",save);
		} catch (Exception e){
			e.printStackTrace();
			response = new Response(Constant.ERROR_CODE,e.getMessage(),e);
		}
		response.put("oldVal", column);
		return response;
	}

	
}
