package dat.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import dat.util.Constant;

public class Response implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer code;
	private String message;
	private Object data;
	private Map<String,Object> values;
	{
		values = new HashMap<>();
//		values.put("code intro", "0 represent there are some logic error in the processing, 1 represent success !");
//		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//		values.put("stackTrace", stackTrace);
	}
	public Response() {
		super();
		this.code = Constant.SUCCESS_CODE;
		this.message = "success";
	}
	
	public Response(Integer code) {
		super();
		this.code = code;
		this.message = "success";
	}

	public Response(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public Response(Integer code, String message, Object data) {
		super();
		this.code = code;
		this.message = message;
		this.data = data;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "Response [code=" + code + ", message=" + message + ", data="
				+ data + "]";
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
	
	
	
}
