package dat.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.alibaba.druid.util.StringUtils;
import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.App;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.service.UploadFileService;

public class TextMonalise extends MonaliseDataSource {
	
	private static Logger logger = Logger.getLogger(TextMonalise.class);

	protected TextMonalise(Source source) {
		super(source);
	}

	@Override
	public DataTable<DataMap> getDataTableBody(dat.domain.DataTable table, List<TableColumn> columns) throws Exception {
		File file = App.getContext().getBean(UploadFileService.class).getFile(getSource().getAssociation());
		if(file == null){
			throw new Exception("文件没有找到");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		List<String> lines = reader.lines().collect(Collectors.toList());
		Iterator<String> iterator = lines.iterator();
		String next = iterator.next();
		String[] split = next.split("\t");
		Map<Integer,TableColumn> map = new HashMap<>();
		for (int i = 0; i < split.length; i++) {
			for (TableColumn column : columns) {
				if(StringUtils.equalsIgnoreCase(column.getColumnName(), split[i])){
					map.put(i, column);
				}
			}
		}
		
		DataTable<DataMap> dataTable = new DataTable<DataMap>();
		while(iterator.hasNext()){
			String line = iterator.next();
			String[] cells = line.split("\t");
			if(cells.length != split.length){
				logger.warn("数据列数不一致");
			}
			Set<Entry<Integer,TableColumn>> entrySet = map.entrySet();
			DataMap dataMap = new DataMap();
			for (Entry<Integer, TableColumn> entry : entrySet) {
				Integer index = entry.getKey();
				if(index < cells.length){
					dataMap.put(entry.getValue().getColumnName(), cells[index]);
				}
			}
			dataTable.add(dataMap);
		}
		
		reader.close();
		return dataTable;
	}

}
