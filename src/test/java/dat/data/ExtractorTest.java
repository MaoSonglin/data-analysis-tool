package dat.data;

import java.util.ArrayList;



import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;





import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;





import dat.domain.DataTable;
import dat.domain.Source;
import dat.repos.DataTableRepository;
import dat.service.DataSourceService;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ExtractorTest {

	@Autowired	
	private ApplicationContext context;
	
	@Test
	public void test() {
		DataTableRepository repository = context.getBean(DataTableRepository.class);
		DataSourceService sourceService = context.getBean(DataSourceService.class);
		Page<DataTable> list = repository.findAll(PageRequest.of(5, 10));
		for (DataTable dataTable : list) {
			Source source = dataTable.getSource();
			JdbcTemplate template = sourceService.getTemplate(source);
			DataSource dataSource = template.getDataSource();
			Extractor extractor = new Extractor(dataSource);
			ArrayList<String> columnNames = new ArrayList<>();
			dataTable.getColumns().forEach(elem->{
				String columnName = elem.getColumnName();
				columnNames.add(columnName);
			});
			extractor.setExtractNames(dataTable.getName(), columnNames);
			Iterator<Map<String, String>> iterator = extractor.iterator();
			while(iterator.hasNext()){
				Map<String, String> next = iterator.next();
				System.out.println(next);
			}
			extractor.close();
			break;
		}
	}

}
