package dat.service;

import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;

import dat.domain.Reference;
import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.repos.ReferenceRepository;
import dat.repos.VirtualColumnRepository;
import dat.util.Constant;
import dat.vo.Response;

@Deprecated
public interface ReferenceService {

	/**
	 * 保存
	 * @param ref
	 * @return
	 */
	Response save(Reference ref);

	Reference findByForeginColumnId(String id);

	List<Reference> findByPkgId(String id);

}

//@Service
@Deprecated
class ReferenceServiceImpl implements ReferenceService{
	
	@Autowired
	private ReferenceRepository referenceRepos;
	
	@Autowired
	private VirtualColumnRepository virtualColumnRepos;

	@Override
	public Response save(Reference ref) {
		if(ref.getId()==null){
			
			VirtualColumn foreignColumn = virtualColumnRepos.findById(ref.getForeignColumn().getId()).orElse(null);
			VirtualTable primaryTable = foreignColumn.getTable();
			
			VirtualColumn referencedColumn = virtualColumnRepos.findById(ref.getReferencedColumn().getId()).orElse(null);
			VirtualTable referencedTable = referencedColumn.getTable();
			
			ref.setForeignColumn(foreignColumn);
			ref.setPrimaryTable(primaryTable);
			ref.setReferencedColumn(referencedColumn);
			ref.setReferencedTable(referencedTable);
			
			Reference save = referenceRepos.save(ref);
			
			return new Response(Constant.SUCCESS_CODE,"添加成功",save);
		}else{
			Reference save = referenceRepos.save(ref);
			return new Response(Constant.SUCCESS_CODE,"修改成功",save);
		}
	}

	@Override
	public Reference findByForeginColumnId(String id) {
		Reference reference = referenceRepos.findOne((root,query,cb)->{
			Path<String> path = root.get("foreignColumn").get("id");
			Predicate predicate = cb.equal(path, id);
			return predicate;
		}).orElse(null);
		return reference;
	}

	@Override
	public List<Reference> findByPkgId(String id) {
		List<Reference> findAll = referenceRepos.findAll((root,query,cb)->{
			return cb.equal(root.join("primaryTable").join("packages").get("id"), id);
		});
		return findAll;
	}
}