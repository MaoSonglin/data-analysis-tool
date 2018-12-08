package dat.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import dat.data.DataAdapter;
import dat.data.QueryHelper;
import dat.domain.VirtualTable;
import dat.repos.VirtualTableRepository;

@RestController
@RequestMapping("/test")
public class TestController {
	
	@Autowired
	ApplicationContext context;
	
	@RequestMapping("/adapter")
	public Object testAdapter(String id) throws IOException{
		QueryHelper helper = context.getBean(QueryHelper.class);
//		helper.query(table, columns)
		VirtualTable virtualTable = context.getBean(VirtualTableRepository.class).findById(id).orElse(null);
		DataAdapter adapter = helper.query(virtualTable, virtualTable.getColumns());
		adapter.limit(200, 20);
		Iterator<Map<String, String>> iterator = adapter.iterator();
		List<Map<String,String>> list = new ArrayList<>();
		while(iterator.hasNext()){
			Map<String, String> next = iterator.next();
			list.add(next);
		}
		adapter.close();
		return list;
	}
	
	@RequestMapping("/entityManager")
	public String printEntityManager(){
		String[] beanDefinitionNames = context.getBeanDefinitionNames();
//		String name = context.getBean(EntityManager.class).getClass().getName();
		String name = JSON.toJSON(beanDefinitionNames).toString();
		return name;
	}
	
	@RequestMapping("/print")
	public String print(){
		EntityManager entityManager = context.getBean(EntityManager.class);
		entityManager.getMetamodel();
		return "test";
	}
	
	@RequestMapping("/unwrap")
	public String unwrap(){
		EntityManager unwrap = context.getBean(EntityManager.class).unwrap(EntityManager.class);
		String name = unwrap.getClass().getName();
		Resource resource = context.getResource("classpath:keyword.txt");
		try (InputStream is = resource.getInputStream();
				InputStreamReader reader = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(reader);){
			Object[] array = br.lines().toArray();
			return JSON.toJSONString(array);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return name;
	}
}
