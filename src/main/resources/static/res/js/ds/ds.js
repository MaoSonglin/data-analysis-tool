Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;

var app = new Vue({
	el : "#wrapper",
	data : {
		dsList : dsList,
		datasource : {},
		keySet : keySet,
		page : {
			curr : 0,
			size : 10,
			search : " ",
		}
	},
	created : function(){
		getDsPage(this.page);
	},
	methods : {
		toUpdate : function(ds,event){ // 弹出修改数据源信息的模态框
			addForm.ds = ds;			// 设置要修改的数据
			showDialog("修改数据源")
			event.stopPropagation()
		},
		remove : function(ds,event){	// 删除一个数据源
			confirmDialog("您确定要删除这个数据源吗？",del)
			function del(){
				Vue.http.delete(basePath+"ds/"+ds.id,{
					before : openLoading
				}).then(function(res){
					closeLoading()
					layer.msg(res.body.message)
					if(res.body.code == 1){
						app.dsList = app.dsList.filter(function(item){return item !== ds})
						if(ds==app.datasource){
							app.datasource=app.dsList[0]
						}
					}
				},function(error){
					closeLoading()
					layer.msg("网络异常，请稍后重试！")
				})
			}
			event.stopPropagation()
		},
		choose : function(ds,event){
			this.datasource = ds
			event.stopPropagation()
		},
		showValue : function(key,value){
			if(key == "password"){
				return "*********";
			}
			else if(typeof(value) == 'string' || typeof(value) == 'number' || typeof(value) == 'boolean'){
				return value;
			}
			else if(key == "association" && value){
				return value.fileName;
			}
			return null;
		}
	},
	watch : {
		"page.curr" : function(newVal,oldVal){
			getDsPage(this.page)
		},
		"page.size" : function(newVal,oldVal){
			getDsPage(this.page)
		}
	}
});

function getDsPage(page){
	Vue.http.get(basePath+"ds/"+page.curr+"/"+page.size+"/"+page.search,{
		before : openLoading
	}).then(function(res){
		if(res.body.code !== 1){
			layer.msg(res.body.message)
		}else{
			app.dsList = res.body.data.content
			app.datasource = app.dsList.shift()
			if(app.datasource){
				app.dsList.unshift(app.datasource)
			}
			layui.use('laypage',function(){
				layui.laypage.render({
					elem:"pageinfo",
					curr : res.body.data.number+1,
					limit : res.body.data.size,
					count : res.body.data.totalElements,
					layout: ['count', 'prev',  'next', 'skip'],
					jump:function(obj,first){
						if(!first){
							app.page.curr =  obj.curr-1;
							app.page.size = obj.limit;
						}
					}
				})
			})
		}
		closeLoading()
	},function(error){
		closeLoading()
		layer.msg("网络异常")
		console.log(error)
	})
}

/**
 * 判断一个数据源名称对应的数据源类型
 */
function isFile(name){
	for(var i in databaseProductNames.file){
		if(name == databaseProductNames.file[i]){
			return true;
		}
	}
	return false;
}
/**
 * 表单
 */
var addForm = new Vue({
	el : "#add-div",
	data : {
		dstype : [],
		ds : {},
		isFile : true,
		loading : null
	},
	created : function(){
		for(var i in databaseProductNames.server){
			this.dstype.push(databaseProductNames.server[i])
		}
		for(var i in databaseProductNames.file){
			this.dstype.push(databaseProductNames.file[i])
		}
	},
	methods:{
		chooseDatabase : function(){
			$("#add-div-2").removeClass("layui-hide")
			this.index = layer.open({
				title : "选择数据源类型",
				type : 1,
				content : $("#add-div-2"),
				btn : ["取消"],
				success : function(){
				},
				end : function(){
					$("#add-div-2").addClass("layui-hide")
				},
				btn1 : function(index,layero){
					layer.close(index,layero)
				},
			})
		}
	},
	watch : {
		"ds.databaseName" : function(newVal,oldVal){
			layer.close(this.index);
			try{
				this.ds.isFile = isFile(newVal)
				this.ds.driverClass=driverInfos[newVal].driverClass
				this.ds.url=driverInfos[newVal].url
			}catch(e){
				console.log(e)
			}
		},
		"ds.association":function(newVal,oldVal){
			this.isFile = newVal ? true:false;
		}
	},
	mounted : function(){
		drawUpload()
	}
})


/**
 * 重新绘制表单
 */
