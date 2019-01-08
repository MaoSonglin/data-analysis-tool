package dat.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.sql.DataSource;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.jboss.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dat.domain.DataTable;
import dat.domain.Source;
import dat.domain.TableColumn;
import dat.domain.VirtualColumn;
import dat.repos.DataTableRepository;
import dat.repos.DsRepository;
import dat.repos.TableColumnRepository;
import dat.service.DataSourceService;
import dat.service.DataTableService;
import dat.util.Constant;
import dat.util.DialectUtil;
import dat.util.StrUtil;
import dat.vo.DataTablePaingBean;
import dat.vo.Response;
import dat.vo.TableData;

@Service
public class DataTableServiceImpl implements DataTableService ,ApplicationContextAware{
	
	private static Logger logger = Logger.getLogger(DataTableServiceImpl.class);
	
	@Resource(name="dataTableRepository")
	private DataTableRepository tabRepos;
	
	@Resource(name="tableColumnRepository")
	private TableColumnRepository tableColumnRepos;
	
	@Resource(name="dsRepository")
	private DsRepository dsRepos;
	
	@Resource(name="dataSourceServiceImpl")
	private DataSourceService dsService;

	private ApplicationContext context;
	
	@Override
	public Response search(DataTablePaingBean pageBean) {
		Page<DataTable> page = tabRepos.findAll((root,query,cb)->{
			List<Predicate> predicates = new ArrayList<>();
			String id = pageBean.getId();
			if(!StringUtils.isEmpty(id)){
				predicates.add(cb.like(root.get("id"), StrUtil.appendLike(id)));
			}
			String sourceName = pageBean.getSourceName();
			if(!StringUtils.isEmpty(sourceName)){// 如果数据源名称不为空
				predicates.add(cb.like(root.get("source").get("name"), StrUtil.appendLike(sourceName)));
			}
			String tableName = pageBean.getTableName();
			if(!StringUtils.isEmpty(tableName)){ // 如果数据表名称不为空
				predicates.add(cb.like(root.get("name"), StrUtil.appendLike(tableName)));
			}
			Predicate ps[] = new Predicate[predicates.size()];
			// 只查询出结果中没有标志删除的数据
			Predicate notEqual = cb.notEqual(root.get("state"), Constant.DELETE_STATE);
			return predicates.isEmpty()? notEqual:cb.and(cb.or(predicates.toArray(ps)),notEqual);
		},
				PageRequest.of(pageBean.getCurPage(), pageBean.getPageSize(), 
						new Sort(Direction.DESC,"addTime")));
		return new Response(Constant.SUCCESS_CODE,"查询成功",page);
	}

	@Override
	public Response getById(String id) {
		Optional<DataTable> optional = tabRepos.findById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",optional.get());
		return response;
	}

	@Override
	public Response getSource(String id) {
		Source s = tabRepos.findSourceById(id);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功",s);
		return response;
	}

	@Override
	public Response getTables(String id) {
		List<TableColumn> columns = tabRepos.findById(id).get().getColumns();
		return new Response(Constant.SUCCESS_CODE,"查询成功",columns);
	}
	
	/**
	 * 获取指定ID的数据表记录对应的原始表中的数据
	 * @param id
	 * @return
	 */
	@Deprecated
	public List<Map<String,String>> getValues(TableData tInfo){
		// 获取数据表
		DataTable tb = tabRepos.findById(tInfo.getId()).get();
		// 获取数据源
		Source source = dsRepos.findOne((root,query,cb)->{
			Join<Source, DataTable> join = root.join("tables");
			Predicate equal = cb.equal(join.get("id"), tb.getId());
			return equal;
		}).get();
		// 获取数据表的字段
		List<TableColumn> columns = tableColumnRepos.findAll((root,query,cb)->{
			return cb.equal(root.get("dataTable").get("id"), tb.getId());
		});
		
		// 分页设置对象
		RowSelection selection = new RowSelection();
		selection.setFirstRow(tInfo.getOffset());
		selection.setMaxRows(tInfo.getLimit());
		
		return queryValues(selection, tb, source, columns);
	}

