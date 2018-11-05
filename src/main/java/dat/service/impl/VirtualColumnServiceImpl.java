package dat.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dat.domain.Response;
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

	
}
