package dat.aop;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import dat.pojo.Response;
import dat.util.Constant;
@Component
@Aspect
public class ControllerExceptionHandler implements Serializable {
	
	private static Logger log = Logger.getLogger(ControllerExceptionHandler.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5798625811819409397L;
	
	public ControllerExceptionHandler(){
		log.infov("让每一个请求都返回指定类容..............");
	}

	//@AfterThrowing(value="execution(* dat.controller.*.*(..))")
	public void aroud(JoinPoint point){
		
	}
	
	@Around("execution(* dat.controller.*.*(..))")
	public Object aroud(ProceedingJoinPoint point){
		MethodSignature signature = (MethodSignature) point.getSignature();
		String name = signature.getMethod().getName();
		String declaringTypeName = signature.getDeclaringTypeName();
		Class<?>[] parameterTypes = signature.getParameterTypes();
		StringBuffer sb = new StringBuffer();
		for (Class<?> parameterName : parameterTypes) {
			sb.append(parameterName.getSimpleName()).append(",");
		}
		if(sb.length() > 0){
			sb.deleteCharAt(sb.length()-1);
		}
		log.infof("访问控制器方法%s.%s(%s)", declaringTypeName,name,sb.toString());
		try {
			Object proceed = point.proceed();
			return proceed;
		} catch (Throwable e) {
			e.printStackTrace();
			Class<?> returnType = signature.getReturnType();
			if(returnType.equals(Response.class)){
				return new Response(Constant.ERROR_CODE,e.getMessage(),e);
			}else if(returnType.equals(String.class)){
				return e.getMessage();
			}
		}
		return null;
	}

	protected Object getNewReturenValue(ProceedingJoinPoint point, Throwable e) {
		// 获取切入点方法的形参类型
		Class<?>[] classes = getArgsTypes(point);
		// 异常记录树
		StackTraceElement[] stackTrace = e.getStackTrace();
		// 切入对象
		Object target = point.getTarget();
		Class<? extends Object> targetClass = target.getClass();
		String className = targetClass.getName();
		Class<?> returnType = null;
		for (StackTraceElement stackTraceElement : stackTrace) {
			boolean equals = stackTraceElement.getClassName().equals(className);
			if(equals){
				String methodName = stackTraceElement.getMethodName();
				try {
					Method method = targetClass.getMethod(methodName, classes);
					returnType = method.getReturnType();
					break;
				} catch (NoSuchMethodException | SecurityException e1) {
					e1.printStackTrace();
				}
			}
		}
		Object response = null;
		if(returnType != null){
			try {
				if(returnType.isPrimitive()){
					response = Constant.INTERNAL_ERROR;
				}
				else if(CharSequence.class.isAssignableFrom(returnType)){
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						PrintStream printStream = new PrintStream(bos);
						e.printStackTrace(printStream);
						response = bos.toString();
						printStream.close();
						bos.close();
					} catch (Exception e1) {
						e1.printStackTrace();
						response = e.getMessage();
					}
				}
				else if(Number.class.isAssignableFrom(returnType)){
					response = Constant.INTERNAL_ERROR;
				}
				else {
					response = new Response(Constant.INTERNAL_ERROR,"系统在处理请求过程中发现异常",e);
				}
			} catch ( SecurityException e1) {
				e1.printStackTrace();
			}
		}
		
		return response;
	}

	private Class<?>[] getArgsTypes(ProceedingJoinPoint point) {
		Object[] args = point.getArgs();
		List<Class<?>> array = new ArrayList<>();
		for (Object obj : args) {
			array.add(obj.getClass());
		}
		Class<?>[] classes = array.stream().toArray(Class[]::new);
		return classes;
	}

	protected boolean isRetureJson(ProceedingJoinPoint point){
		Class<? extends Object> cls = point.getTarget().getClass();
		RestController restController = cls.getAnnotation(RestController.class);
		if(restController != null)
			return true;
		
		return false;
	}
}
