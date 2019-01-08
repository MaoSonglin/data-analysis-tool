package dat.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import dat.util.Constant;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer code;
	private String message;
	private Object data;
	private Map<String,Object> values;
	{
		values = new HashMap<>();
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
	
	public Object put(String key,Object value){
		Object put = this.values.put(key, value);
		return put;
	}
	
	public Object remove(String key){
		return this.values.remove(key);
	}
	
	public Object get(String key){
		return this.values.get(key);
	}
	
}
