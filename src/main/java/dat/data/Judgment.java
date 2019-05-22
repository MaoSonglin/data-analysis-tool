package dat.data;

/**
 * @author MaoSonglin
 * 多表连接判定接口。
 */
public interface Judgment {

	/**
	 * 判定左值和有值是否符合连接条件
	 * @param left
	 * @param right
	 * @return
	 */
	boolean compare(Object left,Object right);
	
	public class EqualsJudgment implements Judgment{

		@Override
		public boolean compare(Object left, Object right) {
			if(left == null)
				return false;
			if(right == null)
				return false;
			return left.equals(right);
		}
		
	}
}