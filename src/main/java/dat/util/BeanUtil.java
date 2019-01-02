package dat.util;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanUtil {
	
	private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);
	
	/**
	 * 将对象original中不是null的属性赋值给target中响应的属性
	 * @param original
	 * @param target
	 */
	@SuppressWarnings("unchecked")
	public static <T> void copyAttributes(T original,T target){
		Class<T> cls = (Class<T>) original.getClass();
		Field[] declaredFields = cls.getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {
				Object object = field.get(original);
				if(object!=null){
					field.set(target, object);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error(e.getMessage());
			}
			
		}
	}
	
	
	public static Object getProperty(Object obj,String name,String name2){
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			Object object = field.get(obj);
			if(object == null){
				field = obj.getClass().getDeclaredField(name2);
				field.setAccessible(true);
				object = field.get(obj);
			}
			return object;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
