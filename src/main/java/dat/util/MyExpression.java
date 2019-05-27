package dat.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyExpression extends Expression {
	
	public MyExpression() {
		super();
	}

	public MyExpression(String express) {
		super(express);
	}

	@Override
	protected List<String> getTokens() {
		String express = getExpress();
		Pattern compile = Pattern.compile("\\s+|\\(|\\)|\\+|\\*|\\/|\\-|<|>|(>=)|(<=)|=");
		Matcher matcher = compile.matcher(express);
		List<String> x = new ArrayList<>();
		int last = 0;
		while(matcher.find()){
			int start = matcher.start();
			if(start - last > 1){
				String y = express.substring(last, start);
				x.add(y);
			}
			int end = matcher.end();
			String n = express.substring(start, end);
			last = end;
			x.add(n);
		}
		if(last < express.length())
			x.add(express.substring(last));
		return x;
	}
	@Override
	public boolean validate() {
		List<String> tokens = getTokens();
		Stack<String> opnd = new Stack<>();
		Stack<String> optr = new Stack<>();
		optr.push("#");
		tokens.add("#");
		Iterator<String> iterator = tokens.iterator();
		String token = iterator.next();
		try {
			while(!"#".equals(token) || !"#".equals(optr.peek())){
				
				if(!set.contains(token)){
					opnd.push(token);
					token = iterator.next();
				}
				else{
					String peek = optr.peek();
					switch(precede(peek,token)){
					case '<':
						optr.push(token);
						token = iterator.next();
						break;
					case '>':
						optr.pop();
						opnd.pop();
						opnd.pop();
						opnd.push("true");
						break;
					case '=':
						optr.pop();
						token = iterator.next();
						break;
					default:
						throw new IllegalArgumentException("未定义的优先级");
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return optr.size() == 1 && optr.peek().equals("#");
	}
}
