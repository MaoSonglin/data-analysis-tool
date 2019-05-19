package dat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;









import dat.domain.UploadFile;
import dat.vo.ClassifyFormula;

public class FormulaParser {
	
	public static void main(String[] args) throws Exception {
		String formula = "4+21/(10-3)*(9-4)+1+8";
		Expression expresss = new Expression(formula);
		String value = expresss.getValue();
		System.out.println(value);
		FormulaParser parser = new FormulaParser();
		boolean validate = parser.validate(formula);
		System.out.println(validate);
		expresss.setExpress("(182)");
		String value2 = expresss.getValue();
		System.out.println(value2);
	}
	
	private String formula;
	
	private Expression express;
	
	private Map<String,String> values;
	
	{
		express = new Expression();
		express.setCalc((a,optr,b)->{
			double num1 = 0d,num2 = 0d;
			if(StrUtil.isNumbar(a)){
				num1 = Double.parseDouble(a.toString());
			}else{
				String tmp_a = values.get(a);
				if(StrUtil.isNumbar(tmp_a)){
					num1 = Double.parseDouble(tmp_a);
				}
			}
			
			if(StrUtil.isNumbar(b)){
				num2 = Double.parseDouble(b.toString());
			}else{
				String tmp_b = values.get(b);
				if(StrUtil.isNumbar(tmp_b)){
					num2 = Double.parseDouble(tmp_b);
				}
			}
			if(optr.equals("+")){
				return num1 + num2;
			}
			if(optr.equals("-")){
				return num1 - num2;
			}
			if(optr.equals("*")){
				return num1 * num2;
			}
			if(optr.equals("/")){
				return num1 / num2;
			}
			return null;
		});
	}
	
	public FormulaParser() {
		super();
	}

	public FormulaParser(String formula2,Map<String,String> values) {
		this.formula = formula2;
		this.values = values;
	}

