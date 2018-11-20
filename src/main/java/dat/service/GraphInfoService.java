package dat.service;

import dat.domain.GraphInfo;
import dat.domain.Response;

public interface GraphInfoService {

	Response save(GraphInfo graphInfo);

	GraphInfo getById(String id);

	Response delete(String id);

}
