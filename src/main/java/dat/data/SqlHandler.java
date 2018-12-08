package dat.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Deprecated
public class SqlHandler implements SqlParser {
	public static void main(String[] args) {
		String sql = "select * , xong ,name 2d from table t1 join tables t2 on x=y where 1=1 and x <= 14 limit 23,234";
		SqlHandler handler = new SqlHandler(sql);
		List<Renameable> columns2 = handler.getColumns();
		for (Renameable renameable : columns2) {
			System.out.print(renameable.getName()+"\t");
			System.out.println(renameable.getAlias());
		}
		System.out.println(handler.table.getName());
		System.out.println(handler.table.getAlias());
		System.out.println(handler.getOffset());
		System.out.println(handler.getMax());
		List<FilterCondition> where = handler.getWhere();
		for (FilterCondition filterCondition : where) {
			System.out.println(filterCondition);
		}
	}
	// 文法表
	static List<Map<String,Integer>> grammerTable = null;
	// 对应文发表中SQL语句结束状态
	static Set<Integer> endState = null;
	// 标点符号
	static Set<String> punctuations = null;
	// 关键词
	private static HashSet<String> keyWords;
	// 运算符
	static Set<String> operators = null;
	private char[] charArray;
	private int index;
	// SQL语句拆分成当个单词
	private ArrayList<String> statement ;
	
	List<Renameable> columns = new ArrayList<>();
	
	Renameable table;
	
	List<JoinAble> joinTables = new ArrayList<>();
	
	List<FilterCondition> wheres = new ArrayList<>();
	public Integer offset;
	public Integer size;
	
