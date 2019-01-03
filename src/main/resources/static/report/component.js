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
	template: '<div :class="cls" :id="id">' +
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
		if(this.graph.columns && this.graph.columns.length){
			this.drillList = new Array()	// 已经钻取了的维度
			this.unDrillList = new Array()	// 还未钻取的维度
			this.wheres = new Array()		// 过滤条件
			for(let i in this.graph.categoryColumns){
				this.unDrillList.push(this.graph.categoryColumns[i])
			}
		}
		let column = this.unDrillList.shift()
		if(column){
			this.request(column,this.wheres)
			this.drillList.push(column)
		}
	},
	mounted: function() {
		this.echart = echarts.init(this.$el)
		this.$emit("draw",this.echart)
		let drill = false,param = null
		this.echart.on('mouseover',(args)=>{
			drill = true
			param = args
		})
		this.echart.on('mouseout',(args)=>{
			drill = false
		})
		let that = this
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
						return that.drillList.length > 1
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
						return that.unDrillList.length > 0
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
	},
	methods : {
		test : function(){
			alert("test")
		},
		request : function(category,wheres){
			this.$http.post(basePath + "graph/drill",tile({
				graphId : this.graph.id,
				columnId : category.id,
				wheres : wheres
			})).then(function(res){
				if(res.body.code == 1){
					this.echart.setOption(res.body.data)
					this.$emit("data",res.body.data)
				}
				this.$emit("requestFinish",res.body)
			},function(error){
				layer.msg("网络异常")
			})
		},
		drillDown : function(param){ // 下钻
			console.log(param)
			let curCategory = this.drillList.pop()	// 取已经钻取的最后一个
			this.drillList.push(curCategory)
			curCategory.value = param.name
			this.wheres.push(curCategory.name)
			this.wheres.push(param.name)
			// this.request()
			showDialog(this.unDrillList,function(id){drill(id)})
			// this.$on("drill",)
			let that = this
			function drill(id){
				let column = {}
				for(let i in that.unDrillList){
					if(that.unDrillList[i].id == id){
						column = that.unDrillList[i]
						break
					}
				}
				that.echart.clear()
				that.request(column,that.wheres)
				that.unDrillList = that.unDrillList.filter(elem=>{return elem.id != column.id})
				that.drillList.push(column)
			}
		},
		drillUp : function(param){
			this.wheres.pop()
			this.wheres.pop()
			let column = this.drillList.pop()
			this.request(column,this.wheres)
			this.unDrillList.push(column)
		},
		rotate : function(){
			showDialog(this.graph.categoryColumns,(id)=>{
				this.wheres = []
				for(let i in this.graph.categoryColumns){
					if(this.graph.categoryColumns[i].id == id){
						let x = this.graph.categoryColumns[i]
						this.request(x,this.wheres)
						this.unDrillList = this.graph.categoryColumns.filter(elem=>{return x.id != elem.id})
						this.drillList = [x]
						break
					}
				}
			})
		},
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

Vue.component("sheet",{
	template : '<div class="abs">'+
					'<table class="sheet" border="" cellspacing="" cellpadding="">'+
						'<caption v-html="title"></caption>'+
						'<tr><th v-for="item in data[0]" v-html="item">Header</th></tr>'+
					'	<tr v-for="(row,j) in data" v-if="j > 0"><td v-for="item in row" v-html="item">Data</td></tr>'+
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
	created : function(){},
	methods : {
		setSource : function(source){
			this.data = source
		}
	},
	mounted : function(){
		this.$emit("mounted",this)
	}
})
