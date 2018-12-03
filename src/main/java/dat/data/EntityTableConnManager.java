package dat.data;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;

import dat.domain.DataTable;
import dat.domain.Source;

@SuppressWarnings("unused")
public class EntityTableConnManager implements InvocationHandler,Serializable {
	
	private static final long serialVersionUID = 8812351482502679655L;

	private DataTable table;
	
	private Source source;

	private Connection connection;
	
	public Connection getConnection() throws Exception{
		Connection connection = (Connection) Proxy.newProxyInstance(
				EntityTableConnManager.class.getClassLoader(), 
				new Class<?>[]{Connection.class}, 
				this);
		return connection;
	}
	
	public Connection getConnection(DataTable table) throws Exception{
		return getConnection();
	}
	
	public EntityTableConnManager(DataTable table) throws Exception {
		this(table,table.getSource());
	}

	public EntityTableConnManager(DataTable table, Source source) throws Exception {
		super();
		this.table = table;
		this.source = source;
		Class.forName(source.getDriverClass());
		connection = DriverManager.getConnection(source.getUrl(), source.getUsername(), source.getPassword());
		if(connection == null){
			throw new Exception("获取数据库连接失败");
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String methodName = method.getName();
		if("".equals(methodName)){
			return null;
		}else{
			return method.invoke(connection, args);
		}
	}

}
