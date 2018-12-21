
	var report = JSON.parse(localStorage['report'])
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
	var vue = new Vue({
		el : "#app",
		data : {
			report : report,
			tables : [],
			treeNodes : [],
			graph : {},
			setting : {
				xAxis : {},
				yAxis : {},
				tooltip : {},
				legend : {},
				series : []
			},
			graphTypes : graphTypes,
			drilling : []
		},
		created : function(){
			this.graph = JSON.parse(localStorage.graph)
			this.$http.get(basePath+"graph/"+this.graph.id).then(function(res){
				this.setGraph(res.body.data)
			})
			this.$http.get(basePath+"pkg/tab/"+report.pkg.id).then(function(res){
				this.$set(this,'tables',res.body.data)
			})
		},
		methods : {
			setGraph: function(graph){
				// 图表基本数据
				this.graph = graph
				this.setting = JSON.parse(this.graph.options)
				// 分类
				this.$set(this.setting,'category',this.graph.columns[0].id)
				// 报表
				this.report = graph.report
				// 处理维度
				if(this.graph.dimension){
					let split = this.graph.dimension.split(';')
					for(let i in split){
						for(let j in this.graph.columns){
							if(split[i] === this.graph.columns[j].id){
								this.drilling.push(this.graph.columns[j])
							}
						}
					}
				}
			},
			getFiledCount : function(){
				return this.graph.columns.length - this.setting.series.length - (this.setting.category ? 1 : 0) - this.drilling.length
			},
			getFields : function(table){// 获取数据表table下的所有字段
				if(!table.columns){
					this.$http.get(basePath+"vt/vc/"+table.id).then(function(res){
						this.$set(table,'columns',this.beside(res.body.data))
					})
				}
				if(table.open == undefined){
					this.$set(table,'open',false)
				}
				table.open = ! table.open;
			},
			getColumns : function(){ // 获取当前可用的字段：移除作为分类的字段，作为系列的字段和钻取的字段
				let array = new Array();
				for(let i in this.graph.columns){
					let column = this.graph.columns[i]
					if(column.id != this.setting.category){
						let arr = this.setting.series.filter(elem=>{
							return elem.columnId === column.id
						})
						if(!arr.length && !this.drilling.includes(column)){
							array.push(column)
						}
					}
				}
				return array
			},
			setCategory : function(e){
				let column = this.graph.columns[e.target.selectedIndex]
				this.graph.columns.splice(e.target.selectedIndex,1)
				this.graph.columns.unshift(column)
			},
			addseries : function(column){// 添加系列
				this.setting.series.push({type:'bar',name:column.chinese?column.chinese:column.name,columnId:column.id,"seriesLayoutBy":"row"})
			},
			rmseries : function(seria){ // 删除系列
				this.setting.series = this.setting.series.filter(elem=>{return seria != elem})
			},
			dragStart : function(e){ 
				console.log(e)
				e.preventDefault()
			},
			allowDrop : function(e){
				e.preventDefault()
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
			remove : function(column,e){// 移除图表中的字段
				this.graph.columns = this.graph.columns.filter(elem=>{return elem != column})
				this.$http.delete(basePath+"graph/"+this.graph.id+"/"+column.id).then(function(res){
					if(res.body.code == 1){
						this.setGraph(res.body.data)
						this.$http.get(basePath+"vt/vc/"+column.table.id).then(function(res){
							let table = this.tables.find(elem=>{return elem.id === column.table.id})
							this.$set(table,'columns',this.beside(res.body.data))
						})
					}
				})
				e.preventDefault()
			},
			check : function(field,value,event){
				if(event.target.checked){
					this.$set(this.setting,field,value)
				}else{
					this.$set(this.setting,field,undefined)
				}
				event.stopPropagation()
			},
			beside : function(columns){
				var array = new Array();
				for(var i in columns){
					var f = true;
					for(var j in this.graph.columns){
						if(columns[i].id === this.graph.columns[j].id){
							f = false
							break
						}
					}
					if(f)
						array.push(columns[i])
				}
				return array
			},
			addDrilling : function(column){// 添加系列
				this.drilling.push(column)
			},
			rmDrilling : function(c){// 移除系列
				this.drilling = this.drilling.filter(elem=>{return c != elem})
			},
			save : function(){ // 保存数据
				this.graph.options = JSON.stringify(this.setting)
				this.graph.dimension = "";
				for(let i in this.drilling){
					this.graph.dimension += this.drilling[i].id+";"
				}
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
			}
		}
	})
	