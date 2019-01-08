Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;

var pkgid = getParameter("pkgid")
var container = new Vue({
	el : "#container",
	data : {
		tables : [],	// 当前显示数据表
		page : {},		// 分页信息
		fields : [],	// 当前显示字段
		table : {},		// 当前操作（显示字段）的数据表
		pkg : {}		// 当前数据包
	},
	created : function(){
		this.$http.get(basePath+"pkg/tab/"+pkgid,{before:openLoading}).then(function(res){
			closeLoading()
			layer.msg(res.body.message)
			Vue.set(this,'tables',res.body.data)
			this.pkg = res.body.values.workPackage
			document.getElementsByTagName("title")[0].innerHTML = this.pkg.name + "中的数据表"
			if(this.tables.length) {
				this.table = this.tables[0]
				this.showFields(this.table)
			}
		},function(error){
			closeLoading()
			layer.msg("网络异常！")
		})
	},
	methods : {
		showFields : function(table,event){// 显示当前数据表中的字段信息
			this.$http.get(basePath+"vt/vc/"+table.id,{
				before : openLoading
			}).then(function(res){
				closeLoading()
				if(res.body.code == 1){
					this.$set(this,"fields",res.body.data);
					this.table = table;
				}else{
					layer.msg(res.body.message)
				}
			},function(error){
				closeLoading()
				layer.msg("网络异常")
			})
		},
		addTable:function(){// 添加数据表格
			location.href = "pkg-add-tab.html?pkg.id="+this.pkg.id;
		},
		getRelation : function(field){
			if(field.relation){
				var relation = field.relation;
				if(relation.chinese) return relation.chinese
				return relation.name;
			}
		},
		findFieldById : function(id){
			for(var i = 0; i < this.fields.length; i++){
				if(this.fields[i].id == id){
					return this.fields[i];
				}
			}
			return null;
		},
		updateIndex : function(id,event){
			openLoading()
			this.$http.get(basePath+"pkg/extractor/"+id).then(function(res){
				layer.msg(res.body.message)
				if(res.body.code == 1)
					this.pkg = res.body.data
				closeLoading()
			},closeLoading)
		},
		checkRelationShip : function(){// 查看关系图
			localStorage.pkg = JSON.stringify(this.pkg)
			let index = layer.open({
				type : 2,
				content : "relation-graph.html",
				// btn : ["确定"],
				btn1 : function(index,layero){
					layer.close(index)
				},
				success : function(index,layero){
				},
				end : function(){
					delete localStorage.pkg
				}
			})
			layer.full(index)
		}
	},
	watch : {
		"page.keywork" : function(newVal,oldVal){
			if(!this.tablesBackup){
				this.tablesBackup = this.tables
			}
			if(newVal){
				this.tables = this.tablesBackup.filter(item=>{
					if(item.chinese){
						return item.chinese.search(newVal) > -1;
					}else{
						return item.name.search(newVal)>-1 ;
					}
				})
			}else{
				this.tables = this.tablesBackup
			}
		},
		"table" : function(newVal,oldVal){
			this.fieldUpdated = true;
		}
	},
	updated : function(){
		if(this.fieldUpdated){
			// 跟新数据表格的内容
			layuiTable.init('kjix12312',{
				"url" : basePath+"vc/list",
				page : {
					limit : 5,
					limits : [5,10,15,20,25],
					curr : 1
				},
				where : {
					tableId : this.table.id
				}
			})
			this.fieldUpdated = false
		}
	}
})

