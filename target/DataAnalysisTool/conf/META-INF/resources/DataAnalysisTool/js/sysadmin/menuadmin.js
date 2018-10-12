
$(function(){
	let menu = new Vue({
		el:"#menu",
		data:{
			rootMenu:{children:[]},
			stack:[],
			first:null,
			tree:[],
			properties:['link','title','cssClass','icon','order'],
			newItem:{parentNode:{id:null}}
		},
		created:function(){
			$.ajax({
			type:"get",
			url:basePath+"menu/0",
			async:true,
			xhrFields:{
				withCredentials:true
			},
			success:function(res){
				res.children = res.data;
				menu.rootMenu = res
				menu.first = res
			}
		});
		},
		methods:{
			showChildren:function(target){// 显示子菜单
				if(target.children && target.children.length){
					this.stack.push(this.rootMenu)
					Vue.set(this,'rootMenu',target)
					this.rootMenu.children.sort(function(i1,i2){
						return i1.order < i2.order
					})
				}
			},
			pre:function(){// 显示上一次的菜单
				target = this.stack.pop()
				if(target){
					Vue.set(this,'rootMenu',target)
				}
			},
			edit:function(obj,propety,event){// 编辑
				let span = event.target.querySelector("span")
				$(span).addClass("hidden")
				let input = event.target.querySelector("input")
				$(input).removeClass("hidden")
				input.focus()
			},
			editfinish:function(item,property,event){// 保存
				let span = event.target.parentNode.querySelector("span")
				$(span).removeClass("hidden")
				let input = event.target
				$(input).addClass("hidden") 
			},
			save:function(item,property,$event){
				console.log(item)
				$.ajax({
					type:"get",
					url:basePath+"menu/save",
					async:true,
					data:{
						id : item.id,
						text :item.text,
						link : item.link,
						title : item.title,
						cssclass : item.cssClass,
						icon :item.icon,
						parent : item.parent,
						order : item.order
					},
					xhrFields:{
						withCredentials:true
					},
					success:function(res){
						
					},error:function(error){
						console.log(error)
					}
				});
			},
			remove:function(item,event){
				let that = this
				$.ajax({
					type:"post",
					url:basePath+"menu/delete",
					async:true,
					data:item,
					xhrFields:xhrFields,
					success:function(res){
						if(res.code === 1){
							that.rootMenu.children = that.rootMenu.children.filter(function(cur){
								return cur !== item							
							})
						}else{
							console.log(res)
							alert(res.message)
						}
					},error:function(error){
						console.log(error)
					}
				});
			},
			setNewItemParent:function(item){
				Vue.set(this.newItem,'parentNode',item)
			},
			add:function(){
				if($("#menuform").valid()){
					this.newItem.parent = this.newItem.parentNode.id
					$.ajax({
						type:"post",
						url:basePath+"menu/",
						async:true,
						xhrFields:xhrFields,
						data:menu.newItem,
						success:function(res){
							if(res.code == 1){
								menu.newItem.parentNode.children.push(res.data)
							}
							else{
								console.log(res)
								alert(res.message)
							}
						}
					});
				}
			}
		}
	})


	$("#menuform").validate({
		rules:{
			text:{
				required:true
				,rangelength:[1,255]
			},
			link:{
				required:true,
				rangelength:[1,255]
			},
			title:{
				maxlength:255
			},
			cssClass:{
				maxlength:255
			},
			order:{
				
			}
		},
		errorPlacement:function(error,element){
			error.css("color","red").insertAfter(element)
			console.log(error)
		},
		submintHandler:function(){
			console.log("xxxxxxxxxxxxxxxxxx")
		}
	})
})