package dat.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import dat.domain.Response;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.VirtualTableRepository;
import dat.service.VirtualTableService;
import dat.util.Constant;

@Service
public class VirtualTableServiceImpl implements VirtualTableService {

	private static Logger logger = Logger.getLogger(VirtualTableServiceImpl.class);
	
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

	@Override
	public Response add(VirtualTable table) {
		List<VirtualColumn> columns = table.getColumns();
		for (VirtualColumn virtualColumn : columns) {
			StringBuffer sb = new StringBuffer();
			for (TableColumn column : virtualColumn.getRefColumns()) {
				sb.append(column.getId()).append("+");
			}
			if(sb.length()>0){
				sb.deleteCharAt(sb.length()-1);
			}
			virtualColumn.setState(Constant.ACTIVATE_SATE);
			virtualColumn.setFormula(sb.toString());
			logger.debug(virtualColumn);
		}
		Object json = JSON.toJSON(table);
		try (PrintStream out = new PrintStream(new FileOutputStream("D:\\Program Files\\eclipse\\txt.json"))){
			out.print(json.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
