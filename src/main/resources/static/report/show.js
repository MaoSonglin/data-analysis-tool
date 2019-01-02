	Vue.http.options.emulateJSON=true
	Vue.http.options.withCredentials=true
	var reportid = getParameter("id")
	var vue = new Vue({
		el:"#wrapper",
		data:{
			report:{},		// 当前的报表
			graphs : [],	// 所有的图表集合
			curGraph : {} ,// 当前选中的图表
			wheres : [],
			drilledColumn : [],// 已经钻取了的维度
			rotate : null
		},
		created : function (){
			// 获取报表信息
			this.$http.get(basePath+"report?id="+reportid).then(function(res){
				if(res.body.code == 0){
					layer.msg(res.body.message)
				}else{
					this.report = res.body.data
				}
			},function(error){
				layer.msg("网络错误")
			})
			// 获取报表中的图表信息
			this.$http.get(basePath+"report/graphs/"+reportid).then(function(res){
				if(res.body.code == 1){
					this.$set(this,'graphs',res.body.data)
				}
				else{
					layer.msg(res.body.message)
				}
			},function(error){
				layer.msg("网络错误，获取图表信息失败")
			})
			
			this.$on("request",()=>{
				
				// 处理钻取树
				/****************************************************************************/
				let dom = this.$el.querySelector("#tree")
				let array = this.getDrilled(this.curGraph)
				let nodes = []
				for(let i in array){
					let text = array[i].chinese ? array[i].chinese:array[i].name 
					// text = array[i].value ? text + " ("+array[i].value+")" : text
					nodes.push({
						text : text,
						id : array[i].id,
						nodeId : array[i].id,
						tags : [array[i].value ? array[i].value:''],
						showTags:true 
					})
				}
				$(dom).empty()
				if(nodes.length > 1){
					// 显示钻取树
					$(dom).treeview({data : [{text:"钻取过程",nodes : nodes,showTags:true ,showBorder:true}]}),
					$(dom).treeview(true).expandAll({})
					$(dom).on("nodeSelected  ",(event,node)=>{// 节点点击事件
						if(node.id){
							let tmp = this.curGraph.categoryColumn
							while(tmp){
								if(tmp.id == node.id){
									this.curGraph.categoryColumn = tmp
									this.drillRequest()
									break;
								}
								let p = tmp.previous
								if(p){
									p.next = undefined
								}
								tmp.previous = undefined
								tmp = p
							}
						}
					})
				}
				/****************************************************************************/
				
				// 处理
				
			})
		},
		methods : {
			getPosition : function(graph){
				var style = "position:absolute;top:"+0+"px;right:"+30+"%;bottom:"+40+"%;left:"+0+"px";
				return style
			},
			getDrilling : function(){ // 获取可钻取的条件
				var array = new Array();
				if(this.curGraph.dimension){
					var arrays = this.curGraph.dimension.split(';')
					let drilledColumns = this.getDrilled(this.curGraph)
					for(var i in this.curGraph.columns){
						for(var j in arrays){
							if(this.curGraph.columns[i].id == arrays[j] && ! drilledColumns.includes(this.curGraph.columns[i])){
								array.push(this.curGraph.columns[i])
								break
							}
						}
					}
				}
				return array
			},
			getColumns : function(type){
				let array = new Array()
				for(let i in this.curGraph.columns){
					if(this.curGraph.columns[i].typeName = type){
						array.push(this.curGraph.columns[i])
					}
				}
			},
			getDrilled : function(graph){// 获取已经钻取过得条件
				if(!graph) return []
				let array = new Array()
				let column = graph.categoryColumn
				while(column){
					array.unshift(column)
					column = column.previous
				}
				console.log(array)
				return array
			},
			drillDown : function(column){ // 下钻
				
				this.curGraph.categoryColumn.value = this.clickedData[0]
			
				this.curGraph.categoryColumn.next = column
				column.previous = this.curGraph.categoryColumn
				this.curGraph.categoryColumn = column
				
				/* this.wheres.push(this.curGraph.categoryColumn.previous.name)
				this.wheres.push(this.clickedData[0]) */
				
				this.drillRequest()
				$('#myModal').modal('toggle')
			},
			drillUp : function(graph){ // 向上钻取
				if(graph.categoryColumn.previous){ // 如果当前分类字段存在前驱
					let temp = graph.categoryColumn
					graph.categoryColumn = graph.categoryColumn.previous
					graph.categoryColumn.next = temp.previous = undefined
					delete graph.categoryColumn.value 
					
					this.drillRequest()
				}
				$('#myModal').modal('hide')
			},
			drillRequest : function(){ // 请求钻取的数据
				let wheres = []
				let p = this.curGraph.categoryColumn.previous
				while(p){
					wheres.push(p.name)
					wheres.push(p.value)
					p = p.previous
				}
				this.$http.post(basePath+"graph/drill",tile({
						graphId : this.curGraph.id,
						columnId : this.curGraph.categoryColumn.id,
						wheres : wheres
					})).then(function(res){
					if(res.body.code == 1){
						this.echart.clear()
						res.body.data.toolbox = toolbox
						this.echart.setOption(res.body.data,true)
						this.$set(this.curGraph,'dataset',res.body.data.dataset.source)
						this.initTable = 0
						this.$emit("request")
					}
				})
				
			},
			draw : function(){ // 绘制图表
				for(let i in this.graphs){
					let dom = document.querySelector("#graph-"+i)
					let echart = echarts.init(dom)
					this.echart = echart
					this.curGraph = this.graphs[i]
					let options = JSON.parse(this.graphs[i].options)
					/****************************************************/
					let id = options.category
					for(let j in this.graphs[i].columns){
						if(this.graphs[i].columns[j].id == id){
							this.graphs[i].categoryColumn = this.graphs[i].columns[j]
							this.rotate = this.graphs[i].categoryColumn.id
							break
						}
					}
					this.drillRequest()
					echart.on('click',(param)=>{
						this.curGraph = this.graphs[i]
						this.clickedData = param.data
						this.echart = echart
						$('#myModal').modal('toggle')
					})
					echart.on('mouseover',(param)=>{
						let $raws = $("#data-table tr")
						let $raw = $raws.eq(param.dataIndex + 1)
						$raw.addClass("bg-success")
						$raw.find("td").eq(param.seriesIndex+1).addClass("bg-info")
						let height = 0//表格中对应数据所在偏移量
						for(let i = 0; i < param.dataIndex+1; i++){
							height += $($raws.get(i)).height()
						}
						let $scroll = $("#data-table").parent().parent()
						let scrollHeight = $scroll.innerHeight()
						if(height > scrollHeight){
							$scroll.scrollTop(Math.floor(height/scrollHeight)*scrollHeight)
						}
					})
					echart.on('mouseout',(param)=>{
						let $raw = $("#data-table tr").eq(param.dataIndex + 1)
						$raw.removeClass("bg-success")
						$raw.find("td").eq(param.seriesIndex+1).removeClass("bg-info")
					})
				}
			},
			getRow : function(graph){// 渲染表格
				if(!graph.dataset){
					return
				}
				let result = new Array()
				let m = graph.dataset.length,n = graph.dataset[0].length
				for(let j = 0; j < n; j++){
					let a = new Array()
					for(let i = 0; i < m; i++){
						a.push(graph.dataset[i][j])
					}
					result.push(a)
				}
				return result//this.graphs[index].dataset
			},
			transposition : function(graph,event){
				let id = event.target.value
				for(let i in graph.columns){
					if(graph.columns[i].id == id){
						graph.categoryColumn = graph.columns[i]
						this.drillRequest()
					}
				}
			},
			drill : function(index,e){
				this.curGraph = this.graphs[index]
				this.clickedData = new Array()
				let $tds = $(e.target.parentNode).siblings()
				for(let i = 0, length = $tds.length; i < length; i++){
					this.clickedData.push($tds.get(i).innerHTML)
				}
				$('#myModal').modal('toggle')
			}
		},
		updated:function(){
			if(!this.isDraw){
				this.draw()
				this.isDraw = true
			}
		},
		watch : {
			"rotate" : function(newVal,oldVal){// 旋转
				for(let i in this.curGraph.columns){
					if(this.curGraph.columns[i].id == newVal){
						this.curGraph.categoryColumn = this.curGraph.columns[i]
						this.drillRequest()
						$("#myModal2").modal('hide')
					}
				}
			}
		}
	})
	
	
// 	.layui-table-main > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1),
// 加入我和你妈同时掉入水里，你救谁？
// 先救你
// 那你妈呢？
// 我妈同事掉水里关我妈什么事？
// 	html body div#wrapper div.data-div.bg-warning div.layui-form.layui-border-box.layui-table-view div.layui-table-box div.layui-table-body.layui-table-main table.layui-table tbody tr