package dat.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import dat.App;
import dat.domain.GraphInfo;
import dat.domain.VirtualColumn;
import dat.repos.GraphInfoRepository;

@Service
public class GraphInfoService extends GraphInfoServiceImpl {

	/**
	 * 获取指定图表需要的数据
	 * @param id
	 * @return
	 */
	public Object getGraphData(String id){
		ApplicationContext context = App.getContext();
		Optional<GraphInfo> optional = context.getBean(GraphInfoRepository.class).findById(id);
		GraphInfo graphInfo = optional.get();
		List<VirtualColumn> columns = graphInfo.getColumns();
		
		return null;
	}
}
