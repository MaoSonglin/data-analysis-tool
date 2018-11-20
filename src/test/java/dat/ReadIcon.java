package dat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class ReadIcon {

	@Test
	public void run1(){
		// .fa-tumblr-square:before
		// D:\Program Files\eclipse\workspace\TopJUI-free\static\plugins\font-awesome\css\font-awesome.min.css
		StringBuffer sb = new StringBuffer();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\Program Files\\eclipse\\workspace\\TopJUI-free\\static\\plugins\\font-awesome\\css\\font-awesome.min.css")))){
			String line = null;
			while((line = reader.readLine())!=null){
				sb.append(line).append("\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(sb);
		String regex ="\\.(\\w|\\-)+:before";//"([a-z]+|\\-)+";//"\\.\\w+:before";
		Pattern compile = Pattern.compile(regex);
//		String sb = ",.fa-weixin:before{content:\f1d7}.fa-send:before,";
		Matcher matcher = compile.matcher(sb);
		List<String> icons = new LinkedList<>();
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String icon = sb.substring(start+1, end-7);
			System.out.println("start="+start+",end="+end+","+icon);
			icons.add(icon);
		}
		String jsonString = JSON.toJSONString(icons);
		try(PrintStream out = new PrintStream(new FileOutputStream("icons.json"))){
			out.println(jsonString);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
