package dat.data;

@Deprecated
public interface Renameable {

	default String getName(){ return null;}
	
	default String getAlias(){ return null;}
	
	default String getConjunction(){ return "as";}
}