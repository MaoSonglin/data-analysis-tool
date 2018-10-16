package dat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author MaoSonglin
 * 自定义的常亮配置类
 */
@Configuration
@ConfigurationProperties(prefix="constant",ignoreUnknownFields=true)
public class ConstantConfig {

	/**
	 * 使用Cors协议允许跨域访问的配置信息，该对象配置了允许访问本服务器的origin
	 */
	private Cors cors;
	
	private Interceptors interceptors;
	
	public Cors getCors() {
		return cors;
	}

	public void setCors(Cors cors) {
		this.cors = cors;
	}

	public Interceptors getInterceptors() {
		return interceptors;
	}

	public void setInterceptors(Interceptors interceptors) {
		this.interceptors = interceptors;
	}

	public static class Cors{
		
		private String mapping;
		
		private String[] Origins;
		
		private String[] methods;
		
		private Boolean credentials = true;
		
		private Integer maxAge = 3600;

		public String getMapping() {
			return mapping;
		}

		public void setMapping(String mapping) {
			this.mapping = mapping;
		}

		public String[] getOrigins() {
			return Origins;
		}

		public void setOrigins(String[] origins) {
			Origins = origins;
		}

		public String[] getMethods() {
			return methods;
		}

		public void setMethods(String[] methods) {
			this.methods = methods;
		}

		public Boolean getCredentials() {
			return credentials;
		}

		public void setCredentials(Boolean credentials) {
			this.credentials = credentials;
		}

		public Integer getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(Integer maxAge) {
			this.maxAge = maxAge;
		}
		
		
	}

	public static class Interceptors{
		
		String[] includePath;
		
		String[] excludePath;

		public String[] getIncludePath() {
			return includePath;
		}

		public void setIncludePath(String[] includePath) {
			this.includePath = includePath;
		}

		public String[] getExcludePath() {
			return excludePath;
		}

		public void setExcludePath(String[] excludePath) {
			this.excludePath = excludePath;
		}
		
		
	}
}
