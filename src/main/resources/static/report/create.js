Vue.http.options.emulateJSON=true
Vue.http.options.withCredentials=true
var app = new Vue({
	el : "#app",
	data:{
		report : {},	// 当前操作的报表
		pictures : [],	// 报表中包含的所有图片
		dragedPic : {},	// 当前操作的图片对象
		charts : []		,// echarts对象数组
		dragList : [
			{
				name : '条形图',
				src : '../res/images/bar-chart1.png',
				type : 'bar'
			},
			{
				name : '折线图',
				src : '../res/images/line-chart1.png',
				type : 'line'
			},
			{
				name : '饼状图',
				src : '../res/images/pie-chart1.png',
				type  : 'pie'
			}
		]
	},
	created:function(){
		this.report = JSON.parse(localStorage['report'])
		setWidthAndHeight(this.report);
		this.pictures = [];
		Vue.http.get(basePath+"report/graphs/"+this.report.id).then((res)=>{
			if(res.body.code == 1){
				for(var i in res.body.data)
					this.pictures.push(new Graph(res.body.data[i]))
			}else{
				layer.msg(res.body.message)
			}
		})
	},
	methods:{
		drag : function(data,event){ // 开始拖动
			this.dragedPic = new Picture(600,400)
			this.dragedPic.title = this.dragList[data].name
			this.dragedPic.type = this.dragList[data].type
			this.dragedPic.report = this.report
		},
		allowDrop : function(event){ // 允许
			event.preventDefault()
		},
		drop : function(event){
			event.preventDefault()
			var dom = document.querySelector("div.content");
			
			// 生成一个默认的图表title
			for(var i =0; i < this.pictures.length ; i++){
				if(this.dragedPic.title ==  this.pictures[i].title){
					var j = 1;
					while(this.dragedPic.title + j == this.pictures[i].title){
						j++
					}
					this.dragedPic.title = this.dragedPic.title + j;
				}
			}
			this.dragedPic.moveToClient(event.offsetX- this.dragedPic.width / 2,event.offsetY- this.dragedPic.height / 2)
			Vue.http.post(basePath+"graph",tile(this.dragedPic)).then((res)=>{
				if(res.body.code==1){
					var graph = new Graph(res.body.data)
					this.pictures.push(graph)
				}else{
					layer.msg(res.body.message)
				}
			},function(error){
				layer.msg("网络错误")
			})
			// this.dragedPic.moveToClient(event.offsetX- this.dragedPic.width / 2,event.offsetY- this.dragedPic.height / 2)
			// this.pictures.push(this.dragedPic)
		},
		getStyle : function(pic,event){
			var style = "position:absolute;width:"+pic.width+"px;height:"+pic.height+"px;left:"+pic.left+"px;top:"+pic.top+"px";
			console.log(style)
			return style;
		},
		drawGraph : function(){ // 绘制
			console.log("绘制图形")
			while(x=this.charts.pop()){
				x.dispose()
			}
			for(var i in this.pictures){
				var graph = this.pictures[i];
				var dom = document.querySelector("#graph-"+i)
				var myChart = echarts.init(dom)
				this.$http.get(basePath+"graph/data",{params:tile(graph)}).then(function(res){
					var data = res.body;
					var option = {
						title: {
							text: this.pictures[i].title
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
					console.log(option)
					myChart.setOption(option)
					this.charts.push(myChart)
					function getLegends(graph){
						var a = new Array();
						for(var index in graph.yAxis){
							a.push(graph.yAxis[index].chinese?graph.yAxis[index].chinese:graph.yAxis[index].name)
						}
						return a
					}
					// var legends = getLegends(this.pictures[i]);
					function getSeries(y,legends){
						var array = []
						for(var index =0; index<y.length; index++){
							obj = {
								name : legends[index],
								type : graph.type,
								data : y[index]
							}
							array.push(obj)
						}
						return array;
					}
				})
			}
		},
		rmGraph : function(pic,event){
			layer.confirm("您确定要删除吗？",{
				title : "确认",
				btn : ["确定","取消"]
			},(index,layero)=>{
				layer.close(index)
				this.$http.delete(basePath+"graph/"+pic.id).then(function(res){
					if(res.body.code == 1){
						this.pictures = this.pictures.filter(item=>{return item != pic})
					}else{
						layer.msg(res.body.message)
					}
				},function(error){
					layer.msg("网络异常")
				})
				return true;
			},function(index,layero){
				return true;
			})
			event.stopPropagation()
		},
		setting : function(pic,event){ // 弹出轴编辑框
			dialog.graph = pic;
			var index = layer.open({
				title : pic.title,
				type : 1,
				content : $("#dialog"),
				area : ['700px','500px'],
				maxmin : true,
				cancel : function(index,layero){
					/* layer.confirm("您确定要关闭吗？",{
						btn : ['确定','取消']
					},function(i,l){
						layer.close(i)
						layer.close(index);
					}) */
					return true;
				},
				end : function(){
					$("#dialog").hide()
				}
			})
			layer.full(index)
		}
	},
	mounted:function(){
		this.drawGraph()
	},
	updated:function(){
		this.drawGraph()
	},
	watch:{
		"pictures" :{ 
			handler:function(newVal,oldVal){
				if(newVal.length == oldVal.length){
					
				}else{
					
				}
			},
			deep: true
		}
	}
})


function absolute(graph,w,h){
	graph = Cloner(graph)
	graph.width *= w;
	graph.height *= h;
	graph.top *= h;
	graph.left *= w;
	return graph;
}

function relative(graph,w,h){
	var g = new Object();
	g.width = graph.width / w;
	g.height = graph.height  / h;
	g.top = graph.top / h;
	g.left = graph.left / w;
	return g;
}

function setWidthAndHeight(report){
	var dom = document.querySelector("div.content")
	if(report.width == dom.offsetWidth && report.height == dom.offsetHeight){
		return;
	}
	report.width = dom.offsetWidth
	report.height = dom.offsetHeight
	$.ajax({
		url : basePath+"report",
		method : "post",
		data : report,
		success : function(res){
			if(res.code == 0){
				layer.msg(res.message)
			}
		},
		error :function(error){
			layer.msg("网络异常")
		}
	})
}





function drag(event){
	event.dataTransfer.setData("Text","马小红");
}
function allowDrop(event){
	event.preventDefault()
}
function drop(event){
	event.preventDefault()
	var data = event.dataTransfer.getData("Text")
	alert(data)
}

function Picture(w,h,top,left){
	this.width = w ? w :500;
	this.height = h ? h : 200;
	this.top = top ? top : 0;
	this.left = left ? left : 0;
	this.moveToClient = function(x,y){
		if(x < 0) x = 0;
		if(y < 0) y = 0;
		this.left = x;
		this.top = y;
	}
}
function Graph(g){
	for(var i in g){
		this[i] = g[i]
	}
	this.Picture = Picture
	this.Picture(g.width,g.height,g.top,g.left)
}

/**
 * 当鼠标指针进入报表元素中时，显示调节大小的8个点
 */
function mouseenter(event){
	var index = event.currentTarget.dataset.index
	var content = '<div class="northwest-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="north-point" data-index = "'+index+'"></div><div class="northeast-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="east-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="southeast-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="south-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="southwest-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div><div class="weast-point" onmousedown="resizeWidthAndHeight(event)" data-index = "'+index+'"></div>';
	var $s = $(content)
	var $target = $(event.currentTarget)
	$target.css("cursor","all-scroll")
	$target.append($s)
	/**
	 * 当鼠标离开报表div元素时
	 */
	event.currentTarget.onmouseleave = function(e){
		$s.remove()
		$target.css("cursor","auto")
		$target.find("div.report-toolbar").slideUp('fast')
		e.stopPropagation()
	}
	$target.children("div.report-content").bind("mouseenter mouseover mousedown mouseup",function(e){
		// 显示工具栏
		$target.find("div.report-toolbar").slideDown('fast')
		// 移除调节报表大小的指示点
		$s.remove()
		// 重新设置光标
		$target.css("cursor","auto")
		e.stopPropagation();
	})
	event.stopPropagation()
}

/**
 * 当报表元素上按下鼠标
 */
function capture(event){
	event.stopPropagation()
	var index = event.currentTarget.dataset.index
	var pic = app.pictures[index];
	var oldleft = pic.left ,oldtop = pic.top;
	$("div.content").bind("mousemove",function(e){
		console.log(e.target.tagName+"."+e.target.className,e.clientX,e.clientY)
		pic.moveToClient(oldleft + e.clientX - event.clientX,oldtop + e.clientY - event.clientY)
		e.stopPropagation()
	})
}
function release(event){
	$("div.content").unbind("mousemove")
	var index = event.currentTarget.dataset.index
	var pic = app.pictures[index];
	saveGraph(pic);
	event.stopPropagation()
}

/**
 * 拖动时改变大小
 */
function resizeWidthAndHeight(event){
	// $(event.target).css("display","block");
	var ph = event.target.parentNode.clientHeight,pw = event.target.parentNode.clientWidth;
	event.stopPropagation();
	var $p = $(event.target);
	var pic = app.pictures[event.target.dataset.index];
	$("div.content").bind("mousemove",function(e){
		if($p.hasClass("southeast-point")){
			var w = e.clientX - event.clientX;
			var h = e.clientY - event.clientY;
		}
		else if($p.hasClass("northwest-point")){
			var w = event.clientX - e.clientX;
			var h = event.clientY - e.clientY;
		}
		else if($p.hasClass("northeast-point")){
			var w = e.clientX - event.clientX;
			var h = event.clientY - e.clientY;
		}
		else if($p.hasClass("southwest-point")){
			var w = event.clientX - e.clientX;
			var h = e.clientY - event.clientY;
		}
		else if($p.hasClass("west-point")){
			var w = event.clientX - e.clientX;
			var h = 0;
		}
		else if($p.hasClass("east-point")){
			var w = e.clientX - event.clientX;
			var h = 0;
		}
		else if($p.hasClass("north-point")){
			var w = 0;
			var h = event.clientY - e.clientY;
		}
		else if($p.hasClass("south-point")){
			var w = 0;
			var h = e.clientY - event.clientY;
		}
		else{
			var w = 0, h = 0;
		}
		pic.width = pw + w;
		pic.height = ph + h;
		e.stopPropagation()
	})
	document.onmouseup = function(e){
		e.stopPropagation()
		$("div.content").unbind("mousemove")
		saveGraph(pic);
	}
}

function saveGraph(graph){
	$.ajax({
		url : basePath+"graph",
		method : "post",
		data : tile(graph),
		success : function(res){
			if(res.code == 0){
				layer.msg(res.message)
			}
		},
		error : function(error){
			layer.msg("网络异常")
		}
	})
}

function load(target){
	alert(JSON.stringify(target))
}



/******************************************************/
Vue.component("tree",{
	template : '<ul class="tree">'
				+'		<li class="tree-node" v-for="node in nodes" @dragstart="dragstart(node,$event)">'
				+'			<i :class="foldIcon(node)" @click="toggle(node,$event)"></i>'
				+'			<a href="javascript:;" @click="nodeClick(node,$event)" @dblclick="toggle(node,$event)" ><i :class="nodeIcon(node)"></i>{{node.name}}</a>'
				+'			<tree v-bind:asyn="asyn" v-bind:nodes="node.children" v-if="show(node)" v-on:nodeclick="nodeClick($event)" v-on:loadchildren="loadChildren($event)" v-on:nodedragstart="dragstart(null,$event)"></tree>'			
				+'		</li>'
				+'</ul>',
	props:{
		nodes : {
			type : Array,
			required :true
		},
		asyn : {
			type : Boolean,
			default : false
		}
	},
	created:function(){
		console.log(this.asyn)
	},
	methods:{
		show:function(node){
			if(node.children&&node.spread){
				return true;
			}else{
				return false;
			}
		},
		foldIcon : function(node){
			if( this.asyn ){
				if(node.spread){
					return "layui-icon layui-icon-triangle-d"
				}else{
					return "layui-icon layui-icon-triangle-r"
				}
			}else{
				return node.children? node.spread ? "layui-icon layui-icon-triangle-d":"layui-icon layui-icon-triangle-r":""
			}
		},
		nodeIcon : function(node){
			if(node.children){
				if(node.icon){
					return node.spread ? node.icon.open : node.icon.close;
				}else{
					return "layui-icon layui-icon-table";
				}
			}
			else{
				return "layui-icon layui-icon-file"
			}
		},
		toggle : function(node,event){
			if(this.asyn && ! node.spread && ! node.children){
				Vue.set(node,"children",[])
				this.$emit("loadchildren",node)
				this.requested = true;
			}
			if(node.spread == undefined){
				Vue.set(node,'spread',false)
			}else{
				node.spread = !node.spread
			}
			event.stopPropagation()
		},
		nodeClick : function(node,event){
			this.$emit("nodeclick",node)
		},
		loadChildren : function(event){
			this.$emit("loadchildren",event)
		},
		dragstart : function(node,event){ // 向父容器发送拖动事件的消息
			if(node){
				event.nodeData = node;
			}
			this.$emit("nodedragstart",event)
			if(event) event.stopPropagation()
		}
	}
})



var dialog = new Vue({
	el : "#dialog",
	data : {
		nodes : [],
		xFields: [],// 当前已添加的x轴上的字段
		yFields : [] ,// 当前已添加的y轴上的字段
		graph : {}
	},
	methods:{
		select : function(node){
			alert("select a node")
		},
		loadChildren : function(event){
			// alert("loadChildren")
			var indexs = event.id.search(/\d+/)
			var x = event.id.substring(0,indexs)
			var url = null;
			switch(x){
				case "PKG":
				url = basePath+"pkg/tab/"+event.id
				break;
				case "VT":
				url = basePath+"vt/vc/"+event.id
				break;
				case "VC":
				event.children = null;
				return;
			}
			this.$http.get(url).then(function(res){
				for(var i in res.body.data){
					var elem = res.body.data[i]
					if(elem.chinese) elem.name= elem.chinese ;//+ "("+elem.name+")";
					if(elem.typeName) elem.name+= ":("+elem.typeName+")"
					event.children.push(elem)
				}
				event.spread = true;
			});
		},
		addAxis : function(index,event){
			if(!this.dragTarget) return;
			var layerIndex = layer.load(1);
			this.$http.get(basePath+"graph/"+this.graph.id+"/"+this.dragTarget.id+"/"+index).then(function(res){
				layer.close(layerIndex)
				if(res.body.code == 1){
					this.graph[index].push(this.dragTarget)
				}
				else{
					layer.msg(res.body.message)
				}
			},function(error){
				layer.close(layerIndex)
				layer.msg("网络错误")
			})
		},
		rmAxis : function(axis,field,event){
			// this.$http.delete(basePath+"graph/"+this.graph.id+"/"+field.id+"")
			layer.confirm("您确定要移除字段"+(field.chinese?field.chinese:field.name)+"吗?",{
				title : "确认",
				btn : ["确定","取消"]
			},(index,layero)=>{
				this.graph[axis] = this.graph[axis].filter(elem => {return elem.id != field.id})
				this.saveGraph(this.graph)
				layer.close(index)
			},function(index,layero){})
		},
		startDrag : function(event){
			this.dragTarget = event.nodeData.id.startsWith("VC") ?  event.nodeData : null;
			event.stopPropagation()
		},
		getGraph : function(id){
			this.$http.get(basePath+"graph/"+id).then(function(res){
				this.graph = res.body.data;
			})
		},
		saveGraph : function(graph){
			this.$http.post(basePath+"graph",tile(graph)).then(function(res){
				if(res.body.code == 0){
					layer.msg(res.body.message)
				}
			},function(error){
				layer.msg("网络错误")
			})
		}
	},
	created:function(){
		this.$http.get(basePath+"pkg").then(function(res){
			this.nodes = res.body.data.content;
			for(var i in res.body.data.content){
				res.body.data.content[i].icon = {
					open : "layui-icon layui-icon-cart-simple",
					close : "layui-icon layui-icon-cart"
				}
			}
		})
	}
})

dialog.$on("loadChildren",function(event){
	alert(2)
})