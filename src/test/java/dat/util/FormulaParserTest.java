package dat.util;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FormulaParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValidate() {
		FormulaParser parser = new FormulaParser();
		boolean validate = parser.validate("1*(2+3=)");
		System.out.println(validate);
	}
	
	@Test
	public void test2(){
		MyExpression expression = new MyExpression("1090*(DF435432+3657s)-7");
		List<String> tokens = expression.getTokens();
		System.out.println(tokens);
		boolean validate = expression.validate();
		System.out.println(validate);
	}

}
