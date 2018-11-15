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
		addTable:function(pkg,event){// 添加数据表格
			location.href = "pkg-add-tab.html?pkg.id="+this.pkg.id;
			event.preventDefault()
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
		}
	},
	updated : function(){
		layuiTable.init('kjix12312',{
			page : {
				limit : 5,
				limits : [5,10,15,20,25],
				curr : 1
			}
		})
	}
})

var layuiTable = null;
layui.use('table',function(){
	layuiTable = layui.table
	layuiTable.on('edit(kjix12312)',function(obj){
		var value = obj.value //得到修改后的值
		,data = obj.data //得到所在行所有键值
		,field = obj.field; //得到字段
		// alert('[ID: '+ data.id +'] ' + field + ' 字段更改为：'+ value);
		var index = data.index - 1;
		var tmp = container.fields[index][field];// 保存原来的值
		container.fields[index][field] = value;
		Vue.http.put(basePath+"vc",container.fields[index]).then(function(res){
			if(res.body.code != 1){
				container.fields[index][field] = tmp;
				obj.value = tmp;
				layer.msg(res.body.message);
			}
		},function(error){
			container.fields[index][field] = tmp;
			layer.msg("网络异常，修改失败")
		})
		console.log(obj)
	})
	
	layuiTable.on("tool(kjix12312)",function(obj){
		console.log(obj)
		switch(obj.event){
			case "edit":
				
			break;
			case "del":
				layer.confirm("你确定要删除该字段吗？",{
					title : "提示",
					btn : ['确定','取消']
				},function(index,layer0){
					layer.close(index)
					var x = container.fields[obj.data.index-1];
					x.state = 0;
					// TODO  请求后台删除
					Vue.http.put(basePath+"vc",x).then(function(res){
						if(res.body.code != 1){
							layer.msg(res.body.message)
							x.state = 1;
						}else{
							container.fields = container.fields.filter((item)=>{return x != item;})
							obj.del()
						}
					},function(error){
						layer.msg("网络异常，删除失败")
					})
				},function(index,layero){
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
			location.href = "create_field.html?tabid="+container.table.id+"&pkgid="+container.pkg.id
			break;
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
})