	public boolean validate(String formula){
		Expression expression = new Expression(formula);
		try {
			expression.getValue();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getValue(){
		
		if(ClassifyFormula.isClassifyFormula(formula)){
			return classifyFormula();
		}
		
		if(isColumnId(formula)){
			return values.get(formula);
		}
		
		express.setExpress(formula);
		
		try {
			String value = express.getValue();
			if(isColumnId(value)) return values.get(value);
			return value;//express.getValue();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}//values.get(formula);
	}

	
	private boolean isColumnId(String formula){
		boolean matches = formula.matches("(DF[0-9]+)|(FD[0-9]+)");
		return matches;
	}
	
	/**
	 * @return
	 */
	private String classifyFormula() {
		ClassifyFormula classifyFormula = ClassifyFormula.getFromString(formula);
		try {
			// 获取分类器
			Classify similarity = getClassifyFormulaInstance(classifyFormula);
			// 获取分类文件信息
			UploadFile fileInfo = classifyFormula.getCategoryFile();
			// 加载分类文件
			List<String> lines = ThreadFileReader.getLines(fileInfo);
			if(lines.isEmpty()){
				throw new IllegalArgumentException("空的分类文件:"+fileInfo.getFileName());
			}
			// 获取分类字段原始值
			String expression = classifyFormula.getExpression();
			FormulaParser parser = new FormulaParser(expression,this.values);
			String value = parser.getValue();
			
			List<Double> list = new ArrayList<>();
			for (String line : lines) {
				double rate = similarity.compare(line, value);
				list.add(rate);
			}
			double max = -99999999d;
			for(double l : list){
				if(max < l){
					max = l;
				}
			}
			int index = list.indexOf(max);
			if(index == -1){
				throw new RuntimeException("没有找到合适的分类");
			}
			String category = lines.get(index);
			return category;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param classifyFormula
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Classify getClassifyFormulaInstance(
			ClassifyFormula classifyFormula) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		String classifierName = classifyFormula.getClassifier();
		
		// 如果用户自定义分类
		UploadFile jarFile = classifyFormula.getJarFile();
		if(jarFile != null){
			// 加载jar包
		}
		
		Class<?> clazz = Class.forName(classifierName);
		Classify similarity = (Classify) clazz.newInstance();
		return similarity;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	
}


class Expression{
	
	Set<String> set = new HashSet<>();
	
	Calculation calc = (a,pop,b)->{
		double parseDouble = Double.parseDouble(a.toString());
		double parseDouble2 = Double.parseDouble(b.toString());
		
		if("+".equals(pop)){
			double x = parseDouble + parseDouble2;
			return String.valueOf(x);
		}
		if("-".equals(pop)){
			double x = parseDouble - parseDouble2;
			return String.valueOf(x);
		}
		if("*".equals(pop)){
			double x = parseDouble * parseDouble2;
			return String.valueOf(x);
		}
		if("/".equals(pop)){
			double x = parseDouble / parseDouble2;
			return String.valueOf(x);
		}
		return "("+a+pop+b+")";
	};
	
	{
		set.add("+");
		set.add("-");
		set.add("*");
		set.add("/");
		set.add("(");
		set.add(")");
		set.add("#");
	}
	String express;

	public Expression() {
		super();
	}

	 

	
	public Expression(String express) {
		super();
		this.express = express;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public String getValue() throws Exception {
		Stack<String> opnd = new Stack<>();
		Stack<String> optr = new Stack<>();
		optr.push("#");
		List<String> tokens = getTokens();
		tokens.add("#");
		Iterator<String> iterator = tokens.iterator();
		String token = iterator.next();
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
					String pop = optr.pop();
					String num1 = opnd.pop();
					String num2 = opnd.pop();
					opnd.push(calc.exec(num2, pop, num1).toString());
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
		String value = opnd.peek();
		return value;
	}


	private char precede(String peek, String token) {
		switch(peek){
		case "+":
		case "-":
			switch(token){
			case "+":
			case "-":
			case ")":
			case "#":
				return '>';
			case "*":
			case "/":
			case "(":
				return '<';
			}
			break;
		case "*":
		case "/":
			switch(token){
			case "+":
			case "-":
			case "*":
			case "/":
			case ")":
			case "#":
				return '>';
			case "(":
				return '<';
			}
			break;
		case "(":
			switch(token){
			case "+":
			case "-":
			case "*":
			case "/":
			case "(":
				return '<';
			case ")":
				return '=';
			case "#":
				throw new IllegalArgumentException();
			}
			break;
		case ")":
			switch(token){
			case "+":
			case "-":
			case "*":
			case "/":
			case ")":
			case "#":
				return '>';
			case "(":
				throw new IllegalArgumentException();
			}
			break;
		case "#":
			switch(token){
			case "+":
			case "-":
			case "*":
			case "/":
			case "(":
				return '<';
			case ")":
				throw new IllegalArgumentException();
			case "#":
				return '=';
			}
			break;
		}
		throw new IllegalArgumentException();
	}

	/**
	 * @return
	 */
	private List<String> getTokens() {
		char[] charArray = this.express.toCharArray();
		List<String> tokens = new ArrayList<>();
		StringBuffer buffer = new StringBuffer();
		for (char c : charArray) {
			switch(c){
			case '+':
			case '-':
			case '*':
			case '/':
			case '(':
			case ')':
			case ' ':
				if(buffer.length() > 0){
					String token = buffer.toString();
					tokens.add(token);
					buffer.delete(0, buffer.length());
				}
				tokens.add(String.valueOf(c));
				break;
			default:
				buffer.append(c);
				break;
			}
		}
		if(buffer.length()>0)
			tokens.add(buffer.toString());
		return tokens;
	}

	public void setExpress(String express) {
		this.express = express;
	}




	public void setCalc(Calculation calc) {
		this.calc = calc;
	}

	
}



