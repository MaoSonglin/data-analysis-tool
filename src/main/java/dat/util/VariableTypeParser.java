package dat.util;

/**
 * @author MaoSonglin
 * 变量类型解析器，解析一个字符串是否是数字，日期等
 */
public class VariableTypeParser {
	
	String variable;
	
	public String getTypeName(){
		if(variable == null)
			return "ANY";
		try {
			Double.parseDouble(variable);
			return "Number";
		} catch (NumberFormatException e) {
		}
		if("true".equalsIgnoreCase(variable) || "false".equalsIgnoreCase(variable)){
			return "Boolean";
		}
		return "String";
	}
	
	public boolean isBoolean(){
		if("true".equals(variable)||"false".equals(variable))
			return true;
		else
			return false;
	}
	
	
	public boolean isDate(){
		return false;
	}
	
	public boolean isDigit(){
		if(variable == null)
			return false;
		if(variable.matches("[\\-\\+]?\\d+(\\.\\d+)|\\d*[fFDdLl]+"))
			return true;
		return false;
	}


	public void setVariable(String variable) {
		this.variable = variable;
	}
	
}
