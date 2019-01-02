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


Vue.component("echart", {
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
		if(this.graph.columns && this.graph.columns.length)
		this.$http.post(basePath + "graph/drill", tile({
			graphId: this.graph.id,
			columnId: this.graph.categoryColumns[0].id,
			wheres: []
		})).then(function(res) {
			if(res.body.code == 1)
			this.echart.setOption(res.body.data)
		})
	},
	mounted: function() {
		this.echart = echarts.init(this.$el)
		this.$emit("draw",this.echart)
		let param = {drillDown:true,drillUp:true,rotate:true}
		this.echart.on('mousedown', (args) => {
			param = args
		})
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
						alert(JSON.stringify(row))
					},
					isShown: function(row) {
						if(row.drillUp)
							row.drillUp = !row.drillUp
						return row.drillUp
					}
				},
				"drillDown": {
					name: "下钻",
					onClick: function(row) {
						console.log(row)
					},
					isShown: function(row) {
						if(row.drillDown)
							row.drillDown = !row.drillDown
						return row.drillDown
					}
				},
				"rotate": {
					name: "旋转",
					onClick: function(row) {

					},
					isShown: function(row) {
						if(row.rotate)
							row.rotate = !row.rotate
						return row.rotate
					}
				}
			}
		})
	}
})
