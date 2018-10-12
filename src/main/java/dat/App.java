package dat;

import org.jboss.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Hello world!
 *
 */
@SpringBootApplication(exclude={ErrorMvcAutoConfiguration.class})
public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	SpringApplication app = new SpringApplication(App.class);
    	ConfigurableApplicationContext context = app.run(args);
    	int beanDefinitionCount = context.getBeanDefinitionCount();
    	Logger log = Logger.getLogger(App.class);
    	log.info("创建了"+beanDefinitionCount+"个对象...");
//    	Environment env = context.getBean(Environment.class);
//    	String classpath = env.getProperty("classpath");
//    	log.info("classpath:"+classpath);
    }
}
