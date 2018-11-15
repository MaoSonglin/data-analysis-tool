	Vue.component("tree",{
		props : {
			list : {
				type:Array,
				required : true
			},
			root : {
				type:Boolean,
				default : true
			},
			spread : {
				type:Boolean,
				default : false
			}
		},
		template:'<ul :class="this.spread ? \'myTree\':\' \'">'+
					'<li v-for="item in list" @click="send(item,$event)">'
					+'<i :class="icon1(item)" @click="toggle(item,$event)"> </i>'
					+'<a href="javascript:;" @dblclick="toggle(item,$event)">'
					+'<i :class="icon2(item)"> </i><cite>{{item.name}}</cite>'
					+'<tree  v-if="item.children" v-show="spread" v-bind:spread="item.spread" v-bind:root="false" v-bind:list="item.children" v-on:nodeClick="childrenNode($event)"></tree>'
					+'</a>'
					+'</li>'
					+'</ul>',
		methods:{
			toggle:function(item,event){
				if(item.spread == undefined){
					Vue.set(item,'spread',true)
				}else{
					item.spread = !item.spread;
				}
				this.spread = !this.spread
				event.stopPropagation()
				if(item.spread && (item.children == null || item.children ==undefined) && item.url){
					Vue.http.get(item.url).then(function(res){
						Vue.set(item,"children",res.body.data)
					},function(error){
						
					})
				}
			},
			icon1:function(item){ // 节点前面的三角形图标
				return item.children 
				||item.children===null
				||item.children===undefined ? "layui-icon "+(item.spread ? "layui-icon-triangle-d":"layui-icon-triangle-r") : ""
			},
			icon2 : function(item){// 文件夹节点前的图标和叶子节点前的图标
				return item.children ||item.children===null||item.children===undefined? "layui-icon layui-icon-list" : "layui-icon layui-icon-file"
			},
			send : function(item,event){
				this.$emit("treeNodeClick",item)
				event.stopPropagation()
				// app.$emit("nodeClick",item)
			},
			childrenNode:function(event){
				this.$emit("treeNodeClick",event)
				// app.$emit("nodeClick",event)
			}
		}
	})
	