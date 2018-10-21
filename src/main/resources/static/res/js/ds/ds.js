Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;

var app = new Vue({
	el : "#wrapper",
	data : {
		dsList : []
	},
	created : function(){
		Vue.http.get(basePath+"ds/",{
			before : openLoading
		}).then(function(res){
			console.log(res.body)
			if(res.body.code !== 1){
				alertDialog(res.body.message)
			}else{
				app.dsList = res.body.data.content
			}
			closeLoading()
		},function(error){
			closeLoading()
			alertDialog("网络异常")
			console.log(error)
		})
	},
	methods : {
		toUpdate : function(ds,event){ // 弹出修改数据源信息的模态框
			addForm.ds = ds;			// 设置要修改的数据
			showDialog("修改数据源")
			event.stopPropagation()
		},
		remove : function(ds){	// 删除一个数据源
			Vue.http.delete(basePath+"ds/",{
				before : openLoading
			}).then(function(res){
				closeLoading()
				alertDialog(res.body.message)
				app.dsList = app.dsList.filter(function(item){return item !== ds})
			},function(error){
				closeLoading()
			})
		}
	}
});

var databaseProductNames = {
	server : ['MySQL','Oracle','SQL Server','DB2','hive','Hbase','MongoDB'],
	file : ['Access','SQLite','Excel']
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

var addForm = new Vue({
	el : "#add-div",
	data : {
		dstype : [],
		ds : {},
		isFile : true
	},
	created : function(){
		for(var i in databaseProductNames.server){
			this.dstype.push(databaseProductNames.server[i])
		}
		for(var i in databaseProductNames.file){
			this.dstype.push(databaseProductNames.file[i])
		}
	},
	watch : {
		"ds.databaseName" : function(newVal,oldVal){
			this.ds.isFile = isFile(newVal)
		},
	},
	updated : function(){
		if(this.ds.isFile){// 如果ds.isFile为true，重新渲染文件上传按钮
			drawUpload()
		}
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
		 success : drawForm, // 模态框打开
		 end : function(){$("#add-div").hide();},// 模态框销毁事件
	 })
	 
	 function submit(index,layero){
		 // 验证表单的完整性
		 var f = $("#addForm").valid();
		 if(!f) return f;// 验证不通过
		 
		 layer.confirm("您确定要提交吗？",{
			 btn : ["取消","确定"],
			 skin: 'layui-layer-lan'
		 },function(i,l){
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
		 // 验证通过
		 if(addForm.ds.id){// 修改
			 Vue.http.put(basePath+"ds/",addForm.ds,options).then(success,fail)
		 }else{// 添加
			 Vue.http.post(basePath+"ds/",addForm.ds,options).then(success,fail)
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
					 
				 }else{// 添加成功
					 app.dsList.push(res.body.data) //4、 在页面显示添加结果
				 }
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
			cloner.reset(addForm.ds); // 在关闭模态框是将表单关联的数据恢复到编辑之前
			// TODO 请求后台删除上传的数据
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
			done : function(res){
				console.log(res)
				if(res.code ===1){
					Vue.set(addForm.ds,'association',res.data);
					layer.msg("上传成功");
				}
				else{
					alertDialog(res.message);
				}
			},
			error : function(error){
				alertDialog("网络异常，上传失败")
			}
		})
	})
}
window.onunload = function(){
	alert("页面销毁")
}


 
 // 验证添加数据源的表单
 $("#addForm").validate({
	 rules : {
		 dsname : {
			 required : true,
			 maxlength : 10
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
			 maxlength : "数据源名称不能超过10个字符"
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
 
 