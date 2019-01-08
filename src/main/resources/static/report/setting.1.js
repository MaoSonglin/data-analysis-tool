
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
	feature = {
		saveAsImage : {
			show : true
		},
		restore: { //重置
		    show: true
		},
		dataView : {
			show : true
		},
		dataZoom: { //数据缩放视图
		    show: true
		},
		magicType: {//动态类型切换
			show : true,
		    type: ['bar', 'line']
		},
		brush : {
			show : true,
			type : ['rect','polygon','lineX','lineY','keep','clear']
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
				toolbox : {show : false},
				dataZoom : {show : false},
				series : [],
				categorys : []
			},
			graphTypes : graphTypes,
			graphs : []
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
						if(res.body.code != 1){
						}else{
							var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
							parent.layer.close(index); //再执行关闭        
						}
					})
				})
			},
			chooseColumn : function(event){
				event.stopPropagation()
				layer.open({
					type : 1,
					title : false,
					shade : false,
					content : $("#tree-div"),
					area : ["500px","350px"],
					btn : ["确定","取消"],
					success : ()=>{
						Vue.http.get(basePath+"report/tree/"+this.report.id).then(function(res){
							res = res.body
							for(let i in res.data.nodes){
								let table = res.data.nodes[i]
								for(let j in table.nodes){
									let column = table.nodes[j]
									column.selectedIcon = "glyphicon glyphicon-ok"
									column.icon = "glyphicon glyphicon-unchecked"
								}
								table.selectable = false
								table.showCheckbox = false
							}
							res.data.showCheckbox = false
							res.data.selectable = false
							$("#tree").treeview({
								data:[res.data],
								multiSelect : true,
								showCheckbox : false,
								onNodeSelected : function(event,node){
									// alert(JSON.stringify(node))
									// vue.$emit(signal,node.id)
									// alert(node.id)
								}
							})
						})
					},
					end : function(){
						$("#tree-div").hide()
						vue.$emit("clear")
					},
					btn1 : function(index,layero){
						layer.close(index)
						// vue.$emit("save")
						let arrays = $("#tree").treeview(true).getSelected()
						vue.$emit("save",arrays)
						// console.log(arrays)
					},
					cancel :function(){
						
					}
				})
				// console.log()
			},
			rm : function(column,event){
				// 移除字段
				this.report["columns"] = this.report["columns"].filter(e=>{return e.id != column.id})
				// 移除系列
				this.setting.series = this.setting.series.filter(e=>{return e.columnId != column.id})
				// 移除分类
				try{
					this.setting.categorys = this.setting.categorys.filter(e=>{e.columnId != column.id})
				}catch(e){
					console.log(e)
				}
				saveObj(basePath+"report" , this.report,"post")
			},
			rmseries : function(serie){
				this.setting.series = this.setting.series.filter(elem=>{
					return elem.columnId != serie.columnId
				})
			},
			addseries : function(column){
				this.setting.series.push({type : 'bar',name:column.chinese,columnId:column.id,seriesLayoutBy:"column"})
			},
			rmColumn : function(column){
				this.setting.categorys = this.setting.categorys.filter(e=>{return e.id != column.id})
			},
			config : function(index){
				sessionStorage.report = JSON.stringify(this.report)
				sessionStorage.graph = JSON.stringify(this.graph)
				sessionStorage.serie = JSON.stringify(this.setting.series[index])
				var that = this
				let layerIndex = layer.open({
					title : "系列配置",
					type : 2,
					area : ["768px","500px"],
					content : 'graph/'+this.setting.series[index].type+"/"+this.setting.series[index].type+".html",
					btn : ["确定","取消"],
					btn1 : function(i,layero){
						layer.close(i)
						that.setting.series[index] = JSON.parse(sessionStorage.serie)
						console.log(that.setting.series)
					}
				})
				// layer.full(layerIndex)
			}
		},
		watch : {
			"setting.xAxis" : function(newVal,oldVal){
			},
			"setting.yAxis" : function(newVal,oldVal){
			},
			"setting.tooltip.show" : function(newVal,oldVal){
				if(newVal){
					this.setting.tooltip.trigger = 'axis'
					this.setting.tooltip.axisPointer =  {
							type: 'shadow',
							label: {
								show: true
							}
						}
				}
			},
			"setting.legend" : function(newVal,oldVal){
			},
			"setting.title.text" : function(newVal,oldVal){
				this.graph.title = newVal
			},
			"setting.toolbox.show" : function(newVal,oldVal){
				if(newVal && ! this.setting.toolbox.feature)
					this.setting.toolbox.feature = feature
				else{
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
			"setting.legend.type" : function(newVal,oldVal){
			}
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
	