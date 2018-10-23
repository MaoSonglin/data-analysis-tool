package dat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StrUtil {

	public static String randomStr(int length){
		char [] array = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		return randomChars(length, array);
	}

	private static String randomChars(int length, char[] array) {
		Random random = new Random();
		int len = array.length;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < length ; i++){
			int currentTimeMillis = (int) System.currentTimeMillis();
			int index = Math.abs(random.nextInt(Math.abs(currentTimeMillis)) % len );
			sb.append(array[index]);
		}
		return sb.toString();
	}
	
	public static String generatorId(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String format = sdf.format(new Date());
		return format + randomChars(4, "1234567890".toCharArray());
	}
	
	
	public static String currentTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String format = sdf.format(new Date());
		return format;
	}
	
	public static String appendLike(String key){
		return "%"+key+"%";
	}
}
