Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;
/**
 * 包组件
 */
Vue.component("package",{
	props:{
		pg : {
			type : Object,
			required : true
		},
		edit : {
			type : Boolean,
			default : false
		}
	},
	template:"<div class='layui-text'><span v-if='!edit' v-on:dblclick='toEdit($event)'>{{pg.name}}</span><input v-model='pg.name' v-if='edit' v-on:change='modify($event)'/></div>",
	methods:{
		toEdit : function(event){
			this.edit = true;
		},
		modify : function(event){
			this.$emit('increment')
		}
	},
	watch:{
		"edit" : function(newVal,oldVal,event){
			toggle(newVal,this)
		}
	},
	mounted : function() {
		toggle(this.edit,this)
		delete this.pg.edit
	}
})

function toggle(val,that){
	if(val){
		$(that.$el.parentNode).removeClass("scale")
		setTimeout(function(){
			var input = that.$el.querySelector("input")
			input.select()
			$(input).blur(function(e){
				that.edit = false;
			})
		},100)
	}else{
		$(that.$el.parentNode).addClass("scale")
	}
}

var app = new Vue({
	el : "#wp-list-container",
	data : {
		packages : []
	},
	created : function(){
		for(var i = 0; i < 20; i++){
			this.packages.push({
				name : "数据包" + i
			})
		}
	},
	watch : {
		"packages" : function(newVal,oldNew){
			
		}
	},
	methods : {
		saveUpdate : function(event){// 在这里保存修改
			layer.msg(JSON.stringify(this.packages))
		},
		remove : function(pkg,event){// 删除
			var that = this
			layer.confirm("您确定要删除该数据包吗？",{
				btn:["取消","确定"]
			},function(index,layero){
				layer.close(index)
			},function(index,layero){
				openLoading()
				setTimeout(function(){
					closeLoading()
					alertDialog("删除成功！")
					that.packages = that.packages.filter(function(item){return item.name != pkg.name})
				},1000)
			})
		},
		add : function(event){// TODO 新建数据包
			var that = this
			var newpkg = {name:"数据包",edit:true}
			for(var i = 0; i <= that.packages.length; i++){
				var f = true;
				for(var j in that.packages){
					var curPkg = that.packages[j]
					if(curPkg.name == newpkg.name+i){
						f = false;
						break;
					}
				}
				if(f)
					break;
			}
			newpkg.name += i
			that.packages.push(newpkg)
		},
		toTable : function(pkg,event){
			location.href = "pkg-table.html?pkgid="+pkg.name
			event.preventDefault()
		}
	}
})
/*****************************************************************/
function addPackage(){
	
}

function showAddDialog(){
	layer.open({
		title : "添加数据包",
		content : $("#addPackageDiv"),
		type : 1,
		btn : ["取消","添加"],
		area : ["400px","300px"],
		success : function(){
			$("#addPackageDiv").removeClass("layui-hide")
		},
		cancel : close,
		end : function(){
			$("#addPackageDiv").addClass("layui-hide")
		},
		btn1: close,
		btn2 : function(index,layero){
			confirmDialog("您确定要提交吗？",function(){
				layer.close(index,layero)
				alertDialog("保存成功")
			})
			return false;
		}
	})
	
	function close(index,layero){
		layer.confirm("您确定要放弃吗？",{
			title : "提示",
			btn : ["取消","确定"]
		},function(i,l){
			layer.close(i)
		},function(){
			layer.close(index,layero);
			return true;
		})
		return false;
	}
}