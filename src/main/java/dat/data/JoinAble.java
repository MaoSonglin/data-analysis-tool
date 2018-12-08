package dat.data;

import javax.persistence.criteria.JoinType;

@Deprecated
public interface JoinAble extends Renameable {
	
	default JoinType getJoinType(){return JoinType.INNER;};
}
