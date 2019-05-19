package dat.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IterableUtil {

	@SuppressWarnings("unchecked")
	public static <T> List<T> getIterableField(Iterable<?> iter,String fieldName){
		List<T> list = new ArrayList<>();
		for (Object object : iter) {
			try {
				Field declaredField = object.getClass().getDeclaredField(fieldName);
				declaredField.setAccessible(true);
				Object object2 = declaredField.get(object);
				list.add((T) object2);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
}
