package dat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dat.domain.UploadFile;
import dat.vo.ClassifyFormula;

public class FormulaParser {
	
	private String formula;
	
	private Map<String,String> values;
	
	
	
	public FormulaParser() {
		super();
	}

	public FormulaParser(String formula2,Map<String,String> values) {
		this.formula = formula2;
		this.values = values;
	}

	public boolean validate(String formula){
		
		return false;
	}
	
	public String getValue(){
		
		if(ClassifyFormula.isClassifyFormula(formula)){
			return classifyFormula();
		}
		
		return values.get(formula);
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
