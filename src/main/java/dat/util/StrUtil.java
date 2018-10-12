package dat.util;

import java.util.Random;

public class StrUtil {

	public static String randomStr(int length){
		char [] array = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		Random random = new Random();
		int len = array.length;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < length ; i++){
			int index = Math.abs(random.nextInt((int) System.currentTimeMillis()) % len);
			sb.append(array[index]);
		}
		return sb.toString();
	}
}
