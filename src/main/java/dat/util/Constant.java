package dat.util;

public interface Constant {
	/**
	 * 请求失败
	 */
	int ERROR_CODE = 0;
	/**
	 * 请求成功
	 */
	int SUCCESS_CODE = 1;
	/**
	 * 处理请求过程中发生异常
	 */
	int INTERNAL_ERROR = 2;
	/**
	 * 加密密码的盐值长度
	 */
	int SALT_LENGTH = 10;
	String SESSION_VALIDATE_CODE = "SESSION_VALIDATE_CODE";
	String SESSION_USER_BEAN = "sessionUserBean";
	String AUTO_LOGIN_COOKIE_NAME = "autologin";
}
