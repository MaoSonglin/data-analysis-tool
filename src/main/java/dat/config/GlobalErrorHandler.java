package dat.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import dat.util.Constant;
import dat.vo.Response;

@ControllerAdvice
public class GlobalErrorHandler {

	@ExceptionHandler(value=Exception.class)
	@ResponseBody
	public Response doError(Exception e){
		
		e.printStackTrace();
		return new Response(Constant.ERROR_CODE,String.format("服务器内部发生异常:“%s”", e.getMessage()));
	}
}