	static{
		try(InputStream is = SqlHandler.class.getClassLoader().getResourceAsStream("grammer.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
			Stream<String> lines = reader.lines();
			String[] array = lines.toArray(String[]::new);
			int length = array.length;
			grammerTable = new ArrayList<>(length);
			endState = new HashSet<>();
			for (int i = 0; i < length; i++) {
				grammerTable.add(new HashMap<>());
			}
			for (String line : array) {
				String[] split = line.split("\\s");
				if(split.length >= 3){
					Integer state1 = Integer.valueOf(split[0]);
					Integer state2 = Integer.valueOf(split[2]);
					grammerTable.get(state1).put(split[1], state2);
					if(split.length == 4 && split[3].equals("end")){
						endState.add(state2);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		};
		punctuations = new HashSet<>();
		punctuations.add(",");
		operators = new HashSet<>();
		operators.add("+");
		operators.add("-");
		operators.add("*");
		operators.add("/");
		operators.add("=");
		operators.add("!=");
		operators.add(">");
		operators.add("<");
		operators.add("<=");
		operators.add(">=");
		operators.add("<>");
		
		try(InputStream is = SqlHandler.class.getClassLoader().getResourceAsStream("keyword.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
			keyWords = new HashSet<String>();
			String[] array = reader.lines().toArray(String[]::new);
			for (String key : array) {
				keyWords.add(key);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

	public SqlHandler(String sql) {
		charArray = sql.toCharArray();
		index = 0;
		statement = new ArrayList<String>();
		while(hasNext()){
			String word = nextUnit();
			statement.add(word);
		}
		Integer curState = 0;
		for(int i = 0; i < statement.size(); i++){
			String word = statement.get(i);
			String key = word;
			if(isKeyword(word)){// 关键词
				
			}else if(isOperator(word)){ // 运算符
				
			}else if(isPunctuation(word)){// 标点符号
				
			}else{
				key = "name";
			}
			Integer tmpIndex = grammerTable.get(curState).get(key);
			if(tmpIndex == null){
				curState = grammerTable.get(curState).get("null");
				if(curState != null){
					i--;
				}
			}else{
				curState = tmpIndex;
			}
			if(curState == null){
				throw new IllegalArgumentException("syntax error inner \""+word+"\" in sql statement "+sql);
			}
			doState(curState,word);
		}
	}

	private void doState(Integer curState, String word) {
		StateMachine stateMachine = StateMachine.getStateMachine(curState, this);
		stateMachine.hand(word);
	}

	/**
	 * @param word
	 * @return
	 */
	private boolean isPunctuation(String word) {
		return punctuations.contains(word);
	}

	/**
	 * @param word
	 * @return
	 */
	private boolean isOperator(String word) {
		return operators.contains(word);
	}

	/**
	 * @param word
	 * @return
	 */
	private boolean isKeyword(String word) {
		return keyWords.contains(word);
	}
	
	protected boolean hasNext(){
		return index < charArray.length;
	}
	
	protected String nextUnit(){
		StringBuffer sb = new StringBuffer();
		while(hasNext()){
			char c = charArray[index++];
			String tmp = Character.toString(c);
			if(tmp.matches("\\s+")){// 空格
				if(sb.length() > 0){// 返回之前的字符串
					return sb.toString();
				}else{
					continue;
				}
			}
			else if(isPunctuation(tmp)){// 标点符号“,”
				if(sb.length()>0){	// 该标点符号作为分割符号，将该标点之前的内容返回
					index--;
					return sb.toString();
				}else{
					return tmp; // 该标点作为一个字符串
				}
			}else if(isOperator(tmp)){// 操作符+ - * /
				if(sb.length()>0){ // 之前的字符串
					index--;
					return sb.toString();
				}else{
					sb.append(c);
					boolean f = true;
					while(hasNext() && f){
						char ch = charArray[index++];
						sb.append(ch);
						String str = sb.toString();
						if(!isOperator(str)){
							index--;
							f = false;
							sb.deleteCharAt(sb.length()-1);
						}
					}
					return sb.toString();
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}


	public List<Renameable> getColumns() {
		return columns;
	}


	public Renameable getTable() {
		return table;
	}


	public List<JoinAble> getJoinTables() {
		return joinTables;
	}


	public int getOffset() {
		if(this.offset == null)
			return SqlParser.super.getOffset();
		return this.offset;
	}



	public int getMax() {
		if(this.size == null)
			return SqlParser.super.getMax();
		return this.size;
	}
	public List<FilterCondition> getWhere() {
		return wheres;
	}

}
/**
 * @author MaoSonglin
 *	<br>
 *状态机接口
 */
interface StateMachine {
	static StateMachine getStateMachine(Integer code,SqlHandler handler){
		switch(code){
		case 2:
			return new GetColumnName(handler);
		case 4:
			return new ColumnAliasStateMachine(handler);
		case 6:
			return new AddTableState(handler);
		case 8:
			return new TableAliasState(handler);
		case 10:
			return new Ten(handler);
		case 11:
			return new Eleven(handler);
		case 12:
			return new Twelve(handler);
		case 25:
			return new TwentyFive(handler);
		case 27:
			return new TwentySeven(handler);
		default:
			return (word)->{};
		}
	}
	void hand(String word);
}

class GetColumnName implements StateMachine{

	private SqlHandler handler;

	public GetColumnName(SqlHandler handler) {
		this.handler = handler;
	}

	public void hand(String word) {
		ColumnRenameAble e = new ColumnRenameAble(word,"",word);
		handler.columns.add(e);
	}
	
} 

class ColumnAliasStateMachine extends GetColumnName{

	private SqlHandler handler;

	public ColumnAliasStateMachine(SqlHandler handler) {
		super(handler);
		this.handler = handler;
	}

	public void hand(String word) {
		ColumnRenameAble renameable = (ColumnRenameAble) handler.columns.get(handler.columns.size()-1);
		renameable.alias = word;
	}
	
}

@Deprecated
class ColumnRenameAble implements Renameable{
	String name;
	String alias;
	String conjunction;
	
	public ColumnRenameAble(String name, String alias, String conjunction) {
		super();
		this.name = name;
		this.alias = alias;
		this.conjunction = conjunction;
	}
	public String getName() {
		return name;
	}
	public String getAlias() {
		return alias;
	}
	public String getConjunction() {
		return conjunction;
	}
}
@Deprecated
class TableRenameable implements Renameable{
	String name;
	String alias;
	String conjunction;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getConjunction() {
		return conjunction;
	}
	public void setConjunction(String conjunction) {
		this.conjunction = conjunction;
	}
	
}


class AddTableState implements StateMachine{

	protected SqlHandler handler;

	public AddTableState(SqlHandler handler) {
		this.handler = handler;
	}

	public void hand(String word) {
		TableRenameable t = new TableRenameable();
		t.setName(word);
		handler.table = t;
	}
	
}

/**
 * @author MaoSonglin
 * 解析查询别名
 */
class TableAliasState extends AddTableState{

	public TableAliasState(SqlHandler handler) {
		super(handler);
	}
	
	public void hand(String word) {
		((TableRenameable)handler.table).setAlias(word);
	}
}

class Ten implements StateMachine{
	SqlHandler handler;

	public Ten(SqlHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public void hand(String word) {
		TwoElementOperation elementOperation = new TwoElementOperation();
		elementOperation.setLeft(word);
		handler.wheres.add(elementOperation);
	}
	
}

class Eleven extends Twelve{

	public Eleven(SqlHandler handler) {
		super(handler);
	}
	@Override
	public void hand(String word) {
		int size = handler.wheres.size();
		TwoElementOperation filter = (TwoElementOperation) handler.wheres.get(size-1);
		filter.setCondition(word);
	}
}


class Twelve implements StateMachine{

	SqlHandler handler;
	public Twelve(SqlHandler handler) {
		super();
		this.handler = handler;
	}

	public void hand(String word) {
		int size = handler.wheres.size();
		TwoElementOperation filter = (TwoElementOperation) handler.wheres.get(size-1);
		filter.setRight(word);
	}
	
} 

class TwentyFive implements StateMachine{

	SqlHandler handler;
	
	public TwentyFive(SqlHandler handler) {
		super();
		this.handler = handler;
	}

	public void hand(String word) {
		Integer integer = Integer.valueOf(word);
		handler.offset = integer;
	}
}




class TwentySeven extends TwentyFive{

	public TwentySeven(SqlHandler handler) {
		super(handler);
	}
	@Override
	public void hand(String word) {
		Integer size = Integer.valueOf(word);
		handler.size = size;
	}
} 
@Deprecated
class TwoElementOperation implements FilterCondition{

	String left;
	String right;
	String condition;
	public String getLeft() {
		return left;
	}

	public String getRight() {
		return right;
	}

	public String getCondition() {
		return condition;
	}

	public void setLeft(String left) {
		this.left = left;
	}

	public void setRight(String right) {
		this.right = right;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Override
	public String toString() {
		return "TwoElementOperation ["+left+condition+right+"]";
	}
	
} 
