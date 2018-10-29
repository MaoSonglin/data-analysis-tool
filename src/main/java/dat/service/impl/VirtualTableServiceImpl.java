package dat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import dat.domain.Response;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.VirtualTableRepository;
import dat.service.VirtualTableService;
import dat.util.Constant;

@Service
public class VirtualTableServiceImpl implements VirtualTableService {

	@Resource(name="virtualTableRepository")
	private VirtualTableRepository vtRepos;
	
	public Response getById(String id) {
		try {
			VirtualTable virtualTable = vtRepos.getOne(id);
			return new Response(Constant.SUCCESS_CODE,"查询成功",virtualTable);
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Constant.ERROR_CODE,String.format("ID为“%s”的虚拟数据表不存在！", id));
		}
	}

	public Response getVirtualColumns(String id) {
		List<VirtualColumn> virtualColumns = vtRepos.getColumnsWithId(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",virtualColumns);
		response.put("virtualTableId", id);
		return response;
	}

}