function drawForm(){
	layui.use(['form'], function(){
		var form = layui.form; 
		form.render()
		form.on('select(dstype)',function(data){
			Vue.set(addForm.ds,'databaseName',data.value);
		})
	 }); 
}
 
 /**
  * 显示添加数据源的模态框
  */ 
 function showAddDialog(){
	 addForm.ds = {} // 创建一个新的数据源对象
	 showDialog("添加数据源")
 }
 /**
  * 显示一个编辑对话框
  */
 function showDialog(title){
	 // 保存编辑前与表单关联的数据值
	 var cloner = new Cloner(addForm.ds)
	 layer.open({
		 title : title,
		 type : 1,
		 content : $("#add-div"),
		 closeBtn : 2,
		 skin: 'layui-layer-lan',
		 area : ["700px","500px"],
		 btn : ["取消","确定"],
		 btn1 : close, // 点击第一个按钮
		 btn2 : submit,		// 点击第二个按钮
		 cancel : close,// 关闭按钮
		 // success : drawForm, // 模态框打开
		 end : function(){$("#add-div").hide();},// 模态框销毁事件
	 })
	 
	 function submit(index,layero){
		 // 验证表单的完整性
		 var f = $("#addForm").valid();
		 if(!f) return f;// 验证不通过
		 // 验证文件是否上传
		 if(addForm.ds.isFile && !addForm.ds.association){
			 var msg = addForm.ds.databaseName+"类型的数据源需要数据源文件，您确定要提交吗？"
		 }
		 else{
			 var msg = "您确定要提交吗？"
		 }
		 // 验证通过
		 layer.confirm(msg,{
			 btn : ["取消","确定"],
			 skin: 'layui-layer-lan'
		 },function(i,l){
			 layer.close(i)
		 },function(i,l){
			 tj()
			 layer.close(index)
		 })
		 
		 return false ;// 2、 返回true关闭模态框
	 }
	 
	 function tj(){
		 
		 var options = {
		 	before : function(){
		 		openLoading() // 3、提交之前显示加载层
		 	}
		 }
		 if(addForm.ds.id){// 修改
			 Vue.http.put(basePath+"ds/",tile(addForm.ds),options).then(success,fail)
		 }else{// 添加
			 Vue.http.post(basePath+"ds/",tile(addForm.ds),options).then(success,fail)
		 }
		 /**
		  * 请求失败回调函数
		  */
		 function fail(e){
			 closeLoading()
			 error("网络异常，请稍后重试")
		 }
		 
		 /**
		  * 请求成功回调
		  */
		 function success(res){
			 closeLoading()
			 if(res.body.code == 1){
				 if(addForm.ds.id){// 修改成功
					 addForm.ds = res.body.data;
				 }else{// 添加成功
					 app.dsList.push(res.body.data) //4、 在页面显示添加结果
				 }
				 app.datasource = res.body.data;
				layer.msg(res.body.message) // 5、提示成功
			 }else{
			 	error(res.body.message) // 保存失败
			 }
			
		 }
		 
		 /**
		  * 请求成功但添加失败调用的函数
		  */
		 function error(msg){
			 layer.alert(msg,{ // 提示原因
				closeBtn : false,
			 },function(){
				showDialog(title);// 重新显示模态框
			 })
		 }
	 }
	 /**
		* 点击取消按钮或者右上角的关闭按钮回调
		*/
	 function close(parentIndex,layero){
		 layer.confirm("您确定要放弃保存吗？",{
		 	skin: 'layui-layer-lan',
		 	btn : ["取消","确定"]
		 },function(index,layero){
		 	layer.close(index);
		 },function(){
			layer.closeAll("tips")// 关闭模态框中表单验证的提示消息
			layer.close(parentIndex);// 关闭模态框
			if(cloner.target.association != addForm.ds.association){
				// TODO 请求后台删除上传的数据
				console.log("删除之前上传的文件")
				Vue.http.delete(basePath+"file/"+addForm.ds.association.id).then(function(res){
					layer.msg(res.body.message)
				},function(error){
					layer.msg("网络错误")
				})
			}
			cloner.reset(addForm.ds); // 在关闭模态框是将表单关联的数据恢复到编辑之前
			return true;// 关闭
		 });
		 return false;
	 }
	 
 }
 
/**
 * 绘制文件上传按钮
 */
function drawUpload(){
	layui.use('upload',function(){
		var upload = layui.upload
		upload.render({
			elem : "#up-ds-btn",
			url : basePath+"file/upload/",
			accept : "file",
			before : function(){
				addForm.ds.association = null
				addForm.loading = "<i class='layui-icon layui-anim layui-anim-rotate layui-anim-loop'>&#xe63d;</i>"
			},
			done : function(res){
				console.log(res)
				if(res.code ===1){
					Vue.set(addForm.ds,'association',res.data);
					addForm.isFile = true
					layer.msg("上传成功");
				}
				else{
					alertDialog(res.message);
				}
				addForm.loading  = null
			},
			error : function(error){
				alertDialog("网络异常，上传失败")
				addForm.loading = null
			}
		})
	})
}

 
 // 验证添加数据源的表单
 $("#addForm").validate({
	 rules : {
		 dsname : {
			 required : true,
			 maxlength : 25
		 },
		 dstype : {
			 required : true
		 },
		 dsurl : {
			 required : true,
			 maxlength : 100
		 },
		 dsDriverClass : {
			 required : true,
			 maxlength : 50
		 },
		 dsuname : {
			 maxlength : 30
		 },
		 dspwd : {
			 maxlength : 30
		 },
		 comment : {
			 maxlength : 200
		 }
	 },
	 messages : {
		 dsname : {
			 required : "请输入数据源名称",
			 maxlength : "数据源名称不能超过25个字符"
		 },
		 dstype : {
			 required : "请选择数据源类型"
		 },
		 dsurl : {
			 required : "请输入数据源URL",
			 maxlength : "URL不能超过100个字符"
		 },
		 dsDriverClass : {
			 required : "请输入数据库驱动类",
			 maxlength : "数据库驱动类全路径名不能超过50个字符"
		 },
		 dsuname : {
			 maxlength : "用户名不能超过30个字符"
		 },
		 dspwd : {
			 maxlength : "密码不能超过30个字符"
		 }
	 },
	 errorPlacement : function(error,element){
		 if(this.prompt) return;
		 else this.prompt = true;
		 element.focus()
		 var that = this
		 layer.tips(error.html(), element,{
			 tips : 3,
			 time: 2*1000,
			 tipsMore: true,
			 end :function(){
				that.prompt = false
			 }
		 });
		// alertDialog(error.html())
	 }
 })
 
 
 new Vue({
	 el : "#add-div-2",
	 data : {
		 driverInfos : driverInfos
	 },
	 mounted : function(){
		 layui.use(['form'], function(){
		 	var form = layui.form; 
		 	form.render()
			console.log("xhonag")
		 	form.on('radio(dbName)',function(data){
		 		Vue.set(addForm.ds,'databaseName',data.value);
				// alert(data.value)
		 	})
		 }); 
	 }
 })
 