
	// var report = JSON.parse(localStorage['report'])
	Vue.http.options.withCredentials=true
	Vue.http.options.emulateJSON=true
	var graphTypes = [
		{
			type : 'bar',
			name : '条形图'
		},
		{
			type : 'line',
			name : '折线图'
		},
		{
			type : 'pie',
			name : '饼状图'
		}
	]
	var toolbox = {
					show : false,
					feature : {
						restore: { //重置
						    show: true
						},
						dataZoom: { //数据缩放视图
						    show: true
						},
						magicType: {//动态类型切换
						    type: ['bar', 'line']
						}
					}
				}
	var vue = new Vue({
		el : "#app",
		data : {
			report : {},
			treeNodes : [],
			graph : {},
			setting : {
				title : {show : true, "text" : ""},
				xAxis : {show : true},
				yAxis : {show : true},
				tooltip : {show : true},
				legend : {show : true},
				grid : {show : false},
				toolbox : toolbox,
				dataZoom : {show : false},
				series : [],
				categorys : []
			},
			graphTypes : graphTypes,
			graphs : [],
			master : {}
		},
		created : function(){
			this.$http.get(basePath+"report?id="+getParameter("reportId")).then(function(res){
				this.$set(this,"report",res.body.data)
			})
			// 请求图表信息
			this.$http.get(basePath+"graph/"+getParameter("graphId")).then(function(res){
				this.$set(this,"graph",res.body.data)
				if(this.graph.options){
					this.$set(this,"setting",JSON.parse(this.graph.options))
				}
			})
			this.$http.get(basePath+"graph/athers?id="+getParameter("graphId")).then(function(res){
				this.graphs = res.body.data
				for(let i in this.graphs){
					if(this.graphs[i].id == this.graph.parent){
						this.master = this.graphs[i]
					}
				}
			})
		},
		methods : {
			setGraph: function(graph){
			},
			getColumns : function(type){
				if(this.report.columns)
				return this.report.columns.filter(elem=>{
					if(type && type != elem.typeName){
						return false
					}
					for(let i in this.setting.categorys){
						if(this.setting.categorys[i].id == elem.id){
							return false
						}
					}
					for(let i in this.setting.series){
						if(this.setting.series[i].columnId == elem.id){
							return false
						}
					}
					return true
				})
			},
			add : function(column){ // 为图表添加字段
				this.setting.categorys.push(column)
			},    
			save : function(){ // 保存数据
				this.graph.options = JSON.stringify(this.setting)
				layer.confirm("您确定要提交吗？",{
					btn:["确定","取消"]
				},(index,layero)=>{
					layer.close(index)
					delete this.graph.option
					this.$http.post(basePath+"graph",tile(this.graph)).then(res=>{
						layer.msg(res.body.message)
					})
				})
			},
			rmseries : function(serie){
				this.setting.series = this.setting.series.filter(elem=>{
					return elem.columnId != serie.columnId
				})
			},
			addseries : function(column,index){
				this.setting.series.push({
					type : column.type,
					name:column.name,
					columnId:column.columnId,
					seriesLayoutBy:column.seriesLayoutBy,
					parentSeriesIndex : index
				})
			},
			isAdd : function(serie){
				let x = this.setting.series.filter(elem=>{return elem.columnId != serie.columnId})
				for(let i in this.setting.series){
					if(this.setting.series[i].columnId == serie.columnId){
						return false
					}
				}
				return true
			}
		},
		watch : {
			"setting.xAxis" : function(newVal,oldVal){
			},
			"setting.yAxis" : function(newVal,oldVal){
			},
			"setting.tooltip" : function(newVal,oldVal){
			},
			"graph.parent" : function(newVal,oldVal){
				for(let i in this.graphs){
					if(this.graphs[i].id == newVal){
						this.master = this.graphs[i]
					}
				}
			},
			"setting.title.text" : function(newVal,oldVal){
				this.graph.title = newVal
			},
			"setting.toolbox.show" : function(newVal,oldVal){
				if(!newVal)
					this.setting.toolbox = {show : false}
				else{
					this.setting.toolbox.feature = toolbox.feature
				}
			},
			"setting.dataZoom.show" : function(newVal,oldVal){
				if(!newVal)
					this.setting.dataZoom = {show : false}
			},
			"graph.valueColumns" : function(newVal,oldVal){
				let array = newVal.filter(elem=>{
					if(! elem.name){
						return false
					}
					for(let i in this.setting.series){
						if(this.setting.series[i].columnId == elem.id){
							return false
						}
					}
					return true
				})
				if(array.length){
					for(let i in array){
						this.setting.series.push({
							columnId : array[i].id,
							type : "bar",
							name : array[i].chinese ? array[i].chinese:array[i].name,
							seriesLayoutBy : "row"
						})
					}
				}
			},
		},
		mounted : function(){
			// var tree = document.querySelector("#tree")
			// console.log(this.$el)
			this.$on("save",function(data){
				for(let i in data)
				this.report.columns.push({id:data[i].id})
				this.$http.post(basePath+"report",tile(this.report)).then(function(res){
					if(res.body.code == 1){
						this.$set(this.report,"columns",res.body.data.columns)
					}else{
						layer.msg(res.body.message)
					}
				})
			})
		},
	})
	
	function saveObj(url,obj,method,callback){
		$.ajax({
			url : url,
			method : method ? method : "post",
			data : tile(obj),
			xhrFields : {
				withCredentials : true
			},
			success : function(res){
				if(callback)callback(res)
			}
		})
	}
	