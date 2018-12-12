package dat;

import org.jboss.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 启动主函数
 *
 */
@SpringBootApplication(exclude={ErrorMvcAutoConfiguration.class})
public class App extends SpringBootServletInitializer implements ApplicationContextAware 
{
	private static ApplicationContext context;
	
    public static void main( String[] args ) throws Exception
    {
    	SpringApplication app = new SpringApplication(App.class);
    	ConfigurableApplicationContext context = app.run(args);
    	App.context = context;
    	int beanDefinitionCount = context.getBeanDefinitionCount();
    	Logger log = Logger.getLogger(App.class);
    	log.info("创建了"+beanDefinitionCount+"个对象...");
    }

	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder builder) {
		return builder.sources(getClass());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}

	public static ApplicationContext getContext() {
		return context;
	}
    
}
