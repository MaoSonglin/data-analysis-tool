var basePath = "http://localhost:8088/"
function alertDialog(content,callback){
	layer.open({
		title : "提示",
		skin: 'layui-layer-lan',
		content : content,
		btn : ["确定"],
		shadeClose : true,
		btn1 : function(index,layero){
			layer.close(index)
			if(callback){
				callback()
			}
		}
	})
}

function confirmDialog(content,callback){
	layer.open({
		title : "提示",
		content : content,
		skin: 'layui-layer-lan',
		btn : ["取消","确定"],
		btn1 : function(index,layero){
			layer.close(index)
		},
		btn2 : function(index,layero){
			layer.close(index)
			if(callback){
				callback()
			}
		}
	})
}

var loadingIndex = null
function openLoading(){
	loadingIndex = layer.open({
		type : 3
	})
}

function closeLoading(){
	layer.close(loadingIndex)
}
/**
 * 显示一个模态框
 * @param title 模态框的标题
 * @param selector 模态框中显示的DOM元素的选择器
 * @param callback	模态框中确定按钮按下后回调函数
 * @param destory	模态框销毁时回调函数
 */
function showModalDialog(title,selector,callback,destory){
	return layer.open({
		type : 1,
		title : title,
		content : $(selector),
		skin: 'layui-layer-lan',
		area : ['600px','400px'],
		btn : ['提交','取消'],
		yes : function(index,layero){
			if(callback){
				if(callback()){
					layer.close(index);
				}
			}else{
				layer.close(index)
			}
		},
		cancel : function(index,layero){
			layer.close(index)
		},
		closeBtn : 2,
		end : function(){
			if(destory) destory()
			$(selector).hide()
		}
	})
}

	function Saver(){
		this.values = new Array()
		this.save = function(attr,target){
			if(attr instanceof Array){
				for(var i in attr){
					this.values.push({attr:attr[i],value:target[attr[i]]})
					target[attr[i]] = null
				}
			}else{
				this.values.push({attr:attr,value:target[attr]}),
				target[attr] = null
			}
		}
		this.restore = function(attr,target){
			if(attr instanceof Array){
				for(var i in attr){
					this.restore(attr[i],target)
				}
			}else{
				var tmp = new Array()
				for(var item = this.values.shift(); item ; item = this.values.shift()){
					if(item.attr == attr){
						target[attr] = item.attr
						break
					}
					tmp.unshift(item)
				}
				for(var i in tmp){
					this.values.unshift(tmp[i])
				}
			}
		}
	}
	
	
	function Cloner(target){
		this.target = new Object();
		for(var i in target){
			this.target[i] = target[i]
		}
		
		this.reset = function(target,attrs){
			if(!attrs){
				for(var i in this.target){
					target[i] = this.target[i]
				}
			}
			else if(attrs instanceof Array){
				for(var i in attrs){
					var index = attrs[i]
					this.reset(target,index)
				}
			}else{
				target[attrs] = this.target[attrs]
			}
		}
	}
	
	function tile(obj){
		var that = new Object()
		doObject(obj,"")
		function doObject(obj,attr){
			if(typeof(obj) == 'function'){
				
			}
			else if(typeof(obj) == 'string' || typeof(obj) == 'boolean' || typeof(obj) == 'number'){
				if(attr)
					that[attr] = obj
				else
					that = obj
			}else
				for(var index in obj){
					var val = obj[index]
					var x = attr
					if(isNaN(index)){
						x += x ? "."+index : index
					}else{
						x += x ? "["+index+"]" : index
					}
					doObject(val,x)
				}
		}
		
		
		return that
	}
	
	function getParameter(para){
		var reg = new RegExp("(^|&)"+para +"=([^&]*)(&|$)");
		var url = decodeURI(window.location.search)
		var r =  url.substr(1).match(reg);
		if(r!=null){
		   // return unescape(r[2]); 
		   return r[2]
		}
		return null;
	} 
	