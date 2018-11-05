Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;

var pkgid = getParameter("pkgid")
var container = new Vue({
	el : "#container",
	data : {
		tables : [],
		page : {},
		fields : [],
		table : {},
		pkg : {}
	},
	created : function(){
		this.$http.get(basePath+"pkg/tab/"+pkgid,{before:openLoading}).then(function(res){
			closeLoading()
			layer.msg(res.body.message)
			Vue.set(this,'tables',res.body.data)
			this.pkg = res.body.values.workPackage
		},function(error){
			closeLoading()
			layer.msg("网络异常！")
		})
	},
	methods : {
		showFields : function(table,event){
			var array = new Array();
			for(var i = 0; i < 100; i++){
				array.push(table.name+"字段"+i);
			}
			this.$http.get(basePath+"vt/vc/"+table.id,{
				before : openLoading
			}).then(function(res){
				closeLoading()
				if(res.body.code == 1){
					this.$set(this,"fields",res.body.data);
					this.table = table;
				}
				layer.msg(res.body.message)
			},function(error){
				closeLoading()
				layer.msg("网络异常")
			})
		},
		addTable:function(pkg,event){
			location.href = "pkg-add-tab.html?pkg.id="+this.pkg.id;
			event.preventDefault()
		}
	},
	watch : {
		"fields" : function(newVal,oldVal){
		}
	},
	updated : function(){
		layuiTable.init('kjix12312',{})
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
		for(var i in container.fields){
			let curr = container.fields[i]
			if(curr.id ==  data.id){
				curr[field] = value
			}
		}
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
					var x = container.fields[obj.data.id];
					// TODO  请求后台删除
					
					container.fields = container.fields.filter((item)=>{return x != item;})
					obj.del()
				},function(index,layero){
				})
			break;
		}
	})
})
