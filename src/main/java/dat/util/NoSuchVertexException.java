package dat.util;

public class NoSuchVertexException extends Exception {

	public NoSuchVertexException(String vertex1) {
		super("没有"+vertex1);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1879703773803146187L;

}
