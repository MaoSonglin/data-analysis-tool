package dat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

@SuppressWarnings("unused")
public class Cluster {
	private static JiebaSegmenter segmenter = new JiebaSegmenter();
	
	private List<List<String>> dataSet;
	
	private Map<Integer,Set<List<String>>> clusters;
	
	public void cluster(int k){
		// 新建分类到分类中数据的映射，key表示第key个分类，值是一个二维数组
		// 二维数组的第一维表示某个分类中的数据，第二维表示一条数据
		clusters = new HashMap<>(k);
		for(int i = 0; i < k ; i++ ){
			clusters.put(i, new HashSet<>());
		}
		// 随机选择k个分类作为聚类初始点
		Random random = new Random(System.currentTimeMillis());
		for(int i = 0; i < k ; i++){
			int nextInt = random.nextInt(dataSet.size());
			List<String> list = dataSet.get(nextInt);
			clusters.get(i).add(list);
		}
		
		Iterator<List<String>> iter1 = dataSet.iterator();
		Iterator<Entry<Integer, Set<List<String>>>> iter2 = clusters.entrySet().iterator();
		for (List<String> list : dataSet) {
			
		}
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

	
}
