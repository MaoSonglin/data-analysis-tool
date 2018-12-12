package dat.data;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;

public interface DataAdapter extends Iterable<Map<String,String>>,Iterator<Map<String,String>>,Closeable {
	
	void filter(String where);
	
	int clearFilter();
	
	void limit(int offset,int size);
}







