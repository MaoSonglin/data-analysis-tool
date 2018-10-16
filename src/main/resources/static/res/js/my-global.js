
function alertDialog(content,callback){
	layer.open({
		title : "提示",
		skin: 'layui-layer-lan layui-layer-molv',
		content : content,
		btn : ["确定"],
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
		skin: 'layui-layer-lan layui-layer-molv',
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