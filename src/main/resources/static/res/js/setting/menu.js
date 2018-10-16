$(function() {
	Vue.http.options.withCredentials = true;
	Vue.http.options.emulateJSON = true;
	var app = new Vue({
		el: "#app",
		data: {
			menu: null,
			addMenu: {},
			menus: [],
			isEdit: false,
			isAdd: false
		},
		created: function() {
			loadTree()
			this.addMenu = new Object()
			SetNull(addMenu)
		},
		mounted: function() {},
		methods: {
			toEdit: function(event) {
				var $target = $(event.target)
				$target.toggle()
				var $toggleTarget = $(event.target).next("td")
				$toggleTarget.toggle()
				$toggleTarget.focus()
				event.stopPropagation()
			},
			save: function(menu, parent, event) { // 保存
				// TODO 保存菜单
				if (!$(document.forms[0]).valid()) {
					return;
				}
				let icon = document.EditForm.icon.value
				let cssclass = document.EditForm.cssclass.value
				confirmDialog("确定要提交吗？", function() {
					Vue.http.put(basePath + "menu/", {
						id : menu.id,
						text : menu.text,
						title : menu.title,
						link : menu.link,
						cssclass : cssclass,
						icon : icon,
						parent : menu.parent
					}, {
						before: layer.load(1)
					}).then(function(res) {
						alertDialog(res.body.message, function() {
							if (parent && res.body.code == 1) {
								showTree(app.menus)
							}
							layer.closeAll()
							app.cancel()
						})
					}, function(error) {
						alertDialog("网络错误！", layer.closeAll)
					})
				})
				
			},
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
				this.isAdd = true
				this.isEdit = false
				this.addMenu.parent = this.menu.id
			},
			toUpdate: function(menu, event) {
				this.isEdit = true
				this.isAdd = false
			},
			cancel: function(event) {
				this.isAdd = false
				this.isEdit = false
				SetNull(addMenu)
			},
			add: function(menu, parent, event) { // 添加子菜单
				if ($("#addForm").valid()) {
					confirmDialog("确定要提交吗？", function() {
						Vue.http.post(basePath + "menu/", {
							text : menu.text,
							title : menu.title,
							link : menu.link,
							cssclass : document.addFrom.cssclass.value,
							icon :  document.addFrom.icon.value,
							parent : parent.id? parent.id : ""
						}, {
							before: layer.load(1)
						}).then(function(res) {
							alertDialog(res.body.message, function() {
								if (parent && res.body.code == 1) {
									if (!parent.children) parent.children = new Array()
									parent.children.push(res.body.data)
									showTree(app.menus)
								}
								layer.closeAll()
								app.cancel()
							})
						}, function(error) {
							alertDialog("网络错误！", layer.closeAll)
						})
					})
				}
			}
		},

	})

	function SetNull(menu) {
		menu.id = null,
			menu.text = null,
			menu.link = null,
			menu.title = null,
			menu.cssclass = null,
			menu.icon = null,
			menu.parent = null,
			menu.order = null,
			menu.spread = false
	}

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
			showTree(app.menus)
		}, function(error) {

		})
	}

	function showTree(nodes) {
		format(nodes)  
		$("#myTree").empty()
		// 渲染树形结构
		layui.use('tree', function() {
			layui.tree({
				elem: "#myTree",
				click: menuTreeItemClick,
				nodes: nodes,
				skin: 'shihuang'
			})
		})
	}


	function menuTreeItemClick(options) {
		// alert(JSON.stringify(options))
		app.isEdit = false
		app.isAdd = false
		app.menu = options
		SetNull(app.addMenu)
	}
	
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
	
	function findPerent(all,child){
		if (typeof all === "array"){
			for(var i in all){
				if(all[i].id === child.id){
					return all
				}
			}
			for(var i in all){
				var r = findPerent(all[i].children,child)
				if(r)
					return r
			}
		}
		else if(typeof all === "object"){
			if( all !== child ){
				return findPerent(all.children,child)
			}
		}
		return null
	}

})

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
			content : "../frame/icons.html",
			type : 2,
			btn : ['确定','取消'],
			area: ['500px', '300px'],
			btn1 : function(index,layero){
				layer.close(index)
				obj.value = localStorage.icon
			},
			btn2 : function(index,layero){
				layer.close(index)
			}
		})
	}