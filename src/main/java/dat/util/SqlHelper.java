package dat.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlHelper {
	
	public static StringBuffer selectList(Collection<? extends Object> list,String field,String asField) throws Exception{
		StringBuffer sb = new StringBuffer("SELECT ");
		Method method = null;
		Method method2 = null;
		for (Object object : list) {
			if(method == null && method2 == null){
				method = object.getClass().getMethod("get"+Character.toString(field.charAt(0)).toUpperCase()+field.substring(1));
				method2 = object.getClass().getMethod("get"+Character.toString(asField.charAt(0)).toUpperCase()+asField.substring(1));
			}
			String name = method.invoke(object).toString();
			String alias = method2.invoke(object).toString();
			sb.append(name).append(" AS ").append(alias).append(" , ");
		}
		deleteLast(sb, 3);
		return sb;
	}
	
	public static StringBuffer selectList(Collection<? extends Object> list,String field) throws Exception{
		StringBuffer sb = new StringBuffer("SELECT");
		Method method = null;
		for (Object object : list) {
			if(method == null){
				method = object.getClass().getMethod("get"+Character.toString(field.charAt(0)).toUpperCase()+field.substring(1));
			}
			String name = method.invoke(object).toString();
			sb.append(name).append(" , ");
		}
		deleteLast(sb, 3);
		return selectList(list, field,field);
	}
	
	public static StringBuffer from(StringBuffer sb,Collection<?> list,String field) throws Exception{
		sb.append(" FROM ");
		Method method = null;
		for (Object object : list) {
			if(method == null){
				method = object.getClass().getMethod("get"+Character.toString(field.charAt(0)).toUpperCase()+field.substring(1));
			}
			String name = method.invoke(object).toString();
			sb.append(name).append(" , ");
		}
		deleteLast(sb, 3);
		return sb;
	}
	
	
	public static StringBuffer from(List<?> list,String field) throws Exception{
		StringBuffer sb = new StringBuffer(" FROM ");
		Method method = null;
		for (Object object : list) {
			if(method == null){
				method = object.getClass().getMethod("get"+Character.toString(field.charAt(0)).toUpperCase()+field.substring(1));
			}
			String name = method.invoke(object).toString();
			sb.append(name).append(" , ");
		}
		deleteLast(sb, 3);
		return sb;
	}
	
	/**
	 * 删除字符串sb中最后的length个字符
	 * @param sb
	 * @param length
	 * @return
	 */
	public static StringBuffer deleteLast(StringBuffer sb,int length){
		return sb.delete(sb.length()-length, sb.length());
	}
	
	
	public static StringBuffer limit(int offset,int max){
		StringBuffer sb = new StringBuffer(" LIMIT ");
		sb.append(offset).append(" , ").append(max);
		return sb;
	}
	
	public static StringBuffer limit(StringBuffer sb,int offset,int max){
		sb.append(" LIMIT ");
		sb.append(offset).append(" , ").append(max);
		return sb;
	}

	public static void addFunction(StringBuffer buffer, String functionName,
			String fieldName,String alias) {
		buffer.append(functionName.toUpperCase()).append("(").append(fieldName).append(") AS ").append(alias);
	}
	
	public static List<Object> addWhere(List<String> where, StringBuffer buffer) {
		int size = where.size();
		if(size > 1)
			buffer.append(" where ");
		List<Object> param = new ArrayList<>();
		for(int i = 0; i < size-1; i += 2){
			buffer.append(where.get(i)).append(" = ? and ");
			param.add(where.get(i+1));
		}
		if(size > 1)
		SqlHelper.deleteLast(buffer, 5);
		return param;
	}
}
