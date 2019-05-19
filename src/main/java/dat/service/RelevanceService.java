package dat.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dat.domain.Relevance;
import dat.domain.VirtualTable;
import dat.repos.RelevanceRepository;

public interface RelevanceService {
	
	Set<Relevance> getByTable(VirtualTable table);
	
	Set<Relevance> getByTables(Iterable<VirtualTable> tables);
	
	@Service
	public static class RelevanceServiceImpl implements RelevanceService{

		@Autowired
		private RelevanceRepository repos;
		
		@Override
		public Set<Relevance> getByTable(VirtualTable table) {
			List<Relevance> list = repos.findAll((root,query,cb)->{
				Predicate equal = cb.equal(root.get("column1").get("table").get("id"), table.getId());
				Predicate equal2 = cb.equal(root.get("column2").get("table").get("id"), table.getId());
				return cb.or(equal,equal2);
			});
			return new HashSet<>(list);
		}

		@Override
		public Set<Relevance> getByTables(Iterable<VirtualTable> tables) {
			Set<String> ids = new HashSet<>();
			tables.forEach(table->{
				ids.add(table.getId());
			});
			List<Relevance> list = repos.findAll((root,query,cb)->{
				Predicate in = root.get("column1").get("table").get("id").in(ids);
				Predicate in2 = root.get("column2").get("table").get("id").in(ids);
				return cb.or(in,in2);
			});
			return new HashSet<>(list);
		}
		
	}
	
}
