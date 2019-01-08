package dat.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class Classify2 {
	public static void main(String[] args) {
		Classify2 classify = new Classify2("中文系那个大声道非","hxgsdf32","VARCHAR");
		Integer category = classify.getCategory();
		System.out.println(category);
		classify.setCnName("企业进区");
		classify.setEnName("uoxgewr32");
		classify.setDataType("NUMBER");
		System.out.println(classify.getCategory());
		
	}
	
	private static Logger logger = LogManager.getLogger(Classify2.class);
	
	private static List<List<Double>> clusters;
	
	private static List<List<String>> dataSet;
	
	private static JiebaSegmenter segmenter;
	
	static{
		try(InputStream is = Classify2.class.getResourceAsStream("cluster.json");
				InputStreamReader inputStreamReader = new InputStreamReader(is,"UTF-8");
				BufferedReader reader = new BufferedReader(inputStreamReader)) {
			StringBuffer sb = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line);
			}
			clusters = new ArrayList<>();
			List<String> array = JSON.parseArray(sb.toString(), String.class);
			for (String list : array) {
				List<Double> array2 = JSON.parseArray(list,Double.class);
				clusters.add(array2);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		URL res = Classify2.class.getResource("df_info_reg.csv");
		try(CSVParser csvParser = CSVParser.parse(res, Charset.forName("gbk"),
				CSVFormat.newFormat(','));){
			dataSet = new ArrayList<>((int)csvParser.getRecordNumber());
			List<CSVRecord> records = csvParser.getRecords();
			for (CSVRecord csvRecord : records) {
				List<String> line = new ArrayList<>();
				for (String string : csvRecord) {
					line.add(string);
				}
				dataSet.add(line);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		segmenter = new JiebaSegmenter();
	}
	
	private String cnName;
	
	private String enName;
	
	private String dataType;
	
	public Classify2() {
	}
	
	public Classify2(String cnName,String enName,String dataType) {
		this.cnName = cnName;
		this.enName = enName;
		this.dataType = dataType;
	}
	
	public Integer getCategory(){
		double tfidf = getTfidf(cnName, 1);
		double tfidf2 = getTfidf(enName, 2);
		double d3 = "VARCHAR".equalsIgnoreCase(dataType) ? 0d : 
			"NUMBER".equalsIgnoreCase(dataType) ? 1d :
				"DATATIME".equalsIgnoreCase(dataType) ? 2d : 3d;
		// 向量化
		List<Double> v = new ArrayList<>(3);
		v.add(tfidf);
		v.add(tfidf2);
		v.add(d3);
		logger.debug("向量化为："+v);
		// 计算该点到所有分类的距离
		List<Double> lengths = getLengths(v);
		// 距离最小的分类
		int c = 0;
		for(int i= 1; i < lengths.size(); i++){
			if(lengths.get(c) > lengths.get(i)){
				c = i;
			}
		}
		return c;
	}

	/**
	 * 计算点v到各个分类中心的距离
	 * @param v
	 * @return
	 */
	private List<Double> getLengths(List<Double> v) {
		// 存放距离的数组
		List<Double> lengths = new ArrayList<>();
		// 遍历所有的分类，分别计算距离
		for (List<Double> list : clusters) {
			// 分类list的分量迭代器
			Iterator<Double> i1 = list.iterator();
			// 待计算的点的分量迭代器
			Iterator<Double> i2 = v.iterator();
			// 将距离初始化为0
			double tmp = 0f;
			// 两点之间的距离等于两点的每个分量差的平方和再开方
			while(i1.hasNext() && i2.hasNext()){
				tmp += Math.pow(i1.next() - i2.next(), 2d);
			}
			double length = Math.sqrt(tmp);
			lengths.add(length);
			logger.debug("两点之间的距离："+list+"=>"+v+" = "+length);
		}
		return lengths;
	}
	
	protected double getTfidf(String text,int columnIndex){
		// 处理的列的所有数据
		List<String> column = getColumn(columnIndex);
		return maxTfidf(text, column);
	}

	/**
	 * @param text
	 * @param column
	 * @return
	 */
	private double maxTfidf(String text, List<String> column) {
		// 分词结果
		List<SegToken> process = segmenter.process(text, SegMode.INDEX);
		
		int size = process.size();
		// 存放每一个分词词组的tfidf的数组
		List<Double> tfids = new ArrayList<>(size);
		for (SegToken segToken : process) {
			String word = segToken.word;
			// 该词在分词集合中出现的次数
			double count = 0.0d;
			for (SegToken token : process) {
				count += token.word.equals(word) ? 1d : 0d;
			}
			// 该词语的tf，即频率
			double tf = count / size;
			// 出现该词语的文档篇数
			double times = 0d;
			for (String element : column) {
				times += element.contains(word) ? 1d : 0d;
			}
			// 该词语的idf
			double idf = Math.log(column.size()/(times+1));
			tfids.add(tf * idf);
		}
		// 排序
		tfids.sort(null);
		return tfids.isEmpty() ? 0d : tfids.get(tfids.size()-1);
	}

	/**
	 * @param columnIndex
	 * @return
	 */
	private List<String> getColumn(int columnIndex) {
		List<String> column = new ArrayList<>(dataSet.size());
		for (List<String> row : dataSet) {
			String elem = row.get(columnIndex);
			column.add(elem);
		}
		return column;
	}

	public String getCnName() {
		return cnName;
	}

	public void setCnName(String cnName) {
		this.cnName = cnName;
	}

	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public static List<List<Double>> getClusters() {
		return clusters;
	}

	public static List<List<String>> getDataSet() {
		return dataSet;
	}

	@Override
	public String toString() {
		return "Classify [cnName=" + cnName + ", enName=" + enName
				+ ", dataType=" + dataType + "]";
	}
	
	
}
