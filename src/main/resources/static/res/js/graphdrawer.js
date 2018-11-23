
function Bar(dom,graph,data){
	var option = {
		title: {
			text: graph.title
		},
		tooltip: {},
		legend: {
			data:getLegends(graph)
		},
		xAxis: {
			name : graph.xAxis.pop(),
			data: data.values.x.pop()
		},
		yAxis: {},
		series:getSeries(data.values.y,getLegends(graph)) /* [{
		}] */
	};
	function getLegends(graph){
		var a = new Array();
		for(var index in graph.yAxis){
			a.push(graph.yAxis[index].chinese?graph.yAxis[index].chinese:graph.yAxis[index].name)
		}
		return a
	}
	function getSeries(y,legends){
		var array = []
		for(var index =0; index<y.length; index++){
			obj = {
				name : legends[index],
				type : 'bar',
				data : y[index]
			}
			array.push(obj)
		}
		return array;
	}
	var myChart = echarts.init(dom)
	myChart.setOption(option)
	return myChart;
}