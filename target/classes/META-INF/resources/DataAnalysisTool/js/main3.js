document.writeln("<script type='text/javascript' src='js/basepath.js' ></script>")
document.writeln("<script type='text/javascript' src='js/vue.min.js' ></script>")
document.writeln("<script type='text/javascript' src='js/vue-resource.js'></script>")
document.writeln("<script type='text/javascript' src='alter.js' ></script>")
window.onload = function(){
	Vue.http.options.withCredentials = true;
	Vue.http.options.emulateJSON = true;
	var mune = new Vue({
		el : "#menu",
		data:{
			menulist:null,
			user:null
		},
		created:function(){
			let that = this
			$.ajax({
				type:"get",
				url:basePath+"menu/0",
				async:true,
				xhrFields:{
					withCredentials:true
				},
				success:function(res){
					that.menulist = res.data;
					for(let i =0;i < that.menulist.length; i++){
						let item = that.menulist[i].children;
						for(let j=0; j < item.length; j++){
							item[j].childItemClass = "list-group-item";
						}
					}
				}
			});
		},
		methods:{
			menuItemClick:function(e){
				//$("#menu .glyphicon.glyphicon-chevron-down").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right")
				let a = e.target.parentNode.querySelector("span.glyphicon")
				let nodelist = document.querySelectorAll("#menu .glyphicon.glyphicon-chevron-down")
				for(var i = 0 ; i < nodelist.length; i++){
					if(a !== nodelist[i]){
						$(nodelist[i]).removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right")
					}
				}
				$(a).toggleClass("glyphicon-chevron-down","glyphicon-chevron-right")
			},
			addPage:function(item,event){// 添加标签页
				let index = indexOf(content.navtabs,item);
				if(index === -1){
					// 添加标签页的表头
					content.navtabs.push(item);
					content.curPageIndex = content.navtabs.length - 1;
					let iframe = document.querySelector(".mainWindow")
					let clone = iframe.cloneNode(true)
					clone.src = item.link
					iframe.parentNode.appendChild(clone)
					let $ifremas = $(".mainWindow")
					$ifremas.hide()
					$(clone).show()
				}else{
					content.curPageIndex = index;
					let $ifremas = $(".mainWindow")
					$ifremas.hide()
					$($ifremas[index]).show()
				}
			}
		}
	})

	var content = new Vue({
		el : "#content",
		data : {
			navtabs : [{text:"首页",link:"index.htm"}],
			user : null,
			curPageIndex : 0
		},
		created:function(){
			
		},
		methods:{
			toggle:function(index){// 切换标签页
				this.curPageIndex = index
				let $ifremas = $(".mainWindow")
				$ifremas.hide()
				$($ifremas[index]).show()
			},
			remove:function(index,event){
				// 先获取网页中所有的iframe框架
				let iframes = document.querySelectorAll(".mainWindow")		
				// 移除指定下标的框架
				iframes[index].parentNode.removeChild(iframes[index])
				if(this.curPageIndex == index){
					if(index == iframes.length-1){
						$(iframes[index-1]).show()
					}else{
						$(iframes[index+1]).show()
					}
				}
				// 移除标签页的标头
				let needremove = this.navtabs[index]
				this.navtabs = this.navtabs.filter(function(item){
					return needremove !== item;
				})
				if((this.curPageIndex > index) || (this.curPageIndex == this.navtabs.length)){
					this.curPageIndex--;
				}
				event.stopPropagation()
			}
		}
	})
	
	let navVue = new Vue({
		el :"#header",
		data:{
			user:null
		},
		created:function(){
			this.$http.post(basePath+"user/state").then(function(res){
				console.log(res)
				if(res.body && res.body.code === 1 && res.body.data){
					this.user = res.body.data
				}
			},function(error){
				console.log(error)
			})
		},
		methods:{
			save:function(event){// 修改密码后保存密码
				let oldpwd = document.querySelector("#oldpwd").value
				let newpwd = document.querySelector("#newpwd").value
				let repwd = document.querySelector("#repwd")
				if($("#modifyForm").valid()){
//					if(oldpwd === newpwd){
//						alert("新密码和原密码不能相同！")
//					}
					this.$http.put(basePath+"user/update",{
						username:this.user.username,
						password:newpwd}).then(function(res){
							if(res.body.code === 1){
								navVue.user = res.body.data
								oldpwd = ""
								newpwd = ""
								repwd = ""
							}
							alert(res.body.message)
						},function(error){
							alert(error.status)
						})
				}else{
					event.stopPropagation()
				}
				
			},
			clear:function(){// 清除模态框中的表单
				let oldpwd = document.querySelector("#oldpwd")
				let newpwd = document.querySelector("#newpwd")
				let repwd = document.querySelector("#repwd")
				oldpwd.value = ""
				newpwd.value = ""
				repwd.value = ""
			}
		}
	})

	// 验证修改密码的表单
	$.validator.addMethod("validateCharactor",function(value,element,param){
		if(/^[\w_]{8,18}$/.test(value)){
			return true;
		}
		return false;
	},"密码只能包含数字、字母、下划线")
	$("#modifyForm").validate({
		rules : {
			oldpwd : {
				required: true,
				rangelength : [8,18],
				validateCharactor : true
			},
			newpwd : {
				required : true,
				validateCharactor:true,
				rangelength : [8,18]
			},
			repwd : {
				equalTo : "#newpwd",
				required : true
			}
		},
		messages : {
			oldpwd : {
				required : "请输入原密码",
				rangelength : "原密码不能少于8个字符，不能多于18个字符",
				validateCharactor : "密码只能包含数字、字母、下划线"
			},
			newpwd : {
				required : "请输入新密码",
				rangelength:"新密码不能少于8个字符，不能多于18个字符"
			},
			repwd : {
				required :"请输入确认密码",
				equalTo : "请输入相同的密码"
			}
		},
		errorPlacement : function(error,element){
			error.css("color","red").insertAfter(element)
		}
	})
	
}

function indexOf(array,item){
	for(let i = 0; i < array.length ; i++){
		if(array[i] === item){
			return i;
		}
	}
	return -1;
}
