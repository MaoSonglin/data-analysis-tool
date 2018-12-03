package dat.service;

import dat.domain.GraphInfo;
import dat.vo.GraphDataQueryBean;
import dat.vo.Response;

public interface GraphInfoService {

	Response save(GraphInfo graphInfo);

	GraphInfo getById(String id);

	Response delete(String id);

	Response getData(GraphInfo g) throws Exception;

	Response getGraphDataById(GraphDataQueryBean id);
}
