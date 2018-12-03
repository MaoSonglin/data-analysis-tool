package dat.data;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import org.jboss.logging.Logger;

import dat.domain.Source;

public class DataSourceConnManager implements InvocationHandler,Closeable{
	private static Logger logger = Logger.getLogger(DataSourceConnManager.class);
	private String driverClass;
	private String url;
	private String username;
	private String password;
	private String databaseName;
	private Connection connection;
	private DataSourceConnManager(Source source) {
		driverClass = source.getDriverClass();
		url = source.getUrl();
		username = source.getUsername();
		password = source.getPassword();
		databaseName = source.getDatabaseName();
		try {
			Class.forName(driverClass);
			connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			logger.debug("get connection with url "+url+" and user "+username+" error");
			throw new RuntimeException(e);
		}
	}
	

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		logger.debug("try to invoke method "+name+" with parameter "+Arrays.toString(args));
		if("equals".equals(name)){
			if(parameterTypes.length == 0){
				return this.equals(args[0]);
			}
		}
		else{
			return method.invoke(connection, args);
		}
		throw new IllegalArgumentException("method "+name+" with arguments "+Arrays.toString(args)+" is not implemented !");
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public Connection getConnection() {
		Connection proxy = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, this);
		return proxy;
	}
	
	public static Connection getConnection(Source source){
		Connection proxy = (Connection) Proxy.newProxyInstance(DataSourceConnManager.class.getClassLoader(), new Class<?>[]{Connection.class}, new DataSourceConnManager(source));
		return proxy;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((databaseName == null) ? 0 : databaseName.hashCode());
		result = prime * result
				+ ((driverClass == null) ? 0 : driverClass.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataSourceConnManager other = (DataSourceConnManager) obj;
		if (databaseName == null) {
			if (other.databaseName != null)
				return false;
		} else if (!databaseName.equals(other.databaseName))
			return false;
		if (driverClass == null) {
			if (other.driverClass != null)
				return false;
		} else if (!driverClass.equals(other.driverClass))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}


	@Override
	public void close() throws IOException {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}
	
}
