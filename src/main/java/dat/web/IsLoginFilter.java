package dat.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@WebFilter("/*")
public class IsLoginFilter implements Filter,ApplicationContextAware {

	
	public IsLoginFilter(ApplicationContext context) {
		super();
		this.context = context;
	}

	private ApplicationContext context;
	private Logger log;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log = Logger.getLogger(IsLoginFilter.class.getName());
		log.info(context.toString());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<Entry<String,String[]>> entrySet = parameterMap.entrySet();
		System.err.printf("一共收到%d个请求参数!\n",entrySet.size());
		for (Entry<String, String[]> entry : entrySet) {
			System.err.println(entry.getKey()+":"+Arrays.toString(entry.getValue()));
		}
		HttpServletRequest req = (HttpServletRequest) request;
		String url = req.getRequestURI().toString();
		System.out.println("请求路径："+url);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}

}