	/**
	 * @param tInfo
	 * @param tb
	 * @param source
	 * @param columns
	 * @return
	 */
	@Deprecated
	public List<Map<String, String>> queryValues(RowSelection selection, DataTable tb,
			Source source, List<TableColumn> columns) {
		
		StringBuffer sb = appendToSql(tb, columns);
		// 获取数据源对应的数据库方言
		Dialect dialect = DialectUtil.getDialect(source.getDatabaseName());
		// SQL语句的分页设置器
		LimitHandler limitHandler = dialect.getLimitHandler();
		// 设置分页后的SQL数据
		String sql = limitHandler.processSql(sb.toString(), selection);
		logger.debug(sql);
		// 操作数据源的jdbcTemplate
		JdbcTemplate template = dsService.getTemplate(source);
		DataSource ds = template.getDataSource();
		// 查询结果存放的数组
		List<Map<String,String>> list = new LinkedList<>();
		try (Connection conn = ds.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);){
			// 为数据库操作语句对象绑定参数
			limitHandler.bindLimitParametersAtEndOfQuery(selection, ps, 1);
			// 执行查询
			try(ResultSet rs = ps.executeQuery();){
				// 解析查询结果
				while(rs.next()){
					Map<String,String> map = new LinkedHashMap<>();
					columns.forEach(elem->{
						try {
							String columnName = elem.getColumnName();
							String columnValue = rs.getString(columnName);
							map.put(columnName, columnValue);
						} catch (Exception e) {
							logger.error(e.getMessage());
						}
					});
					list.add(map);
				}
			};
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param tb
	 * @param columns
	 * @return
	 */
	public StringBuffer appendToSql(DataTable tb, List<TableColumn> columns) {
		StringBuffer sb = new StringBuffer();
		// 创建SQL语句中药查询的字段
		columns.forEach(elem->{
			String columnName = elem.getColumnName();
			sb.append(columnName).append(",");
		});
		sb.deleteCharAt(sb.length()-1);
		// 添加select关键字
		sb.insert(0, "select ");
		// 添加from关键词和数据表名称
		sb.append(" from ").append(tb.getName());
		return sb;
	}

	@Override
	public List<Map<String, String>> getValues(DataTable table) {
		if(table.getClass().equals(DataTable.class)){
			return getValues(new TableData(table.getId(),1000,0));
		}else{
			// 分页设置对象
			RowSelection selection = new RowSelection();
			selection.setFirstRow(0);
			selection.setMaxRows(1000);
			return queryValues(selection, table, table.getSource(), table.getColumns());
		}
	}
	
	public List<Map<String,String>> getValues(DataTable table,List<TableColumn> columns){
		// 分页设置对象
		RowSelection selection = new RowSelection();
		selection.setFirstRow(0);
		selection.setMaxRows(20);
		Source source = table.getSource();
		if(source == null){
			source = dsRepos.findOne((root,query,cb)->{
				Join<Source, DataTable> join = root.join("tables");
				Predicate equal = cb.equal(join.get("id"), table.getId());
				return equal;
			}).get();
		}
		List<Map<String, String>> list = queryValues(selection, table, source, columns);
		return list;
	}

	@Override
	public List<DataTable> getByVirtualColumns(List<VirtualColumn> columns) {
		List<Object> list = getDataTableIds(columns);
		if(!list.isEmpty()){
			List<DataTable> tables = tabRepos.findAll((root,query,cb)->{
				return root.get("id").in(list);
			});
			return tables;
		}
		return new ArrayList<>();
	}

	/**
	 * @param columns
	 * @return
	 */
	private List<Object> getDataTableIds(List<VirtualColumn> columns) {
		String sql = "SELECT DISTINCT "
					+ "d.id id "
				+ "FROM "
					+ "data_table d, "
					+ "table_column t, "
					+ "virtual_column_ref_columns r "
				+ "WHERE "
					+ "r.ref_columns_id = t.id "
					+ "AND d.id = t.data_table_id "
					+ "AND r.reference_by_id IN ";
		StringBuffer sb = new StringBuffer(sql);
		sb.append("( ");
		List<Object> params = new ArrayList<>();
		columns.forEach(elem->{
			sb.append("? , ");
			params.add(elem.getId());
		});
		sb.delete(sb.length()-3, sb.length());
		sb.append(")");
		sql = sb.toString();
		logger.debug(sb);
		JdbcTemplate jdbcTemplate = this.context.getBean("jdbcTemplate",JdbcTemplate.class);
		List<Object> list = jdbcTemplate.query(sql, (rs,i)->{
			Object object = rs.getObject("id");
			return object;
		},params.toArray());
		return list;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}

}
