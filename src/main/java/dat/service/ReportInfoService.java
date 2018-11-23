package dat.service;

import java.util.List;

import dat.domain.GraphInfo;
import dat.domain.ReportInfo;
import dat.vo.ReportPagingBean;
import dat.vo.Response;

public interface ReportInfoService {

	Response searchByPageInfo(ReportPagingBean pageInfo);

	Response save(ReportInfo report);

	ReportInfo getById(String id);

	List<GraphInfo> getGraphs(String id);

}
