
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
			},
			graphTypes : graphTypes,
		},
		created : function(){
			this.graph = JSON.parse(localStorage.graph)
			this.report = this.graph.report
			// 请求图表信息
			this.$http.get(basePath+"graph/"+this.graph.id).then(function(res){
				this.setGraph(res.body.data)
			})
			// 请求获取当前图表所属数据包下包含的数据表
			this.$http.get(basePath+"pkg/tab/"+this.report.pkg.id).then(function(res){
				this.$set(this,'tables',res.body.data)
			})
			// loadTree(this.graph.id)
			function loadTree(reportId){
				Vue.http.get(basePath+"graph/tree/"+reportId).then(function(res){
					setTimeout(()=>{
						let data = res.body.data
						$("#tree").treeview({
							data:[data],
							onNodeSelected : nodeSelected
						})
					},100)
				})
			}
			var that = this
			function nodeSelected(event,node){
				// 添加
				Vue.http.get(basePath+"graph/"+that.graph.id+"/"+node.id).then(function(res){
					if(res.body.code == 1){
						Vue.set(that.graph,"columns",res.body.data.columns)
					}else{
						layer.msg(res.body.message)
					}
				})
			}
		},
		methods : {
			setGraph: function(graph){
				// 图表基本数据
				this.graph = graph
				if(graph.options){
					this.setting = JSON.parse(this.graph.options)
				}
				// 分类
				if(graph.columns.length){
					this.$set(this.setting,'category',this.graph.columns[0].id)
				}
				// 报表
				this.report = graph.report
				
			},
			add : function(table,column){ // 为图表添加字段
				this.$http.get(basePath+"graph/"+this.graph.id+"/"+column.id).then(function(res){
					if(res.body.code == 1){
						table.columns = table.columns.filter(c=>{return c != column}) 
						this.setGraph(res.body.data)
					}else{
						layer.msg(res.body.message)
					}
				})
			},    
			save : function(){ // 保存数据
				this.graph.options = JSON.stringify(this.setting)
				layer.confirm("您确定要提交吗？",{
					btn:["确定","取消"]
				},(index,layero)=>{
					layer.close(index)
					this.$http.post(basePath+"graph",tile(this.graph)).then(res=>{
						if(res.body.code != 1){
							layer.msg(res.body.message)
						}else{
							var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
							parent.layer.close(index); //再执行关闭        
						}
					})
				})
			},
			chooseColumn : function(signal,type,event){
				event.stopPropagation()
				layer.open({
					type : 1,
					title : false,
					shade : false,
					content : $("#tree-div"),
					area : ["500px","350px"],
					btn : ["确定","取消"],
					success : ()=>{
						Vue.http.get(basePath+"graph/tree/"+this.graph.id).then(function(res){
							res = res.body
							for(let i in res.data.nodes){
								let table = res.data.nodes[i]
								for(let j in table.nodes){
									let column = table.nodes[j]
									if(type && column.type && column.type !=  type){
										column.state = {disabled : true}
									}
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
									alert(node.id)
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
						vue.$emit("save",{data:arrays,field:signal})
						// console.log(arrays)
					},
					cancel :function(){
						
					}
				})
				// console.log()
			},
			rmColumn : function(field,column,event){
				this.graph[field] = this.graph[field].filter(e=>{return e.id != column.id})
			},
			rmseries : function(serie){
				this.graph.valueColumns = this.graph.valueColumns.filter(e=>{
					return e.id != serie.columnId
				})
				this.setting.series = this.setting.series.filter(elem=>{
					return elem.columnId != serie.columnId
				})
			}
		},
		watch : {
			"setting.xAxis" : function(newVal,oldVal){
			},
			"setting.yAxis" : function(newVal,oldVal){
			},
			"setting.tooltip" : function(newVal,oldVal){
			},
			"setting.legend" : function(newVal,oldVal){
			},
			"graph.title" : function(newVal,oldVal){
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
							name : array[i].chinese ? array[i].chinese:array[i].name
						})
					}
				}
			},
		},
		mounted : function(){
			// var tree = document.querySelector("#tree")
			// console.log(this.$el)
			this.$on("save",function(data){
				alert(JSON.stringify(data))
				for(let i in data.data)
				this.graph[data.field].push({id:data.data[i].id})
				this.$http.post(basePath+"graph",tile(this.graph)).then(function(res){
					if(res.body.code == 1){
						this.$set(this.graph,data.field,res.body.data[data.field])
					}else{
						layer.msg(res.body.message)
					}
				})
			})
		},
	})
	