package dat.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dat.domain.GraphInfo;
import dat.domain.Menu;
import dat.domain.ReportInfo;
import dat.repos.GraphInfoRepository;
import dat.repos.MenuRepository;
import dat.repos.ReportInfoRepository;
import dat.service.ReportInfoService;
import dat.util.Constant;
import dat.vo.ReportPagingBean;
import dat.vo.Response;

@Service
public class ReportInfoServiceImpl implements ReportInfoService {

	private static Logger logger = Logger.getLogger(ReportInfoServiceImpl.class);
	@Autowired
	ReportInfoRepository reportInfoRepos;
	
	@Autowired
	GraphInfoRepository graphInfoRepos;
	
	@Autowired
	ApplicationContext  context;
	
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
		return reportInfoRepos.findById(id).orElse(null);
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
	
	@Override
	@Transactional
	public Response pulish(String reportid, Integer menuid) {
		Response res = new Response();
		MenuRepository repository = context.getBean(MenuRepository.class);
		// 根据ID查找到报表发布到的目录
		Menu menu = repository.findById(menuid).orElse(null);
		if(menu == null){
			res.setCode(9);
			res.setMessage("目录不存在");
			return res;
		}
		// 根据ID查询出待发布的报表
		ReportInfo reportInfo = reportInfoRepos.findById(reportid).orElse(null);
		if(reportInfo == null){
			res.setCode(8);
			res.setMessage("报表不存在");
			return res;
		}
		Menu m = new Menu();
		m.setText(reportInfo.getName());
		m.setUrl("report/show.html?id="+reportInfo.getId());
		m.setParent(menu.getId());
		repository.save(m);
		
		reportInfo.setPublish(m);
		ReportInfo save = reportInfoRepos.save(reportInfo);
		
		res.setCode(Constant.SUCCESS_CODE);
		res.setMessage("发布成功");
		res.setData(save);
		return res;
	}
	
	@Override
	@Transactional
	public Response unpublish(String reportid) {
		ReportInfo reportInfo = reportInfoRepos.findById(reportid).orElse(null);
		if(reportInfo == null){
			return new Response(Constant.ERROR_CODE,"报表不存在",reportid);
		}
		// 获取发布目录
		Menu publish = reportInfo.getPublish();
		reportInfo.setPublish(null);
		ReportInfo save = reportInfoRepos.save(reportInfo);
		// 删除之前发布的目录
		context.getBean(MenuRepository.class).delete(publish);
		return new Response(Constant.SUCCESS_CODE,"操作成功",save);
	}

	@Override
	@Transactional
	public Response delete(String id) {
		ReportInfo reportInfo = reportInfoRepos.findById(id).orElse(null);
		if(reportInfo == null){
			return new Response(Constant.ERROR_CODE,"ID不存在");
		}
		Menu menu = reportInfo.getPublish();
		// 删除发布的目录
		if(menu != null)
			context.getBean(MenuRepository.class).delete(menu);
		List<GraphInfo> graphs = reportInfo.getGraphs();
		// 删除图表信息
		context.getBean(GraphInfoRepository.class).deleteAll(graphs);
		reportInfoRepos.delete(reportInfo);
		return new Response(Constant.SUCCESS_CODE,"删除成功");
	}

}
