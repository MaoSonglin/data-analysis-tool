package dat.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import com.tsc9526.monalisa.core.query.datatable.DataMap;
import com.tsc9526.monalisa.core.query.datatable.DataTable;

import dat.domain.VirtualColumn;
import dat.domain.VirtualTable;
import dat.service.WorkPackageService;

@Service("virtualTableServiceImpl")
public class VirtualTableService extends VirtualTableServiceImpl {
	
	private static Logger logger = Logger.getLogger(VirtualTableService.class);
	
	@Override
	public boolean extract(VirtualTable table) {
		// 数据表
		VirtualTable virtualTable = getVtRepos().findById(table.getId()).get();
		// 获取表中的数据
		DataTable<DataMap> tableBody = getTableBody(table.getId());
		// 列
		List<VirtualColumn> columns = virtualTable.getColumns();
		
		String sql = insertSql(virtualTable);
		WorkPackageService packageService = getContext().getBean(WorkPackageService.class);
		
		logger.debug(sql);
		// sqlite数据库只能单线程访问，所有使用线程锁
		synchronized (this) {
			try (Connection conn = packageService.getConnection(virtualTable.getPackages().iterator().next().getId());// 数据包所在数据库连接
					PreparedStatement ps = conn.prepareStatement(sql)){
				// 禁止自动提交
				conn.setAutoCommit(false);
				// 批处理执行频率
				int j = 1000;
				for (DataMap dataMap : tableBody) {
					// 遍历查询的数据字段,设置SQL语句的参数
					for(int i = 0 , size = columns.size(); i < size; i++){
						String name = columns.get(i).getName();
						Object value = dataMap.get(name);
						ps.setObject(i+1, value);
					}
					// 添加批处理
					ps.addBatch();
					if(--j == 0){
						// 执行批处理
						ps.executeBatch();
						ps.clearBatch();
						j = 1000; 
					}
				}
				ps.executeBatch();
				conn.commit();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
