package dat.util;

import java.util.List;

/**
 * @author MaoSonglin
 * 计算相似度的接口
 */
public interface Classify {
	/**
	 * 比较分类category和值value的相似对值，相似度越高值约大
	 * @param category	分类字符串
	 * @param value		文本字符串
	 * @return			分类字符串与文本字符串的相似度
	 */
	double compare(String category,String value);
	
	
	/**
	 * 对文本数据集values进行分类，分类为categories中的分类
	 * @param values		数据集
	 * @param categories	分类集合
	 * @return				数据集分类后的集合
	 */
	List<? extends Object> classify(List<? extends Object> values,List<? extends Object> categories);
}
