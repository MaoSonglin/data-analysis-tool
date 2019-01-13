package dat.test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import dat.vo.EchartOptions;
import dat.vo.EchartOptions.Axis;


public class EchartOptionTest {

	@Test
	public void run1() throws IOException{
		EchartOptions options = new EchartOptions();
		Axis xAxis = new Axis();
		options.setxAxis(xAxis);
		options.setyAxis(xAxis);
		String s = JSON.toJSONString(options);
		System.out.println(s);
		System.out.println(JSON.toJSONString(xAxis));
		ObjectMapper objectMapper = new ObjectMapper();
		String string = objectMapper.writeValueAsString(options);
		System.out.println(string);
		ObjectReader objectReader = objectMapper.reader().forType(EchartOptions.class);
		EchartOptions obj = objectReader.readValue(string);
		System.out.println(obj);
	}
	
	@Test
	public void run2() throws IOException{
		String file = "D:\\Program Files\\eclipse\\workspace\\DataAnalysisTool\\src\\test\\java\\dat\\test\\options.json";
		String string = FileUtils.readFileToString(new File(file));
		ObjectMapper objectMapper = new ObjectMapper();
		EchartOptions readValue = objectMapper.readValue(string, EchartOptions.class);
		System.out.println(readValue);
	}
	
	@Test
	public void Run3() throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
		String format = sdf.format(new Date());
		System.out.println(format);
		Date parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2019-01-12T12:36:25.714+0000");
		System.out.println(parse);
	}
}
