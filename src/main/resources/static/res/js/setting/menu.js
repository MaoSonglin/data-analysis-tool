	Vue.http.options.withCredentials = true;
	Vue.http.options.emulateJSON = true;
	var app = new Vue({
		el: "#app",
		data: {
			menu: null,
			// addMenu: {},
			menus: []
		},
		created: function() {
			loadTree()
			// this.addMenu = new Object()
			// SetNull(addMenu)
		},
		mounted: function() {},
		methods: {
			remove: function(menu, event) {
				confirmDialog("您确定要删除菜单\"" + menu.text + "\"及其子菜单吗？", function() {
					Vue.http.delete(basePath + "menu/" + menu.id, {
						before: layer.load(1)
					}).then(function(res) {
						alertDialog(res.body.message, function(){
							layer.closeAll()
							var f = menu.father
							f.children = f.children.filter(function(item){
								return item.id !== menu.id;
							})
							showTree(app.menus)
							Vue.set(app,"menu",f)
						})
					}, function(error) {
						alertDialog("网络异常，请稍后重试", layer.closeAll)
					})
				})
			},
			toAdd: function(event) {
				addMenuVue.menu = {}
				editDialog("添加子菜单")
			},
			toUpdate: function(menu, event) {
				addMenuVue.menu = this.menu
				editDialog("修改")
			}
		},

	})

	// 加载目录树中的数据
	function loadTree() {
		Vue.http.get(basePath+"menu/").then(function(res) {
			// app.menu = res.body[0]
			app.menus = [{
					text: '系统菜单',
					name: "系统菜单",
					spread: true,
					children: res.body.data,
					id : null
				}],
			app.menu = app.menus[0]
			Vue.set(addMenuVue,'lst',res.body.data)
			showTree(app.menus)
		}, function(error) {

		})
	}

	// 显示目录树
	function showTree(nodes) {
		format(nodes)  
		$("#myTree").empty()
		// 渲染树形结构
		layui.use('tree', function() {
			layui.tree({
				elem: "#myTree",
				click: function(options){app.menu=options},
				nodes: nodes,
				skin: 'shihuang'
			})
		})
	}
	
	// 在显示目录树之前对数据进行处理
	function format(nodes) {
		for (var i in nodes) {
			var node = nodes[i]
			node.name = node.text
			if(!node.spread) node.spread = false
			if (node.children) {
				format(node.children)
				for(var j in node.children){
					node.children[j].father = node
				}
			}
		}
	}

	var addMenuVue = new Vue({
		el : "#menuform",
		data : {
			menu : {},
			lst : []
		},
		methods : {
			chooseIcon  : function(icon,event){
				layer.open({
					title : "选择图标",
					content : "icons.html",
					type : 2,
					btn : ['取消'],
					area: ['500px', '300px'],
					btn1 : function(index,layero){
						layer.close(index)
						// obj.value = localStorage.icon
					},
					success : function(){
						localStorage.icon = "";
					},
					end : function(){
						Vue.set(addMenuVue.menu,icon,localStorage.icon)
					}
				})
			},
			change : function(event){
				console.log(event.target)
			},
		}
	})
	addMenuVue.$watch('menu',function(newValue,oldValue){
		addMenuVue.menu.parent = null
		drawForm()
	})
	
	function editDialog(title){
		var cloner = new Cloner(addMenuVue.menu)
		layer.open({
			type : 1,
			title : title,
			content : $("#menuform"),
			skin: 'layui-layer-lan',
			area : ['600px','500px'],
			btn : ['提交','取消'],
			closeBtn : false,
			yes : function(index,layero){
				var options = {before : openLoading}// 请求选项，在请求前显示加载层
				var saver = new Saver()
				saver.save(['father','children'],addMenuVue.menu)
				console.log(JSON.stringify(addMenuVue.menu))
				if(addMenuVue.menu.id){
					Vue.http.put(basePath+"menu/",addMenuVue.menu,options).then(s,e)
				}else{
					Vue.http.post(basePath+"menu/",addMenuVue.menu,options).then(s,e)
				}
				function s(res){
					closeLoading()
					alertDialog(res.body.message,layer.close(index))
					if(!addMenuVue.menu.id){// 如果是添加
						if(father){
							father.children.push(res.body.data)
							res.body.data.father = father
							// 重新显示菜单树
							showTree(app.menus)
							app.menu = addMenuVue.menu
						}
					}
					if(res.body.code != 1){
						cloner.reset(addMenuVue.menu)
					}else{
						saver.restore(['father','children'],addMenuVue.menu)
					}
				}
				function e(error){
					closeLoading()
					alertDialog(error.body,layer.close(index))
					cloner.reset(addMenuVue.menu)
				}
			},
			cancel : function(index,layero){
				layer.close(index)
				cloner.reset(addMenuVue.menu)
			},
			success : function(){
			},
			end : function(){
				$("#menuform").hide()
				addMenuVue.mune = {}
			}
		})
	}

function drawForm(){
	layui.use('form',function(){
		var form = layui.form; 
		form.render()
		form.on('select(parent)', function(data){
			addMenuVue.menu.parent = data.value
			addMenuVue.menu.father = addMenuVue.lst.filter(function(item){
				return item.id == parseInt(data.value)
			}).pop()
		});
	})
	
}


	$("form").validate({
		rules: {
			text: {
				required: true,
				rangelength: [1, 255]
			},
			link: {
				required: true,
				rangelength: [1, 255]
			},
			title: {
				maxlength: 255
			},
			cssclass: {
				maxlength: 255
			},
			order: {

			}
		},
		errorPlacement: function(error, element) {
			alertDialog(error.html())
			alert("小红")
		},
		submintHandler: function() {
			console.log("xxxxxxxxxxxxxxxxxx")
		}
	})

	function showIcons(obj){
		layer.open({
			title : "选择图标",
			content : "icons.html",
			type : 2,
			btn : ['取消'],
			area: ['500px', '300px'],
			btn1 : function(index,layero){
				layer.close(index)
				// obj.value = localStorage.icon
			},
			end : function(){
				
			}
		})
	}
	
	