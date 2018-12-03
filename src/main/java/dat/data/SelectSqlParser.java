package dat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Deprecated
public class SelectSqlParser implements dat.data.VirtualTableConnManager.SqlParser{
	static Set<String> set = new HashSet<>();
	static List<Map<String,Integer>> table = new ArrayList<>();
	static Set<Integer> endSet = new HashSet<>();
	private static Set<String> puncs = new HashSet<>();
	static {
		puncs.add(",");
		puncs.add("+");
		puncs.add("-");
		puncs.add("*");
		puncs.add("/");
		puncs.add("=");
		puncs.add("<");
		puncs.add("<=");
		puncs.add(">");
		puncs.add(">=");
		puncs.add("!=");
		puncs.add("!");
		set.add("select");
//		set.add(",");
		set.add("from");
		set.add("where");
		set.add("left");
		set.add("right");
		set.add("innear");
		set.add("outer");
		set.add("join");
		set.add("as");
		set.add("on");
		set.add("in");
		set.add("is");
		set.add("not");
		set.add("=");
		set.add("and");
		set.add("or");
		set.add("not");
		set.add("null");
		table = new ArrayList<>();
		for(int i = 0; i <= 20; i++){
			table.add(new HashMap<>());
		}
		table.get(0).put("select", 1);
		
		table.get(1).put("word", 2);

		table.get(2).put(",", 1);
		table.get(2).put("as", 3);
		table.get(2).put("word", 4);
		table.get(2).put("from", 5);
		
		table.get(3).put("word", 4);
		
		table.get(4).put(",", 1);
		table.get(4).put("from", 5);
		
		table.get(5).put("word", 6);
		
		table.get(6).put("as", 7);
		table.get(6).put("word",8);
		table.get(6).put("join", 9);
		table.get(6).put("where", 17);
		
		table.get(7).put("word", 8);

		table.get(8).put("join", 9);
		table.get(8).put("where", 17);
		
		table.get(9).put("word", 10);
		
		table.get(10).put("as", 11);
		table.get(10).put("word", 12);
		table.get(10).put("on", 13);
		
		table.get(11).put("word", 12);
		table.get(12).put("on", 13);
		table.get(13).put("word", 14);
		table.get(14).put("=", 15);
		table.get(15).put("word", 16);
		
		table.get(16).put("join", 9);
		table.get(16).put("where", 17);
		
		table.get(17).put("word", 18);
		
		table.get(18).put("=", 19);
		table.get(18).put("!=", 19);
		table.get(18).put(">", 19);
		table.get(18).put(">=", 19);
		table.get(18).put("<", 19);
		table.get(18).put("<=", 19);
		table.get(18).put("<>", 19);
		
		table.get(19).put("word", 20);
		
		table.get(20).put("and", 17);
		table.get(20).put("or", 17);
		
		endSet.add(6);
		endSet.add(8);
		endSet.add(16);
		endSet.add(20);
	}
	
	private String sql ;
	
	private String tableName;
	
	private String tableAlias;
	
	private List<String> columnLabel;
	
	public void setSql(String sql) {
		this.sql = sql;
		init(sql);
	}

	private List<String> columnAlias;
	
	private List<String> joinTables;
	
	private List<String> joinTableAlias;
	
	private List<String> joinTypes;

	private char[] charArrays;

	private int index;
	
	public SelectSqlParser(String sql) {
		
		setSql(sql);
	}

	/**
	 * @param sql
	 */
	private void init(String sql) {
		tableName = null;
		tableAlias= null;
		columnLabel = new ArrayList<>();
		columnAlias = new ArrayList<>();
		joinTables = new ArrayList<>();
		joinTableAlias = new ArrayList<>();
		joinTypes = new ArrayList<>();
		charArrays = sql.toCharArray();
		if(!isValidate()){
			throw new IllegalArgumentException("error in statement "+sql);
		}
		index = 0;
		parser(sql);
	}

	public boolean isValidate() {
		index = 0;
		Integer state = 0;
		while(hasNext()){
			String nextPhrase = nextPhrase();
			if(isKeyWord(nextPhrase)||ispunctuation(nextPhrase)){
				state = table.get(state).get(nextPhrase);
			}
			else if(validName(nextPhrase)){
				state = table.get(state).get("word");
			}
			else{
				return false;
			}
			if(state == null){
				return false;
			}
			else if(endSet.contains(state)){
				if(!hasNext()) return true;
			}
		}
		return endSet.contains(state);
	}
	
	private boolean ispunctuation(char c){
		if(c==','||c=='='||c=='<'||c=='>'||c=='!'){
			return true;
		}
		return false;
	}
	
	private boolean ispunctuation(String nextPhrase) {
		return puncs.contains(nextPhrase.toLowerCase());
	}

	private void parser(String sql2) {
		index = 0;
		Integer state = 0;
		while(hasNext()){
			String nextPhrase = nextPhrase();
			if(isKeyWord(nextPhrase)||ispunctuation(nextPhrase)){
				state = table.get(state).get(nextPhrase);
			}else if(validName(nextPhrase)){
				state = table.get(state).get("word");
			}
			doState(state,nextPhrase);
		}
	}

	private void doState(Integer state, String nextPhrase) {
		switch(state){
		case 2:
			columnLabel.add(nextPhrase);
			columnAlias.add(nextPhrase);
			break;
		case 4:
			columnAlias.remove(columnAlias.size()-1);
			columnAlias.add(nextPhrase);
			break;
		case 6:
			tableName = nextPhrase;
			break;
		case 8:
			tableAlias = nextPhrase;
			break;
		case 10:
			joinTables.add(nextPhrase);
			break;
		case 12:
			joinTableAlias.add(nextPhrase);
			break;
		default:
			break;
		}
	}

	private String nextPhrase() {
		StringBuffer sb = new StringBuffer();
		while(hasNext()){
			char c = charArrays[index++];
			boolean isSpace = Pattern.matches("\\s", Character.toString(c));
			if(sb.length()>0 && isSpace){
				String word = sb.toString();
				return word;
			}
			else if(ispunctuation(c)){
				if(sb.length()>0) {
					index --;
					return sb.toString();
				}
				else return Character.toString(c);
			}
			else if(!isSpace)
				sb.append(c);
		}
		if(sb.length()>0)
		return sb.toString();
		else
			throw new IllegalArgumentException();
	}
	static boolean validName(String name){
		for (String key : set) {
			if(key.equalsIgnoreCase(name)) return false;
		}
		boolean b = Pattern.matches("\\w+", name);
		return b;
	}
	
	static boolean isKeyWord(String name){
		if(set.contains(name.toLowerCase())){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @return
	 */
	private boolean hasNext() {
		return index < charArrays.length;
	}

	public String getSql() {
		return sql;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public List<String> getColumnLabel() {
		return columnLabel;
	}

	public List<String> getColumnAlias() {
		return columnAlias;
	}

	public List<String> getJoinTables() {
		return joinTables;
	}

	public List<String> getJoinTableAlias() {
		return joinTableAlias;
	}

	public List<String> getJoinTypes() {
		return joinTypes;
	}
}

