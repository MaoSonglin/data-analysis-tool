package dat.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterUtils{
	public static boolean satisfy(String filter,Map<String,String> map){
		List<String> names = getSegments(filter);
		if(names.size() == 3){
			String name = names.get(0);
			boolean b = map.containsKey(name);
			if(!b)
				throw new IllegalArgumentException("invalid column name \""+name+"\"");
			String value = map.get(name);
			String tmp = names.get(1);
			String s = names.get(2);
			if(tmp.equals("=")){
				return s.equals(value);
			}else if("!=".equals(tmp) || "<>".equals(tmp)){
				return !s.equals(value);
			}else if("<".equals(tmp)){
				return Double.valueOf(value) > Double.valueOf(s);
			}else if(">".equals(tmp)){
				return Double.valueOf(value) < Double.valueOf(s);
			}else if("<=".equals(tmp)){
				return Double.valueOf(s) <= Double.valueOf(value);
			}else if(">=".equals(tmp)){
				return Double.valueOf(s) >= Double.valueOf(value);
			}
		}
		return false;
	}

	/**
	 * @param filter
	 * @return
	 */
	protected static List<String> getSegments(String filter) {
		List<String> names = new ArrayList<>();
		Pattern compile = Pattern.compile("(\\s|\\W)+");
		Matcher matcher = compile.matcher(filter);
		List<Integer> list = new ArrayList<>();
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			list.add(start);
			list.add(end);
		}
		list.add(filter.length());
		int start = 0;
		for (Integer end : list) {
			if(start != end){
				String name = filter.substring(start, end).trim();
				if(!name.isEmpty())
					names.add(name);
			}
			start = end;
		}
		return names;
	}
}
