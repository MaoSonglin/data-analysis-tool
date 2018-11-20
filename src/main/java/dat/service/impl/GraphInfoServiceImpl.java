package dat.service.impl;

import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.domain.GraphInfo;
import dat.domain.Response;
import dat.repos.GraphInfoRepository;
import dat.service.GraphInfoService;
import dat.util.Constant;

@Service
public class GraphInfoServiceImpl implements GraphInfoService {
	private static Logger logger = Logger.getLogger(GraphInfoServiceImpl.class);
	@Autowired
	private GraphInfoRepository graphInfoRepos;
	
	@Transactional
	public Response save(GraphInfo graphInfo) {
		logger.debug("保存图表信息:"+graphInfo);
		// 检测是否标题重复
		Specification<GraphInfo> spec = (root,query,cb)->{
			Predicate p1 = cb.equal(root.get("title"), graphInfo.getTitle());
			Predicate p2 = cb.notEqual(root.get("id"), graphInfo.getId());
			return cb.and(p1,p2);
		};
		long count = graphInfoRepos.count(spec);
		if(count > 0){
			return new Response(Constant.ERROR_CODE,"图表"+graphInfo.getTitle()+"已经存在");
		}
		GraphInfo save = graphInfoRepos.save(graphInfo);
		return new Response(Constant.SUCCESS_CODE,"保存成功",save);
	}

	public GraphInfo getById(String id) {
		logger.debug("获取ID为"+id+"的图表");
		Optional<GraphInfo> optional = graphInfoRepos.findById(id);
		try {
			GraphInfo graphInfo = optional.get();
			if(Constant.DELETE_STATE == graphInfo.getState())
				return null;
			return graphInfo;
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return null;
	}

	public Response delete(String id) {
		logger.debug("删除ID为"+id+"的报表");
		graphInfoRepos.deleteById(id);
		return new Response(Constant.SUCCESS_CODE,"删除成功",id);
	}

}
