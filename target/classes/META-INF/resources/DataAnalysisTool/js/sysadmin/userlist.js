Vue.http.options.withCredentials = true
Vue.http.options.emulateJSON = true
var usersView = new Vue({
	el : "#list",
	data : {
		userlist : null,
		pageSize : null,
		currPage : null,
		totalPage : null
	},
	created:function(){
		$.ajax({
			type:"get",
			url:basePath+"user/list",
			async:true,
			success:function(res){
				if(res.code === 1){
					usersView.userlist = res.data
					pagination("#list",{
						totalCount : res.data.length,
						pageSize : 5,
						curPage : 1
					},function(obj){
						console.log(obj)
						usersView.pageSize = obj.pageSize
						usersView.currPage = obj.curPage
						usersView.totalPage = obj.totalPage
					})
				}else{
					console.log(res);
				}
			}
		});
	},
	methods : {
		update:function(user){
			let tmp = user.password
			user.password = "12345678"
			this.$http.put(basePath+"user/update",user).then(function(res){
				if(res.body&&res.body.code == 1){
					showDialog("消息",res.body.message)
					user.password = res.body.data.password
				}else{
					showDialog("提示",res.body.message)
					user.password = tmp
				}
			},function(error){
				showDialog("提示","网络异常")
				user.password = tmp
			})
		},
		remove :function(user){
			this.$http.delete(basePath+"user/"+user.id).then(function(res){
				showDialog("消息",res.body.message)
			},function(error){
				showDialog("警告","请求异常")
			})
		}
	}
})

var addUserFormVue = new Vue({
	el : "#addUserModal",
	data : {
		user : {username:null,password:null,repassword:null}
	},
	created:function(){
		
	},
	methods:{
		addUser:function(event){
			let result = $("#addUserForm").valid()
			if(result){
				this.$http.post(basePath+"user/add",this.user).then(function(res){
					if(res.body){
						showDialog("提示",res.body.message)
						usersView.userlist.unshift(res.body.data)
					}else{
						showDialog("提示","服务器异常")
					}
				},function(error){
					showDialog("提示","网络异常")
				})
				this.user.username = null;
				this.user.password = null;
				this.user.repassword = null;
			}else{
				event.stopPropagation()
			}
		}
	}
})


/*************************************************表单验证***********************************************/
$.validator.addMethod("validateCharactor",function(value,element,param){
	if(/^[\w_]{8,18}$/.test(value)){
		return true;
	}
	return false;
},"该字段只能数字、字母和下划线")
$("#addUserForm").validate({
	rules:{
		username:{
			required:true,
			rangelength:[8,18],
			validateCharactor:true
		},
		password:{
			required:true,
			rangelength:[8,18],
			validateCharactor:true
		},
		repassword:{
			required:true,
			equalTo:"#password"
		}
	},
	messages:{
		username:{
			required:"请输入用户名",
			rangelength:"用户名不小于8位，不能大于18位"
		},
		password:{
			required:"请输入密码",
			rangelength:"密码不能小于8位，不能超过18位"
		},
		validatecode:{
			required:"请输入验证码"
		}
	},
	errorPlacement:function(error,element){
		error.css("color","red")
		error.appendTo(element.parent())
	},
	submitHandler:function(form){
		
	}
})



function search(){
	let queryValue = document.queryForm.queryValue.value
	if(queryValue == null){
		showDialog("错误","请输入查询条件！")
		return;
	}
	Vue.http.get(basePath+"user/list/"+queryValue).then(function(res){
		if(res.body && res.body.code == 1)
		Vue.set(usersView,"userlist",res.body.data)
	},function(error){
		console.log(error);
	})
}
