

var menulist = [
	{
		text:"中文水电费还送",
		link:"#http://www.baidu.com/",
		children:[
			{
				text:"天猫",
				link:"https://www.tianmao.com/",
				active:null
			},
			{
				text:"淘宝",
				link:"https://www.taobao.com",
				active:null
			},
			{
				text:"苏宁易购",
				link:"https://www.suing.com",
				active:null
			}
		]
	},
	{
		text:"中信红",
		link:"#",
		children:[
			{
				text:"华为",
				link:"https://www.huawei.com",
				active:null
			},
			{
				text:"阿里巴巴",
				link:"https://www.alibaba.com",
				active:null
			},
			{
				text:"腾讯",
				link:"https://www.qq.com",
				active:null
			}
		]
	}
]
for(let i = 0; i < 10; i++){
	menulist.push({
		text:"x"+i,
		link:"#",
		children:[
			{
				text:"华为",
				link:"https://www.huawei.com",
				active:null
			},
			{
				text:"阿里巴巴",
				link:"https://www.alibaba.com",
				active:null
			},
			{
				text:"腾讯",
				link:"https://www.qq.com",
				active:null
			}
		]
	})
}
function addCss(){
	for(let i =0;i < menulist.length; i++){
		let item = menulist[i].children;
		for(let j=0; j < item.length; j++){
			item[j].cssClass = "list-group-item";
		}
	}
}
addCss();
let v = new Vue({
	el:"#menu",
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
		active:function(item,event){
			addCss()
			item.cssClass = "list-group-item active"
			//$(event.target).addClass("active");
		}
	}
})

let header = new Vue(
	{
		el:"#header",
		data:{
			user:null
		},
		created:function(){
			let that = this
			// 检查用户是否登录
			let i = setInterval(function(){
				console.log("检查用户是否登录..")
				$.ajax({
					type:"get",
					url:basePath+"user/state",
					async:true,
					xhrFields:xhrFields,
					success:function(res){
						if(res.code === 1 && res.message === "用户已经登录"){
							Vue.set(that,'user',res.data)
							clearInterval(i)
						}
						console.log("用户还未登录....")
					}
				});
			},1000*6)
		}
	}
)

