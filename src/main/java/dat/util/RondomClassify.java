package dat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Deprecated
@SuppressWarnings("all")
public class RondomClassify implements Classify {
	
	public static void main(String[] args) {
		for(int i = 0; i < 10; i++)
		System.out.println(new Object().hashCode());
	}
	
	public double compare(String category, String value) {
		return Math.random() * 10000;
	}

	@Override
	public List<? extends Object> classify(List<? extends Object> values,
			List<? extends Object> categories) {
		int hashCode = categories.hashCode();
		Random random = new Random(hashCode);
		List<Object> result = new ArrayList<>();
		int length = categories.size();
		for(int i=0, size = values.size(); i < size; i++){
			int nextInt = random.nextInt();
			Object object = categories.get(nextInt%length);
			result.add(object);
		}
		return result;
	}

}
