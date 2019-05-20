package dat.aop;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class MonaliseJdbcProxy implements MethodInterceptor {

	@Override
	public Object intercept(Object sub, Method method, Object[] params, MethodProxy arg3) throws Throwable {
		String name = method.getName();
		if("iterator".equalsIgnoreCase(name)){
			
		}
		return null;
	}

}
