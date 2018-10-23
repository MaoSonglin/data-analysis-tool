Vue.http.options.emulateJSON=true
Vue.http.options.withCredentials=true

var application = new Vue({
	el : "#table-div",
	data : {
		tables : [],
		page : {curPage:0,pageSize:5}
	},
	created : function(){
		getTables(this.page,this)
	},
	updated : function(){
		showPage(this.page)
	},
	methods : {
		search:function(){
			this.page.curPage = 0
			this.page.pageSize = 5
			getTables(this.page,this)
		}
	}
})
function getTables(page,obj){
	Vue.http.get(basePath+"tab",{
		before : openLoading,
		params : page
	}).then(function(res){
		closeLoading()
		if(res.body.code == 1){
			Vue.set(obj,'tables',res.body.data.content)
			page.totalCount = res.body.data.totalElements
			page.totalPage = res.body.data.totalPages
		}else{
			layer.msg("网络异常")
		}
	},function(error){
		layer.msg("网络异常")
	})
}

function showPage(page){
	layui.use('laypage',function(){
		var laypage= layui.laypage
		var limits = new Array(5,10,15,20,25)
		laypage.render({
			elem: 'page'
			,count: page.totalCount,
			curr : page.curPage+1,
			limit : page.pageSize,
			limits : limits
			,layout: ['count', 'prev', 'page', 'next', 'limit', 'refresh', 'skip']
			,jump: function(obj,first){
				if(!first){
					page.curPage = obj.curr -1;
					page.pageSize = obj.limit;
					getTables(page,application);
				}
			}
		});
	})
}