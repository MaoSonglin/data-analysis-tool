Vue.http.options.emulateJSON=true;
Vue.http.options.withCredentials=true;

var name=getParameter("name")
console.log(name)

var container = new Vue({
	el : "#container",
	data : {
		tables : [],
		page : {},
		fields : [],
		table : {}
	},
	created : function(){
		for(var i = 0; i < 100; i++){
			this.tables.push({name:"数据表"+i})
		}
	},
	methods : {
		showFields : function(table,event){
			var array = new Array();
			for(var i = 0; i < 100; i++){
				array.push(table.name+"字段"+i);
			}
			this.$set(this,"fields",array);
			this.table = table;
		}
	}
})