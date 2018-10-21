package dat.util;

import java.lang.reflect.Field;

public class BeanUtil {

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
				e.printStackTrace();
			}
			
		}
	}
}
