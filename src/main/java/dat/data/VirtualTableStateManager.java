package dat.data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

@Deprecated
public class VirtualTableStateManager implements InvocationHandler {
	private static Logger logger = Logger.getLogger(VirtualTableStateManager.class);
	VirtualTableConnManager tableConnManager;
	List<PreparedStatement> psList = new ArrayList<>();
	private Map<String, Set<String>> nameMap;
	private VirtualTableStateManager() {
	}
	private VirtualTableStateManager(List<PreparedStatement> psList2,
			Map<String, Set<String>> nameMap) {
		this.psList = psList2;
		this.nameMap = nameMap;
	}

	/*public static PreparedStatement getPreparedStatement(List<PreparedStatement> psList , Map<String, Set<String>> nameMap){
		PreparedStatement proxy = (PreparedStatement) Proxy.newProxyInstance(VirtualTableStateManager.class.getClassLoader(),
				new Class<?>[]{PreparedStatement.class},
				new VirtualTableStateManager(psList,nameMap));
		return proxy;
	}*/
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String name = method.getName();
		if("executeQuery".equals(name)){
			if(args==null || args.length==0)
				return executeQuery();
		}else if("close".equals(name)){
			close();
			return null;
		}else if("isClose".equals(name)){
			return isClose();
		}
		
		throw new IllegalAccessException("Method "+name+" is not implemented !");
	}

	private void close() throws SQLException {
		logger.debug("close preparedStatement ");
		for (PreparedStatement ps : psList) {
			ps.close();
		}
	}
	private boolean isClose() throws SQLException{
		boolean f = true;
		for (PreparedStatement ps : psList) {
			f = f && ps.isClosed();
		}
		return f;
	}
	public ResultSet executeQuery() throws SQLException{
		logger.debug("get result");
		List<ResultSet> list = new ArrayList<>();
		for (PreparedStatement ps : psList) {
			ResultSet rs = ps.executeQuery();
			list.add(rs);
		}
		logger.debug("获取结果集个数"+list.size());
		VirtualTableResultSetAdapter resultSetAdapter = new VirtualTableResultSetAdapter();
		resultSetAdapter.setResultSetList(list);
		resultSetAdapter.setNameMap(nameMap);
		resultSetAdapter.setTableConnManager(tableConnManager);
		return resultSetAdapter; 
	}

	public static PreparedStatement getPreparedStatement(
			VirtualTableConnManager virtualTableConnManager) {
		VirtualTableStateManager vtsm = new VirtualTableStateManager();
		vtsm.nameMap =virtualTableConnManager.getNameMap();
		vtsm.tableConnManager = virtualTableConnManager;
		vtsm.psList = virtualTableConnManager.getPsList();
		PreparedStatement proxy = (PreparedStatement) Proxy.newProxyInstance(VirtualTableStateManager.class.getClassLoader(),
				new Class<?>[]{PreparedStatement.class},
				vtsm);
		return proxy;
	}

	
}
