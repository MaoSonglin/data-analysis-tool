package dat.repos;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class CustomerSpecs{

	public static <T> Specification<T> byAuto(final EntityManager entityManager,final T example){
		return new Specification1<T>(entityManager,example);
	}
	
	/**
	 * 构造一个specification查询接口，该方法返回的接口封装使用like匹配关键字example的查询接口
	 * @param entityManager
	 * @param example
	 * @return
	 */
	public static <T> Specification<T> byKeyWord(Class<T> cls,EntityManager entityManager,String example){
		return new AllStringAttrLike<T>(cls,entityManager, example);
	}
	
	static class Specification1<T> implements Specification<T>{
		private static final long serialVersionUID = 7131579426213025814L;
		private Specification1(EntityManager entityManager, T example2) {
			this.entityManager = entityManager;
			this.example = example2;
		}
		private T example;
		private EntityManager entityManager;
		@Override
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
				CriteriaBuilder criteriaBuilder) {
			List<Predicate> predicates = new ArrayList<>();
			@SuppressWarnings("unchecked")
			EntityType<T> entity = (EntityType<T>) entityManager.getMetamodel().entity(example.getClass());
			Set<Attribute<T,?>> attributes = entity.getDeclaredAttributes();
			for (Attribute<T, ?> attr : attributes) {
				Object attrValue = getValue(example,attr);
				if(attrValue != null){
					if(attr.getJavaType().equals(String.class)){
						if(!StringUtils.isEmpty(attrValue)){
							Path<String> field = root.get(attribute(entity,attr.getName(),String.class));
							Predicate predicate = criteriaBuilder.like(field, pattern(attrValue));
							predicates.add(predicate);
						}
					}else{
						Predicate predicate = criteriaBuilder.equal(root.get(attribute(entity,attr.getName(),attrValue.getClass())), attrValue);
						predicates.add(predicate);
					}
				}
			}
			return predicates.isEmpty() ? criteriaBuilder.conjunction():criteriaBuilder.and(toArray(predicates));
		}
	}
	
	static class AllStringAttrLike<T> implements Specification<T>{
		
		private Class<T> cls;

		public AllStringAttrLike(Class<T> cls,EntityManager entityManager,String example) {
			this.entityManager = entityManager;
			this.example = example;
			this.cls = cls;
		}
		EntityManager entityManager;
		String example;
		private static final long serialVersionUID = -1689776393659112953L;
		
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
				CriteriaBuilder criteriaBuilder) {
			if(example==null){
				return criteriaBuilder.conjunction();
			}
			List<Predicate> predicates = new ArrayList<>();
			EntityType<T> entityType = (EntityType<T>) entityManager.getMetamodel().entity(cls);
			Set<Attribute<T,?>> attributes = entityType.getDeclaredAttributes();
			for (Attribute<T, ?> attribute : attributes) {
				boolean b = attribute.getJavaType().equals(String.class);
				if(b){
					Expression<String> path = root.get(attribute.getName());
					predicates.add(criteriaBuilder.like(path, pattern(example)));
				}
			}
			return predicates.isEmpty() ? criteriaBuilder.conjunction():criteriaBuilder.or(toArray(predicates));
		}
		
	}
	
	private static String pattern(Object attrValue) {
		String x = (String) attrValue;
		if(!x.startsWith("%")){
			x = "%"+x;
		}
		if(!x.endsWith("%"))
		{
			x = x + "%";
		}
		return x;
	}
	private static <E,S> SingularAttribute<S,E> attribute(EntityType<S> entity, String name,
			Class<E> class1) {
		SingularAttribute<S,E> attribute = entity.getDeclaredSingularAttribute(name, class1);
		return attribute;
	}
	private static <S,T> Object getValue(T example,Attribute<T,?> attr){
		Object object = ReflectionUtils.getField((Field) attr.getJavaMember(), example);
		return object;
	}
	
	private static  Predicate[] toArray(List<Predicate> list){
		int size = list.size();
		Predicate[] predicates = new Predicate[size];
		for(int i=0; i < size; i++){
			predicates[i] = list.get(i);
		}
		return predicates;
	}
	
}

