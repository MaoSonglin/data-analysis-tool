Vue.http.options.emulateJSON=true
Vue.http.options.withCredentials=true

Vue.component('size', {
	template: '<div class="drag abs" @mousedown="startMove(graph,$event)" >' +
		'<div v-for="point in points" v-bind:class="point" @mousedown="startResize(graph,$event)">' +
		'</div>' +
		'</div>',
	props: {
		graph: {
			type: Object,
			required: true
		},
		points: {
			type: Object,
			default: function() {
				return ["north-point", "east-point", "south-point", "weast-point", "northeast-point", "southeast-point",
					"southwest-point", "northwest-point"
				]
			}
		},
		container: {
			type: String,
			default: "#app"
		}
	},
	methods: {
		startMove: function(graph, event) { // 开始移动
			let that = this
			$(this.container).bind("mousemove", function(e) {
				let width = $(this).width()
				let height = $(this).height()
				if (e.originalEvent.movementX || e.originalEvent.movementY) {
					let w = e.originalEvent.movementX / width * 100
					let h = e.originalEvent.movementY / height * 100
					graph.left += w
					graph.top += h
					that.repos = true
				}
			})
			this.end()
		},
		startResize: function(graph, event) { // 开始设置大小
			event.stopPropagation()
			let that = this
			$(this.container).bind("mousemove", function(e) {
				let width = $(this).width()
				let height = $(this).height()
				if (e.originalEvent.movementX || e.originalEvent.movementY) {
					let w = e.originalEvent.movementX / width * 100
					let h = e.originalEvent.movementY / height * 100
					graph.width += w
					graph.height += h
					that.repos = true
				}
			})
			this.end()
		},
		end: function() {
			$(this.container).bind("mouseup", () => {
				$(this.container).unbind("mousemove")
				$(this.container).unbind("mouseup")
				if (this.repos) {
					this.$emit("repos", this.graph)
					this.repos = false
				}
			})
		}
	},
	watch: {
		"graph.left": function(newVal, oldVal) {

		},
		"graph.top": function(newVal, oldVal) {

		},
		"graph.width": function(newVal, oldVal) {

		},
		"graph.height": function(newVal, oldVal) {

		},
	}
})


// 指定图表的配置项和数据
var option = {
	title: {
		text: 'ECharts 入门示例'
	},
	tooltip: {},
	legend: {
		data: ['销量']
	},
	xAxis: {
		data: ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子"]
	},
	yAxis: {},
	series: [{
		name: '销量',
		type: 'bar',
		data: [5, 20, 36, 10, 10, 20]
	}]
};


