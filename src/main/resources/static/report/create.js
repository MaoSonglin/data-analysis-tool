Vue.http.options.emulateJSON=true
Vue.http.options.withCredentials=true

var graphTypes = [
	{
		name : '条形图',
		src : '../res/images/bar-chart1.png',
		type : 'bar'
	},
	{
		name : '折线图',
		src : '../res/images/line-chart1.png',
		type : "line"
	},
	{
		name : '饼状图',
		src : '../res/images/pie-chart1.png',
		type : "pie"
	}
]
let report = JSON.parse(localStorage['report'])
// delete localStorage['report']
var app = new Vue({
	el : "#app",
	data:{
		report : {},	// 当前操作的报表
		pictures : [],	// 报表中包含的所有图片
		dragedPic : {},	// 当前操作的图片对象
		charts : []		,// echarts对象数组
		dragList : null
	},
	created:function(){
		this.dragList = graphTypes
		this.report = JSON.parse(localStorage['report'])
		setWidthAndHeight(this.report);
		this.pictures = [];
		Vue.http.get(basePath+"report/graphs/"+this.report.id).then((res)=>{
			if(res.body.code == 1){
				if(res.body.data.length){
					for(var i in res.body.data){
						var g = new Graph(res.body.data[i]);
						g.option = JSON.parse(g.options);
						this.pictures.push(g)
					}
				}else{
					addNewGraph()
				}
			}else{
				layer.msg(res.body.message)
			} 
		})
		let that = this
		function addNewGraph(){
			let data = new Picture()
			data.title = "图表"
			data.report = that.report
			Vue.http.post(basePath+"graph",tile(data)).then(res=>{
				if(res.body.code == 1){
					that.pictures.push(new Graph(res.body.data))
				}
				layer.msg(res.body.message)
			})
		}
	},
	methods:{
		drag : function(data,event){ // 开始拖动
			this.dragedPic = new Picture(600,400)
			// this.dragedPic.title = this.dragList[data].name
			// this.dragedPic.type = this.dragList[data].type
			this.dragedPic.report = this.report
			// this.dragedPic.options = this.dragList[data].option;
		},
		allowDrop : function(event){ // 允许
			event.preventDefault()
		},
		drop : function(event){
			event.preventDefault() 
			// 设置图表位置 
			this.dragedPic.moveToClient(event.offsetX- this.dragedPic.width / 2,event.offsetY- this.dragedPic.height / 2)
			// 保存图表
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
		},
		getStyle : function(pic,event){
			var style = "position:absolute;top:"+0+"px;right:"+0+"px;bottom:"+0+"px;left:"+0+"px";
			console.log(style)
			return style;
		},
		drawGraph : function(){ // 绘制
			console.log("绘制图形")
			while(x=this.charts.pop()){
				x.dispose()
			} 
			for(var i in this.pictures){
				let graph = this.pictures[i];
				let dom = document.querySelector("#graph-"+i)
				let myChart = echarts.init(dom)
				let option = JSON.parse(graph.options) 
				if(option)
				myChart.setOption(option)
				myChart.on("click",function(param){
					alert(1)
				})
				this.$http.get(basePath+"graph/data/"+graph.id+"/2/2").then(function(res){
					if(res.body.code){
						myChart.setOption({
							dataset : {
								source : res.body.data
							}
						})
						this.charts.push(myChart) 
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
			// window.parent.addParentTab({href:'./html/article/detail.html',title:'强化党内监督是全面从严治党重要保障'})
			localStorage.graph = JSON.stringify(pic)
			var index = layer.open({
				title : pic.title,
				type : 2,
				content : /* $("#dialog"),// */'setting.html',
				area : ['700px','500px'],
				maxmin : true,
				end : function(){
					
				},
				btn1 : function(index,layero){
					layer.close(index);
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
		data : tile(report),
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

function load(target){ 
}



/******************************************************/



	

/* */



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
	var $target = $(event.currentTarget)
	$target.children().hide()
	$(document).bind("mouseup",function(e){
		$target.trigger("mouseup")
	})
	$target.bind("mouseup",function(e){
		release(event)
		e.stopPropagation()
	})
}
function release(event){
	$("div.content").unbind("mousemove")
	var index = event.target.dataset.index
	var pic = app.pictures[index];
	var $target = $(event.target)
	$target.children().show()
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
	$(document).bind("mouseup",function(e){
		$("div.content").unbind("mousemove")
		saveGraph(pic);
		e.stopPropagation()
		$(document).unbind("mouseup")
	}) 
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




