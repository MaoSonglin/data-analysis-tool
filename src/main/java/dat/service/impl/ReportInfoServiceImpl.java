package dat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.domain.GraphInfo;
import dat.domain.ReportInfo;
import dat.domain.ReportPagingBean;
import dat.domain.Response;
import dat.repos.GraphInfoRepository;
import dat.repos.ReportInfoRepository;
import dat.service.ReportInfoService;
import dat.util.Constant;

@Service
public class ReportInfoServiceImpl implements ReportInfoService {

	private static Logger logger = Logger.getLogger(ReportInfoServiceImpl.class);
	@Autowired
	ReportInfoRepository reportInfoRepos;
	
	@Autowired
	GraphInfoRepository graphInfoRepos;
	
	public Response searchByPageInfo(ReportPagingBean pageInfo) {
		Specification<ReportInfo> spec = (root,query,cb)->{
			List<Predicate> predicates = new ArrayList<>();
			String keyword = pageInfo.getKeyword();
			if(!StringUtils.isEmpty(keyword)) {
				Predicate like = cb.like(root.get("name"), "%"+keyword+"%");
				predicates.add(like);
			}
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			predicates.add(notEqual);
			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};
		Page<ReportInfo> page = reportInfoRepos.findAll(spec,PageRequest.of(pageInfo.getCurPage()-1, pageInfo.getPageSize(),Sort.by(Direction.DESC, "id")));
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功");
		response.setData(page);
		response.put("searchInfo", pageInfo);
		return response;
	}
	@Transactional
	public Response save(ReportInfo report) {
		Specification<ReportInfo> spec = (root,query,cb)->{
			Predicate p1 = cb.notEqual(root.get("id"), report.getId());
			Predicate p2 = cb.equal(root.get("name"), report.getName());
			Predicate p3 = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return cb.and(p1,p2,p3);
		};
		List<ReportInfo> list = reportInfoRepos.findAll(spec);
		if(list.isEmpty()){
			ReportInfo save = reportInfoRepos.save(report);
			if(logger.isDebugEnabled())
				logger.debug(save);
			return new Response(Constant.SUCCESS_CODE,"保存成功",save);
		}else{
			return new Response(Constant.ERROR_CODE,"报表名称“"+report.getName()+"”已经存在了",report);
		}
	}

	@Override
	public ReportInfo getById(String id) {
		Optional<ReportInfo> optional = reportInfoRepos.findById(id);
		try {
			ReportInfo reportInfo = optional.get();
			return reportInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<GraphInfo> getGraphs(String id) {
		List<GraphInfo> list = graphInfoRepos.findAll((root,query,cb)->{
			Predicate equal = cb.equal(root.get("report").get("id"), id);
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return cb.and(equal,notEqual);
		});
		return list;
	}

}