var layuiTable = null;
layui.use('table',function(){
	layuiTable = layui.table
	layuiTable.init('kjix12312',{
		"url" : basePath+"vc/list",
		page : {
			limit : 5,
			limits : [5,10,15,20,25],
			curr : 1
		},
		where : {
			tableId : this.table.id
		}
	})
	layuiTable.on('edit(kjix12312)',function(obj){
		update(obj)
	})
	
	layuiTable.on("tool(kjix12312)",function(obj){
		switch(obj.event){
			case "edit":
				
			break;
			case "del":
				del(obj)
			break;
			case 'setRelation':
			var field = container.findFieldById(obj.data.id)
			localStorage.pkg = JSON.stringify(container.pkg)
			localStorage.field = JSON.stringify(field)
			layer.open({
				title : "编辑关联关系",
				type : 2,
				content : "addRelation.html",
				area : ["500px","350px"],
				btn : ["确定","取消"],
				btn1 : function(index,layero){
					layer.close(index)
					let reference = JSON.parse(localStorage.reference)
					console.log(reference)
					Vue.http.post(basePath+"ref",tile(reference)).then(function(res){
						layer.msg(res.message)
					})
				},
				success : function(){
					
				},
				end : function(){
					delete localStorage.reference
					delete localStorage.field
					delete localStorage.pkg
				}
			})
			
			break;
		}
	})
	
	/**
	 * 数据表格工具栏事件监控
	 */
	layuiTable.on("toolbar(kjix12312)",function(obj){
		switch(obj.event){
			case 'rmtab': // 从当前数据包中移除一个数据表格
			pkgRmTab()
			break;
			case 'addField':// 新建字段
			sessionStorage.tables = JSON.stringify(container.tables)
			sessionStorage.table = JSON.stringify(container.table)
			let href = "create_field.html?tabid="+container.table.id+"&pkgid="+container.pkg.id
			if(window.parent && window.parent.addParentTab){
				window.parent.addParentTab({title:container.pkg.name+"新建字段",href:"workset/"+href})
			}else{
				window.open(href,"_blank")
			}
			break;
			case 'category':
			if(window.parent && window.parent.addParentTab){
				window.parent.addParentTab({title:"分类管理",href:"workset/category.html?pkgId="+container.pkg.id})
			}
			break
		}
	})
	
	function pkgRmTab(){
		layer.confirm("您确定要从数据包"+container.pkg.name+"中移除数据表"+container.table.name+"吗？",{
			title : "确定",
			btn : ["确定","取消"]
		},function(index,layero){
			Vue.http.delete(basePath+"pkg/rm/"+container.pkg.id+"/"+container.table.id).then(function(res){
				if(res.body.code == 1){
					container.tables = container.tables.filter(item=>{return item != container.table})
					container.tables.length ? container.showFields(container.tables[0]) : '';
				}
				layer.msg(res.body.message)
			},function(error){
				layer.msg("网络异常")
			})
		},function(index,layero){
			return true;
		})
	}
	
	function update(obj){
		var value = obj.value //得到修改后的值
		,data = obj.data //得到所在行所有键值
		,field = obj.field; //得到字段
		var index = data.index - 1;
		var column =  container.findFieldById(obj.data.id)
		var tmp = column[field];// 保存原来的值
		column[field] = value;
		Vue.http.put(basePath+"vc",tile(column)).then(function(res){
			if(res.body.code != 1){
				column[field] = tmp;
				obj.value = tmp;
				layer.msg(res.body.message);
			}else{
				obj.update()
			}
		},function(error){
			column[field] = tmp;
			layer.msg("网络异常，修改失败")
		})
	}
	
	function del(obj){
		layer.confirm("你确定要删除该字段吗？",{
			title : "提示",
			btn : ['确定','取消']
		},function(index,layer0){
			layer.close(index)
// 			var x = container.findFieldById(obj.data.id)
// 			x.state = 0;
			// TODO  请求后台删除
			Vue.http.delete(basePath+"vc/"+obj.data.id).then(function(res){
				if(res.body.code != 1){
					layer.msg(res.body.message)
					// x.state = 1;
				}else{
					container.fields = container.fields.filter((item)=>{return obj.data.id != item.id;})
					obj.del()
				}
			},function(error){
				layer.msg("网络异常，删除失败")
			})
		})
	}
})
