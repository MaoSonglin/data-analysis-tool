package dat.config;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Deprecated
public class DataSourceConditional {
	
	private static String path;

	static {
		ClassLoader classLoader = DataSourceConditional.class.getClassLoader();
		path = classLoader.getResource("").getPath();
		try {
			path = URLDecoder.decode(path, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static class ClassPathDataSource implements Condition{

		@Override
		public boolean matches(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			return false;
		}
		
	}
}
