package dat.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class VirtualTableConnManagerTest {

	@Test
	public void testPrepareStatement() throws Exception {
		String sql = "select you , he her,i as me, then x from i";
		String regex = "select\\s+(\\w+\\s+((as\\s+)?\\w+\\s*)?,)?\\s*\\w+\\s+((as\\s+)?\\w+\\s+)?from\\s\\w+";
		Pattern compile = Pattern.compile(regex);
		Matcher matcher = compile.matcher(sql);
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String substring = sql.substring(start, end);
			System.err.println(substring);
		}
	}

}
