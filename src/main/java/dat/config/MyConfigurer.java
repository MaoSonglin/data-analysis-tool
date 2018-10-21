package dat.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.druid.pool.DruidDataSource;

import dat.config.ConstantConfig.Cors;
import dat.web.AddUserServlet;
import dat.web.ImageServlet;
import dat.web.IsLoginFilter;

/**
 * @author MaoSonglin
 *	应用程序在springMVC方面的配置类
 */
@Configuration
public class MyConfigurer implements WebMvcConfigurer {

	private static Logger log = Logger.getLogger(MyConfigurer.class);
	
	@Autowired
	private ApplicationContext context = null;
	

	@Autowired
	private Environment env;
	
	public MyConfigurer(){}
	
	public MyConfigurer(ApplicationContext context){
		this.context = context;
	}
	/**
	 * 设置允许跨域请求
	 * @return
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		ConstantConfig config = context.getBean(ConstantConfig.class);
		Cors cors = config.getCors();
		registry.addMapping(cors.getMapping())
		.allowedOrigins(cors.getOrigins())
		.allowedMethods(cors.getMethods())
		.allowCredentials(cors.getCredentials()).maxAge(cors.getMaxAge());
		log.info(String.format("允许原域%s使用方法%S访问路径%s",
				Arrays.toString(cors.getOrigins()),
				Arrays.toString(cors.getMethods()),
				cors.getMapping()));
	}
	
	
/*
	public void addInterceptors(InterceptorRegistry registry) {
		InterceptorRegistration interceptorRegistration = registry.addInterceptor(new HandlerInterceptor(){
			private File[] listFiles;
			{
				String path = getClass().getClassLoader().getResource("META-INF/resources").getPath();
				
			}
			public boolean preHandle(HttpServletRequest request,
					HttpServletResponse response, Object handler)
					throws Exception {
				 if(request.getSession().getAttribute(Constant.SESSION_USER_BEAN) != null)
					 return true;
				 else{
					 Response result = new Response(Constant.ERROR_CODE,"您还没有登录，请先登录!");
					 String jsonString = JSON.toJSONString(result);
					 response.setContentType("text/json;charset=utf-8");
					 response.getWriter().println(jsonString);
					 return false;
				 }
			}
			
		});
		Interceptors inter = context.getBean(ConstantConfig.class).getInterceptors();
		String[] path = inter.getIncludePath();
		interceptorRegistration.addPathPatterns(path);
		String[] excludePath = inter.getExcludePath();
		interceptorRegistration.excludePathPatterns(excludePath);
		String info = String.format("拦截器拦截资源路径%s，不拦截资源路径%s",Arrays.toString(path),Arrays.toString(excludePath));
		log.info(info);
	}
	*/
	
	

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);// 在匹配路径的时候忽略后缀
	}



	/**
	 * 配置数据源
	 * @return
	 * @throws IOException 
	 */
	@Bean
	public DataSource createDataSource() throws IOException{
		DruidDataSource dataSource = new DruidDataSource();
		String url = env.getProperty("spring.datasource.url");
		dataSource.setUrl(url);
		String driverClassName = env.getProperty("spring.datasource.driverClassName");
		dataSource.setDriverClassName(driverClassName);
		log.info(String.format("create datasource with driver class %s in url %s",driverClassName, url));
		return dataSource;
	}
	
	/**
	 * @return 生成验证码的servlet
	 */
	@Bean
	public ServletRegistrationBean<ImageServlet> createServlet(){
		ServletRegistrationBean<ImageServlet> servletRegistrationBean = 
				new ServletRegistrationBean<ImageServlet>();
		servletRegistrationBean.setServlet(new ImageServlet());
		Set<String> urlMappingSet = new HashSet<>();
		urlMappingSet.add(env.getProperty("validate.image.path"));
		servletRegistrationBean.setUrlMappings(urlMappingSet);
		return servletRegistrationBean;
	}
	@Bean
	public ServletRegistrationBean<HttpServlet> create(){
		ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<HttpServlet>(new AddUserServlet(),"/add/user");
		return bean;
	}
	
	@Bean
	public FilterRegistrationBean<IsLoginFilter> createFilter(){
		FilterRegistrationBean<IsLoginFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new IsLoginFilter(context));
		Set<String> set = new HashSet<>();
		set.add("*.do");
		registrationBean.setUrlPatterns(set);
		
		return registrationBean;
	}
	
	/**
	 * 文件上传配置
	 * @return
	 */
//	@Bean
	public MultipartResolver fileUpload(){
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(1024*1024*10);
		return multipartResolver;
	}

}
