package dat.vo;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.JSON;

import dat.domain.UploadFile;

public class ClassifyFormula implements Serializable{
	
	public static boolean isClassifyFormula(String text){
		return text.startsWith("ClassifyFormula:");
	}
	
	public static ClassifyFormula getFromString(String text){
		if(!isClassifyFormula(text)){
			throw new IllegalArgumentException(text+"不能转化为分类公式");
		}
		String substring = text.substring(16);
		ClassifyFormula object = JSON.parseObject(substring, ClassifyFormula.class);
		return object;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9182465782019976009L;
	
	private List<String> fields;

	private String expression;
	
	private String classifier;
	
	private UploadFile categoryFile;
	
	private UploadFile jarFile;

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public UploadFile getFileInfo() {
		return categoryFile;
	}

	public void setFileInfo(UploadFile fileInfo) {
		this.categoryFile = fileInfo;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "ClassifyFormula [fields=" + fields + ", classifier="
				+ classifier + ", fileInfo=" + categoryFile + "]";
	}

	public UploadFile getCategoryFile() {
		return categoryFile;
	}

	public void setCategoryFile(UploadFile categoryFile) {
		this.categoryFile = categoryFile;
	}

	public UploadFile getJarFile() {
		return jarFile;
	}

	public void setJarFile(UploadFile jarFile) {
		this.jarFile = jarFile;
	}
	
	
}
