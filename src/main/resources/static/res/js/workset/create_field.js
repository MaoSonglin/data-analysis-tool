	Vue.http.options.withCredentials=true
	Vue.http.options.emulateJSON=true
	var pkgid = getParameter("pkgid"); 
	var tabid = getParameter("tabid");
	
	var form,element;
	layui.use(['element','form'], function(){
		form = layui.form, element = layui.element;
		form.on("select(pkgfilter)",function(data){
			app.setCurPkg(data.value)
		});
		form.on("select(tablefilter)",function(data){
			app.setTable(data.value)
		})
		form.on("select(columnfilter)",function(data){
			if(!app.column.refColumns) app.column.refColumns = new Array();
			for(var i in app.curTable.columns){
				if(data.value == app.curTable.columns[i].id){
					app.column.refColumns.push(app.curTable.columns[i])
					app.formulaAppend(app.curTable.columns[i].formula)
					break;
				}
			}
		})
		form.on("radio(typeName)",function(data){
			if(app.column.typeName)
				app.column.typeName = data.value
			else{
				Vue.set(app.column,'typeName',data.value)
			}
		})
	});
	
	

	var app = new Vue({
		el : "#app",
		data : {
			pkgs : [] ,// 数据包列表
			curPkg : {},// 当前数据包
			curTable : {columns:[]}, // 当前选择的数据表
			pkgPage : {
				curPage : 0,
				pageSize : 100
			},
			column : {},// 新建字段
			treeNodes : [],
			backurl : "pkg-table.html?pkgid="+pkgid
		},
		created: function(){
			getPage(this,this.pkgPage)
		},
		mounted: () => {
			form.render("radio")
			form.render("select")
			// showTree(this.pkgs)
		},
		updated:() =>{
			form.render("radio")
			form.render("select")
			// showTree(this.pkgs)
		},
		methods:{
			setCurPkg:function(pkgid){		// 选择当前的数据包
				for(var i in this.pkgs){ // 遍历所有数据包
					if(pkgid === this.pkgs[i].id){ // 当上一页面传递的业务包ID和当前迭代的业务包id相同时
						this.curPkg = this.pkgs[i] // 设置当前业务包
						if(!this.curPkg.tables){	// 如果当前业务包不包含数据表信息
							getPkgTables(this,this.curPkg)	// 获取数据表信息
						}
						break;
					}
				}
			},
			selected : function(pkg){
				return this.curPkg.id == pkg.id
			},
			input : function(event){ // 小键盘输入事件处理
				switch(event.target.innerHTML){
					case '#':
					break;
					case 'DEL':
					this.stack.pop()
					this.column.formula = this.stack.join("")
					break;
					default:
					this.formulaAppend(event.target.innerHTML)
				}
				event.stopPropagation()
			},
			formulaAppend:function(value){
				if(!this.stack) this.stack = new Array();
				this.stack.push(value);
				if(this.column.formula){
					this.column.formula += value;
				}
				else{
					Vue.set(this.column,'formula',value);
				}
			},
			treeNodeClick : function(event){
				alert(JSON.stringify(event))
			},
			setTable : function(tabid){ // 加载指定数据表中的数据字段
				for(var i in this.curPkg.tables){
					if(tabid === this.curPkg.tables[i].id){
						Vue.http.get(basePath+"vt/vc/"+this.curPkg.tables[i].id).then((res)=>{
							this.curTable = this.curPkg.tables[i]
							Vue.set(this.curTable,"columns",res.body.data)
						})
						break;
					}
				}
			},
			add : function(event){// 添加
				// alert(JSON.stringify(tile(this.column)))
				this.column.table = {id:tabid}
				Vue.http.post(basePath+"vc",tile(this.column)).then((res)=>{
					if(res.body.code == 1){
						location.href = this.backurl;
					}else{
						layer.msg(res.body.message);
					}
				},function(error){
					console.log(error)
				})
			}
		},
		watch:{
			"column.formula" : function(newVal,oldVal){
				// layer.alert(newVal)
			}
		}
	})
	app.$on("nodeClick",function(event){
		alert(JSON.stringify(event))
	})
	
	function getPage(vue,page){
		Vue.http.get(basePath+"pkg",page,{
		}).then(function(res){
			if(res.body.code == 1){
				Vue.set(vue,'pkgs',format(res.body.data.content))
				Vue.set(vue,'treeNodes',[{name:'数据包',children:vue.pkgs}])
				vue.setCurPkg(pkgid)
			}
		},function(error){
			throw error;
		})
	}
	function format(content){
		for(var i in content){
			content[i].url = basePath + "pkg/tab/" +content[i].id
		}
		return content;
	}
	/**
	 * 获取指定数据包下的所有数据表
	 */
	function getPkgTables(vue,pkg){
		Vue.http.get(basePath+"pkg/tab/"+pkg.id).then(function(res){
			if(res.body.code == 1){
				Vue.set(pkg,'tables',res.body.data);
				app.setTable(tabid)
			}
		},function(error){
			
		})
	}
	
	function showTree(nodes){
		$("#myTree").empty()
		var node = [{name:"数据包",children:nodes}]
		layui.use('tree',function(){
			layui.tree({
				elem : "#myTree",
				nodes : node,
				click : function(){}
			})
		})
	}