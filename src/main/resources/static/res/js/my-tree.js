Vue.component("tree",{
	template : '<ul class="tree">'
				+'		<li class="tree-node" v-for="node in nodes" @dragstart="dragstart(node,$event)">'
				+'			<i :class="foldIcon(node)" @click="toggle(node,$event)"></i>'
				+'			<a href="javascript:;" @click="nodeClick(node,$event)" @dblclick="toggle(node,$event)" ><i :class="nodeIcon(node)"></i>{{node.name}}</a>'
				+'			<tree v-bind:asyn="asyn" v-bind:nodes="node.children" v-if="show(node)" v-on:nodeclick="nodeClick($event)" v-on:loadchildren="loadChildren($event)" v-on:nodedragstart="dragstart(null,$event)"></tree>'			
				+'		</li>'
				+'</ul>',
	props:{
		nodes : {
			type : Array,
			required :true
		},
		asyn : {
			type : Boolean,
			default : false
		}
	},
	created:function(){
		console.log(this.asyn)
	},
	methods:{
		show:function(node){
			if(node.children&&node.spread){
				return true;
			}else{
				return false;
			}
		},
		foldIcon : function(node){
			if( this.asyn ){
				if(node.spread){
					return "layui-icon layui-icon-triangle-d"
				}else{
					return "layui-icon layui-icon-triangle-r"
				}
			}else{
				return node.children? node.spread ? "layui-icon layui-icon-triangle-d":"layui-icon layui-icon-triangle-r":""
			}
		},
		nodeIcon : function(node){
			if(node.children){
				if(node.icon){
					return node.spread ? node.icon.open : node.icon.close;
				}else{
					return "layui-icon layui-icon-table";
				}
			}
			else{
				return "layui-icon layui-icon-file"
			}
		},
		toggle : function(node,event){
			if(this.asyn && ! node.spread && ! node.children){
				Vue.set(node,"children",[])
				this.$emit("loadchildren",node)
				this.requested = true;
			}
			if(node.spread == undefined){
				Vue.set(node,'spread',false)
			}else{
				node.spread = !node.spread
			}
			event.stopPropagation()
		},
		nodeClick : function(node,event){
			this.$emit("nodeclick",node)
		},
		loadChildren : function(event){
			this.$emit("loadchildren",event)
		},
		dragstart : function(node,event){ // 向父容器发送拖动事件的消息
			if(node){
				event.nodeData = node;
			}
			this.$emit("nodedragstart",event)
			if(event) event.stopPropagation()
		}
	}
})