let component = Vue.component("echart", {
	template: '<div :class="cls" :id="id"><div class="abs echart"></div>' +
		'</div>',
	props: {
		graph: {
			type: Object,
			required: true
		},
		id: {
			type: String,
			required: true
		},
		"cls": {
			type: String,
			default: ''
		}
	},
	created: function() {
		this.categories = this.graph.option.categorys.filter(elem=>{return true})
// 		this.drillList = []
// 		this.wheres = []
		let series = this.graph.option.series
		this.drillData = {newCategory:null,oldCategory:null,graphId:this.graph.id,wheres:[]},
		this.stack = new Stack()
		this.getData(this.drillData)
	},
	mounted: function() {
		this.echart = echarts.init(this.$el.querySelector(".echart"))
		this.graph.option = JSON.parse(this.graph.options)
		this.echart.setOption(this.graph.option)
		this.echart.showLoading()
		console.log(JSON.parse(this.graph.options))
		this.$emit("draw",this.echart)
		this.echart.on("click",(param)=>{
			console.log("echart click")
			this.$emit("echart",param)
		})
		this.echart.on("datazoom",param=>{
			// this.$emit("echart",param)
		})
		let drill = false,param = null
		this.echart.on('mouseover',(args)=>{
			drill = true
			param = args
			this.$emit("echart",args)
		})
		this.echart.on('mouseout',(args)=>{
			drill = false
			this.$emit("echart",args)
		})
		let that = this
		// alert(JSON.stringify(that.graph))
		let menu = new BootstrapMenu('#' + this.id, {
			actionsGroups: [
				['setting',"delete"],
				['drillUp', 'drillDown','rotate']
			],
			fetchElementData: function($rowElem) {
				return param
			},
			actions: {
				"drillUp": {
					name: "上钻",
					onClick: function(row) {
						that.drillUp(row)
					},
					isShown: function(row) {
						return drill
					},
					isEnabled : function(row){
						return that.drillData.wheres.length > 1
					}
				},
				"drillDown": {
					name: "下钻",
					onClick: function(row) {
						that.drillDown(row)
					},
					isShown: function(row) {
						return drill
					},
					isEnabled : function(row){
						return that.categories.length  > 0
					}
				},
				"rotate": {
					name: "旋转",
					onClick: function(row) {
						that.rotate()
					},
					isShown: function(row) {
						return ! drill
					}
				}
			}
		})
		this.$emit("mounted",this)
		
		this.$on("parentEvent",function(param){
			// alert("parentEvent")
			// console.log(param)
			let option = {type:param.type}
			switch(param.type){
				case 'datazoom':
				option.type = 'dataZoom'
				option.start = param.start,
				option.end = param.end
				break;
				case 'mouseover' :
				// option.startValue = param.dataIndex
				this.echart.dispatchAction({
					type : 'highlight',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				this.echart.dispatchAction({
					type : 'showTip',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				break;
				case 'mouseout':
				this.echart.dispatchAction({
					type :'downplay',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				this.echart.dispatchAction({
					type : 'hideTip',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				break
			}
			this.echart.dispatchAction(option)
		})
	},
	methods : {
		getData : function(data){
			console.log(data)
			this.$http.post(basePath+"graph/drill",tile(data)).then(function(res){
				console.log(res.body.data)
				if(res.body.data){
					formatOption(this.graph.option,res.body.data)
					this.echart.clear()
					this.echart.setOption(this.graph.option)
					this.echart.setOption({dataset:res.body.data})
					this.echart.hideLoading()
					this.$emit("data",res.body.data)
				}
			})
		},
		test : function(){
			alert("test")
		},
		drillDown : function(param){ // 下钻
			let serie = this.graph.option.series[param.seriesIndex],value = param.name
			// 1. 获取之前的分类维度名称
			let encode = standard(this.graph.option.xAxis,serie)
			// let itemName = serie.encode[serie.type == "pie" ? "itemName" : "x"]
			this.drillData.wheres.push(encode.itemName)
			this.drillData.wheres.push(value)
			// this.request()
			showDialog(this.categories.filter(elem=>{
				for(let i in this.drillData.wheres){
					if(this.drillData.wheres[i] == elem.name){
						return false
					}
				}
				return true
			}),(id)=>{
				for(let i in this.categories){
					if(this.categories[i].id == id){
						let category = this.categories[i]
						this.stack.push(category)
						
						// 上一次查询的分类标准
						// this.drillData.oldCategory = encode.itemName
						// 新的分类标准
						this.drillData.itemName = category.name
						// 更新坐标轴名称
						if(this.graph.option.xAxis){
							this.setAxisName(category.chinese?category.chinese:category.name)
						}
						let oldName = encode.itemName
						// 更新各个系列的维度解析方式
						for(let i in this.graph.option.series){
							let serie = this.graph.option.series[i]
							let encode = standard(this.graph.option.xAxis,serie)
							
							// 只更新维度相同的系列
							if(encode.itemName == oldName){
								serie.encode[encode.index] = category.name
							}
						}
						break
					}
				}
				this.$emit("drill",this.drillData)
			})
			// this.$on("drill",)
		},
		setSeriesName : function(seriesName){
			for(let i in this.graph.option.series){
				let serie = this.graph.option.series[i]
				serie.encode[serie.type == "pie" ? "itemName" : "x"] = seriesName
			}
		},
		setAxisName : function(itemName){
			if(this.graph.option.xAxis){
				if(!this.graph.option.yAxis.type || this.graph.option.yAxis.type == 'value'){
					this.graph.option.xAxis.name = itemName
				}else{
					this.graph.option.yAxis.name = itemName
				}
			}
		},
		drillUp : function(param){
			this.drillData.wheres.pop()
			this.drillData.wheres.pop()
			
			let pop = this.stack.pop()
			let top = this.stack.top()
			if(top){
				for(let i in this.graph.option.series){
					let serie = this.graph.option.series[i]
					let encode = standard( this.graph.option.xAxis,serie)
					if(encode.itemName == pop.name){
						serie.encode[encode.index] = top.name
					}
				}
				this.setAxisName(top.chinese?top.chinese:top.name)
			}else{
				this.graph.option = JSON.parse(this.graph.options)
				delete this.drillData.itemName 
			}
			this.$emit("drill",this.drillData)
		},
		drill : function(param){
			if(param != this.drillData){
				param.itemName = this.drillData.itemName
			}
			this.echart.showLoading()
			param.series = this.graph.option.series
			this.getData(param)
		},
		rotate : function(){
			let array = this.graph.option.categorys.filter(elem=>{return true})
			showDialog(array,(id)=>{
				this.drillData.wheres = []
				let column = {}
				for(let i in this.categories){
					if(this.categories[i].id == id){
						column = this.categories[i]
						this.setAxisName(column.chinese?column.chinese : column.name)
						break
					}
				}
				
				for(let i in this.graph.option.series){
					let serie = this.graph.option.series[i]
					let enc = standard(this.graph.option.xAxis,serie)
					serie.encode[enc.index] = column.name
				}
				this.$emit("drill",this.drillData)
			})
		},
	}
})

var standard = function(axis,serie){
	let oldCategory = null
	let index = null
	switch(serie.type){
		case 'pie':
		oldCategory = serie.encode.itemName
		index = 'itemName'
		break
		case 'line':
		case 'bar':
		if(axis && axis.type != 'value'){
			oldCategory = serie.encode.x
			index = 'x'
		}else{
			oldCategory = serie.encode.y
			index = 'y'
		}
		break
		default:
		break
	}
	return {itemName : oldCategory , index : index}
}


Vue.component("subchart",{
	template : "<div class='abs'></div>",
	props : {
		graph : {
			type : Object,
			required : true
		}
	},
	created : function(){
		this.graph.option = JSON.parse(this.graph.options)
	},
	mounted : function(){
		this.echart = echarts.init(this.$el)
		this.echart.setOption(this.graph.option)
		this.$emit("draw",this.echart)
		this.$emit("mounted",this)
		this.$on("parentEvent",function(param){
			// alert("parentEvent")
			// console.log(param)
			let option = {type:param.type}
			switch(param.type){
				case 'datazoom':
				option.type = 'dataZoom'
				option.start = param.start,
				option.end = param.end
				break;
				case 'mouseover' :
				// option.startValue = param.dataIndex
				this.echart.dispatchAction({
					type : 'highlight',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				this.echart.dispatchAction({
					type : 'showTip',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				break;
				case 'mouseout':
				this.echart.dispatchAction({
					type :'downplay',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				this.echart.dispatchAction({
					type : 'hideTip',
					seriesIndex : param.seriesIndex,
					dataIndex : param.dataIndex
				})
				break
			}
			this.echart.dispatchAction(option)
		})
	},
	methods : {
		setOption : function(data){
			try{
				this.echart.clear()
				let option = JSON.parse(this.graph.options)
				this.echart.setOption(JSON.parse(this.graph.options))
				console.log(option)
				console.log({dataset:data})
				this.echart.setOption({dataset:data})
			}catch(e){
				console.log(e)
			}
		}
	}
})


function choose(id){
	localStorage.columnId = id
	layer.closeAll()
}

function showDialog(array,callback){
	let content = "<ul>"
	for(let i in array){
		let col = array[i]
		content += "<li style='cursor:pointer' onclick='choose(\""+col.id+"\")'>"+(col.chinese?col.chinese:col.name)
	}
	content += "</ul>"
	layer.open({
		type : 0,
		title : "钻取",
		shade : false,
		content : content,
		btn : [],
		end : ()=>{
			if(localStorage.columnId&&callback){
				callback(localStorage.columnId)
				delete localStorage.columnId
			}
		}
	})
}


function rotate(matrix){
	let m = matrix.length,n = matrix[0].length
	let a = []
	for(let j = 0; j < n; j++){
		let array = []
		for(let i = 0; i < m ; i++){
			array.push(matrix[i][j])
		}
		a.push(array)
	}
	return a
}


function formatOption(option,dataset){
	for(let i in option.series){
		let serie = option.series[i]
		if(serie.type == 'line' || serie.type == 'bar'){
			if(dataset.source.length > 8){
				option.dataZoom = {
					show : true,
					type : 'slider',
					start : 10,
					end : 8 / dataset.source.length * 100 + 10
				}
				return
			}else{
				option.dataZoom = { show : false}
			}
		}else if(serie.type == 'pie'){
			if(dataset.source.length >= 5){
				option.type = 'scroll'
				option.legend.selected = {}
				// option.legend.data = []
				for(let j in dataset.dimensions){
					if(dataset.dimensions[j] == serie.encode.itemName){
						for(let k in dataset.source){
							option.legend.selected[dataset.source[k][j]] = k < 5 ? true : false
						}
						break
					}
				}
			}
		}
	}
}


Vue.component("sheet",{
	template : '<div class="abs" style="overflow:auto;padding:15px">'+
					'<table class="sheet" border="" cellspacing="" cellpadding="">'+
						'<caption v-html="title"></caption>'+
						'<tr><th v-for="(key,value) in header" v-html="key">Header</th></tr>'+
					'	<tr v-for="(row,j) in data"><td v-for="(key,value) in row" v-html="key">Data</td></tr>'+
					'</table>'+
					'</div>',
	props : {
		graph : {
			type : Object,
			required : true
		},
		data : {
			type : Object,
			default : function(){return [[]]}
		},
		title : {
			type : String,
			default : "数据表"
		}
	},
	created : function(){
		// console.log(this.graph.report)
		this.$http.get(basePath+"report?id="+this.graph.report.id).then(function(res){
			this.graph.report = res.body.data
		})
		this.header = []
	},
	methods : {
		setOption : function(option){
			this.data = option.source
			let columns = this.graph.report.columns
			this.header = this.header.filter(elem=>{return false})
			for(let i in option.dimensions){
				for(let j in columns){
					if(option.dimensions[i] == columns[j].name){
						this.header.push(columns[j].chinese ? columns[j].chinese : columns[j].name)
					}
				}
			}
		},
	},
	mounted : function(){
		this.$emit("mounted",this)
		this.$on("parentEvent",function(param){
			var trs = this.$el.querySelectorAll("table tr")
			let size = trs.length
			let height = $(trs[0]).height()
			switch(param.type){
				case 'datazoom':
				// let height = $(this.$el).height()
				$(this.$el).scrollTop(size * height * param.start / 100)
				break;
				case 'mouseover':
				let h = 0
				for(let i = 0 ; i <= param.dataIndex; i++){
					h += height;
				}
				$(this.$el).scrollTop(h)
				$(trs[param.dataIndex+1]).css("background-color","rgb(122,200,109)")
				$($(trs[param.dataIndex+1])[param.seriesIndex+1]).css("color","rgb(109,105,67)")
				break;
				case 'mouseout':
				$(trs[param.dataIndex+1]).css("background-color","")
				$($(trs[param.dataIndex+1])[param.seriesIndex+1]).css("color","")
			}
		})
	}
})

Vue.component("paragraph",{
	template : "<div class='abs' v-html='graph.option.content' style='padding:15px 20px'><div>",
	props : {
		graph:{
			type :Object,
			required : true
		}
	},
	created : function(){
		this.graph.option = JSON.parse(this.graph.options)
	}
})

function pie(myOption,newOption){
	let s1 = myOption.series
	let s2 = newOption.series
	let serieIndexs = []
	for(let i = 0; i < s1.length; i++){
		for(let j = 0; j < s2.length; j++){
			if(s1[i].columnId == s2[j].columnId){
				serieIndexs.push(j)
			}
		}
	}
	let source = newOption.dataset.source
	let a = source.shift()
	a.shift()
	let array = [a]
	for(let m = 0 ; m < serieIndexs.length; m++){
		source[m].shift()
		array.push(source[m])
	}
	return array
}


Vue.component("position",{
	template : '<div><div class="form-group form-group-sm">'+
					'<label class="control-label col-sm-2">{{label}}</label>'+
					'<div :class="width" class="col-sm-5">'+
						'<div class="input-group">'+
							'<span class="input-group-addon bg-success">左边</span>'+
							'<input class="form-control" type="number" v-model="obj.left" id="obj.left" placeholder="组件距容器左边的距离" />'+
							'<span class="input-group-addon">px</span>'+
						'</div>'+
					'</div>'+
					'<div :class="width" class="col-sm-5">'+
						'<div class="input-group">'+
							'<span class="input-group-addon bg-success">右边</span>'+
							'<input class="form-control" type="number" v-model="obj.right" id="obj.right" placeholder="组件距容器右边的距离" />'+
							'<span class="input-group-addon">px</span>'+
						'</div>'+
					'</div>'+
				'</div>'+
				'<div class="form-group form-group-sm">'+
					'<div :class="width+ \' col-sm-offset-2\'" class="col-sm-5 col-sm-offset-2">'+
						'<div class="input-group">'+
							'<span class="input-group-addon bg-success">顶部</span>'+
							'<input class="form-control" type="number" v-model="obj.top" id="obj.top" placeholder="组件距容器顶部的距离" />'+
							'<span class="input-group-addon">px</span>'+
						'</div>'+
					'</div>'+
					'<div :class="width" class="col-sm-5">'+
						'<div class="input-group">'+
							'<span class="input-group-addon bg-success">底部</span>'+
							'<input class="form-control" type="number" v-model="obj.bottom" id="obj.bottom" placeholder="组件距容器底部的距离" />'+
							'<span class="input-group-addon">px</span>'+
						'</div>'+
					'</div>'+
				'</div></div>',
	props : {
		obj : {
			type : Object,
			required : true
		},
		label : {
			type : String,
			default : "位置"
		},
		width : {
			type :String,
			default : "col-sm-5"
		}
	}
})

Vue.component('bar', {
	template: '<div :class="cls" :id="id"><div class="abs echart"></div></div>',
	props: {
		graph: {
			type: Object,
			required: true
		},
		id: {
			type: String,
			required: true
		},
		"cls": {
			type: String,
			default: ''
		}
	},
	data: {
		option: {}
	},
	created: function() {
		this.option = JSON.parse(this.graph.options)
	},
	mounted: function() {
		console.log(this.option)
		this.echart = echarts.init(this.$el.querySelector(".echart")) 
		this.echart.setOption(this.option)
		this.echart.showLoading() 
		this.$emit("draw",this.echart)
	},
	methods: {
		
	}
})

