package dat.util;

import java.lang.reflect.Method;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class JsonUtils {
	
	public static JSON toJSON(Object obj){
		Class<? extends Object> clazz = obj.getClass();
		if(clazz.isArray()){
			Object[] o = (Object[]) obj;
			JSONArray jsonArray = new JSONArray();
			for (Object object : o) {
				JSON json = toJSON(object);
				jsonArray.add(json);
			}
			return jsonArray;
		}
		if(clazz.isPrimitive()){
			JSON.toJSON(obj);
		}
		Method[] methods = clazz.getMethods();
		
		for (Method method : methods) {
			String name = method.getName();
			int parameterCount = method.getParameterCount();
			if(parameterCount == 0 && name.matches("get\\w+")){
				String key = name.substring(3);
				key = String.valueOf(key.charAt(0)).toLowerCase()+key.substring(1);
			}
		}
		return null;
	}
}
