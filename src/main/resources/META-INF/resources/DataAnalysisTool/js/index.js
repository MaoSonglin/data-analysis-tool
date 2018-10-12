var login = new Vue({
		el:"#login",
		data:{
			username:null,
			password:null,
			validatecode:null,
			autologin:true,
			validateimage:basePath+"valiate/image"
		},
		methods:{
			changeValidateImage:function(){
				this.validateimage =basePath+"valiate/image?x="+new Date()
			},
			reset:function(){
				this.username = null,
				this.password = null,
				this.validatecode = null
			}
		}
	})
	
	$.validator.addMethod("validateCharactor",function(value,element,param){
		if(/^[\w_]{8,18}$/.test(value)){
			return true;
		}
		return false;
	},"该字段只能数字、字母和下划线")
	$("#loginForm").validate({
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
			validatecode:{
				required:true
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
			
			$.ajax({
				method:"post",
				url:basePath+"user/login",
				data:{
					username:login.username,
					password:login.password,
					validateCode:login.validatecode,
					autologin:login.autologin
				},
				xhrFields:{
					withCredentials:true
				},
				success:function(res){
					alert(res.message)
					if(res.code === 1)
						window.top.location.href = 'main.html'
				},
				error:function(error){
					alert(error)
					console.log(error)
					login.password = null
				}
			})
		}
	})