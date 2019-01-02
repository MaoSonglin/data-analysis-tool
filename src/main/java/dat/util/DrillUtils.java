package dat.util;

import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import dat.data.LocalDataAdapter.SqlBuilder;
import dat.domain.VirtualColumn;
import dat.vo.EchartOptions;
import dat.vo.GraphDrillData;
import dat.vo.Serie;
import dat.vo.EchartOptions.Axis;
import dat.vo.EchartOptions.DataSet;
import dat.vo.EchartOptions.DataZoom;
import dat.vo.EchartOptions.ItemStyle;

/**
 * @author MaoSonglin
 * dat.service.impl.GraphInfoServiceImpl.drill(GraphDrillData)使用的工具类
 */
public class DrillUtils {

	/**
	 * @param options
	 * @return
	 */
	public static EchartOptions parseOptions(String options) {
		EchartOptions echartOptions = JSON.parseObject(options, EchartOptions.class);
		// 各种json解析工具似乎都不能直接将'xAxis'这种格式名称的属性封装到javabean中，所有手动封装
		JSONObject object = JSON.parseObject(options);
		Object o = object.get("xAxis");
		if(o != null){
			Axis xAxis = JSON.parseObject(o.toString(), Axis.class);
			echartOptions.setXaxis(xAxis);
		}
		Object o2 = object.get("yAxis");
		if( o2 != null){
			Axis yAxis = JSON.parseObject(o2.toString(),Axis.class);
			echartOptions.setyAxis(yAxis);
		}
		return echartOptions;
	}

	/**
	 * @param echartOptions
	 * @param list
	 */
	public static void setDataSet(EchartOptions echartOptions, List<List<String>> list) {
		DataSet dataSet = new DataSet();
		dataSet.setSource(list);
		echartOptions.setDataset(dataSet);
	}

	/**
	 * @param echartOptions
	 * @param list
	 */
	public static void setDataZoom(EchartOptions echartOptions,
			List<List<String>> list) {
		if(list.size()>0&&list.get(0).size() > 5){
			int size = list.get(0).size();
			DataZoom dataZoom = new DataZoom();
			dataZoom.setEnd(5f / size * 100f);
			echartOptions.setDataZoom(dataZoom);
		}
	}

	/**
	 * @param virtualColumn
	 * @param echartOptions
	 */
	public static void setAxis(VirtualColumn virtualColumn,
			EchartOptions echartOptions) {
		Axis getyAxis = echartOptions.getyAxis();
		Axis getxAxis = echartOptions.getxAxis();
		if(getyAxis==null && getxAxis==null){
			// x轴
			Axis xAxis = new Axis();
			xAxis.setType("category");
			xAxis.setName(virtualColumn.getChinese());
			// y轴
			Axis yAxis = new Axis();
			echartOptions.setyAxis(yAxis);
			echartOptions.setxAxis(xAxis);
		}else if(!virtualColumn.getId().equals(getxAxis.getColumnId())){
			getxAxis.setName(virtualColumn.getChinese() != null ? virtualColumn.getChinese() : virtualColumn.getName());
		}
	}

	/**
	 * @param echartOptions
	 */
	public static void setSeriesColor(EchartOptions echartOptions) {
		List<Serie> series2 = echartOptions.getSeries();
		Random random = new Random(System.currentTimeMillis());
		
		// 设置每个系列的颜色
		for (Serie serie : series2) {
			ItemStyle itemStyle = new ItemStyle();
			int nextInt = random.nextInt(255);
			int nextInt2 = random.nextInt(255);
			int nextInt3 = random.nextInt(255);
			itemStyle.setColor("rgb("+nextInt+","+nextInt2+","+nextInt3+")");
			serie.setItemStyle(itemStyle);
		}
	}

	/**
	 * @param drillData		钻取设置
	 * @param virtualColumn	分类标准
	 * @param columns		待查询的字段数组
	 * @return
	 */
	public static SqlBuilder getSqlInfo(GraphDrillData drillData,
			VirtualColumn virtualColumn, List<VirtualColumn> columns) {
		return (tableName,sqlInfo)->{
			StringBuffer buffer = new StringBuffer("SELECT ");
			// 遍历待查询的字段数组
			columns.forEach(elem->{
				// 如果这个字段是分类标准
				if(elem == virtualColumn){
					buffer.append(elem.getName()).append(" , ");
					return;
				}
				
				// 如果是其他字段
				String functionName = null;
				if("Number".equalsIgnoreCase(elem.getTypeName())){
					// 如果是number类型
					functionName = "sum";
				}else{
					// 如果是其他类型
					functionName = "count";
				}
				// 添加函数
				SqlHelper.addFunction(buffer,functionName,elem.getName(),elem.getName());
				buffer.append(" , ");
			});
			// 删除缓存字符最后的逗号
			SqlHelper.deleteLast(buffer, 3);
			// 添加表名称
			buffer.append(" FROM ").append(tableName);
			// 添加过滤条件
			buffer.append(" WHERE ").append(virtualColumn.getName()).append(" IS NOT NULL");
			List<String> wheres = drillData.getWheres();
			for(int i = 0, size = wheres.size(); i < size-1; i+=2){
				buffer.append(" AND ").append(wheres.get(i)).append(" = ? ");
				sqlInfo.getParams().add(wheres.get(i+1));
			}
			buffer.append(" GROUP BY ").append(virtualColumn.getName());
			sqlInfo.setSql(buffer.toString());
		};
	}
}
