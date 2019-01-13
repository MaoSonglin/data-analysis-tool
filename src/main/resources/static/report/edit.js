		Vue.http.options.withCredentials=true
		Vue.http.options.emulateJSON=true
		let reporId = getParameter("reportId")
		let app = new Vue({
			el : "#app",
			data : {
				graphs : [],
				echarts :[],
				edit : getParameter("edit"),
				components : []
			},
			created : function(){
				this.$http.get(basePath+"report/graphs/"+reporId).then(function(res){
					this.$set(this,"graphs",res.body.data)
				})
			},
			methods : {
				drill : function(e){
					// alert(JSON.stringify(e))
					for(let i in this.components){
						let c = this.components[i]
						if(c.drill) c.drill(e) 
					}
				},
				test : function(child){
					this.components.push(child)
				},
				dataChange : function(graph,data){
					// console.log(rotate(data.dataset.source))
					for(let i in this.components){
						let c = this.components[i]
						if(c.graph.parent && c.graph.parent == graph.id){
							if(c.setOption) c.setOption(data)
						}
					}
				},
				position : function(graph){ // 定位
					return "position:absolute;top:"+graph.top+"%;left:"+graph.left+"%;width:"+graph.width+"%;height:"+graph.height+"%;"
				},
				repos : function(graph){ // 重新设置定位
					this.save(graph)
				},
				save : function(graph){ // 保存
					delete graph.option
					this.$http.post(basePath+"/graph",tile(graph)).then(function(res){
						layer.msg(res.body.message)
					})
				},
				echartClick : function(graph,param){
					for(let i in this.components){
						let c = this.components[i]
// 						if(c.graph.parent && c.graph.parent == graph.id){
// 							// if(c.setOption) c.setOption(data)
// 						}
							c.$emit("parentEvent",param)
					}
				},
				del : function(e){ // 删除
					layer.confirm("您确定要删除该图表吗？",{
						title : "确认",
						btn : ["确定","取消"]
					},(index,layero)=>{
						layer.close(index)
						this.$http.delete(basePath+"/graph/"+e.id).then(function(res){
							layer.msg(res.body.message)
							if(res.body.code == 1){
								this.graphs = this.graphs.filter(g => {return g.id != e.id})
							}
						})
					})
				},
				afterDraw : function(echart){ // 
					this.echarts.push(echart)
					window.onresize = ()=>{
						for(let i in this.echarts){
							this.echarts[i].resize()
						}
					}
				},
				setting : function(graph){
					let url = 'setting.1.html?reportId='+graph.report.id+'&graphId='+graph.id
					if(graph.type == 'graph'){
					}else if(graph.type == 'child-graph'){
						// url = 'setting.2.html?reportId='+graph.report.id+'&graphId='+graph.id
					}else if(graph.type == 'table'){
						url = 'table.html?reportId='+graph.report.id+'&graphId='+graph.id 
					}else if(graph.type == 'paragraph'){
						url = 'paragraph.html?reportId='+graph.report.id+'&graphId='+graph.id
					}else{
						return
					}
					if(window.parent && window.parent.addParentTab){
						window.parent.addParentTab({title:"编辑图表"+graph.title,href:"report/"+url})
					}else{
						window.open(url, "_blank");
					}
				},
				createMenu : function(){
					let url = location.href
					let that = this
					if(/.*edit\.html.*/.test(url))
					new BootstrapMenu('#app',{
						actions : [{
							name : "新建",
							onClick : function(){
								layer.open({
									title : "新建图表",
									content : $("#dialog"),
									type : 1,
									area : ["600px","350px"],
									btn : ["确定","取消"],
									end : function(){
										$("#dialog").hide()
									},
									btn1 : function(index,layero){
										$("#addform").validate(validator)
										if($("#addform").valid()){
											layer.close(index)
											dialog.$emit("save")
										}
									},
									success : ()=>{
										dialog.$emit("setGraph",{report : {id : reporId},width : 50,height : 50,left : 10,top:10,type:'graph'}),
										Vue.set(dialog,"graphs",that.graphs)
									}
								})
							},
						}]
					})
				}
			},
			mounted : function(){
				this.createMenu()
			}
		})
		
		let dialog = new Vue({
			el : "#dialog",
			data : {
				graph : {report:{id:reporId}},
				graphs : []
			},
			created : function(){
				this.$on("save",function(){
					this.$http.post(basePath+"/graph",tile(this.graph)).then(function(res){
						if(res.body.code == 1){
							if(! app.graphs){
								Vue.set(app,"graphs",[])
							}
							app.graphs.push(res.body.data)
						}
						layer.msg(res.body.message)
					})
				})
				this.$on("setGraph",function(g){
					this.graph = g
				})
			}
		})
			
		let validator = {
			rules : {
				title : {required : true,maxlength:50},
				top : {required:true,number:true,range:[0,100]},
				left : {required:true,number:true,range:[0,100]},
				width : {required:true,number:true,range:[0,100]},
				height : {required:true,number:true,range:[0,100]}
			},
			messages:{
				title : {required:"请输入标题",maxlength:"标题不能操作50个字符"},
				top : {required:"请输入y坐标",number:"坐标只能是数值类型",range:"坐标只能在0~100之前"},
				left : {required:"请输入x坐标",number:"坐标只能是数值类型",range:"坐标只能在0~100之前"},
				width : {required:"请输入宽度",number:"只能是数值类型",range:"只能在0~100之前"},
				height : {required:"请输入高度",number:"只能是数值类型",range:"只能在0~100之前"},
			},
			errorPlacement : function(error,element){ 
				layer.tips(error.html(),element[0],{
					tips : 1,
					tipsMore: true
				})
			}
		}
		