package dat.util;

import java.lang.reflect.Method;
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
}
