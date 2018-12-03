package dat.util;

import java.util.Map;

public class FormulaParser {
	
	private String formula;
	
	private Map<String,String> values;
	
	public FormulaParser(String formula2,Map<String,String> values) {
		this.formula = formula2;
		this.values = values;
	}

	public boolean validate(String formula){
		
		return false;
	}
	
	public String getValue(){
		return values.get(formula);
	}
	
}
