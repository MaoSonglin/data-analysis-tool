

Vue.component("category",{
	template : '<div :class="category.style">'+
			'<table class="layui-table" lay-size="sm" lay-skin="line" style="font-size:12px;margin:0px 0px">'+
				'<colgroup><col width="30"><col width="20"><col></colgroup>'+
				'<tbody><tr>'+
					'<td align="right" ><i :class="getHint()" style="cursor:pointer" v-if="hasChildren(category)" @click="toggle(category)"></i></td>'+
					'<td style="margin:0;padding:0"><i :class="category.icon" style="cursor:pointer" @click="toggle(category)"></i></td>'+
					'<td  style="margin-left:0;padding-left:0" v-if="!category.href" v-html="category.name" @click="select(category,$event)" @dblclick="toggle(category)"></td>'+
					'<td style="margin-left:0;padding-left:0" v-if="category.href" @click="select(category,$event)" @dblclick="toggle(category)"><a v-html="category.name" :href="category.href" :target="category.target"></a></td>'+
					// '<td><i class="layui-icon layui-icon-delete"></i></td>'+
				'</tr></tbody></table>'+
			'<div style="margin:0px 0px 0px 20px" v-if="hasChildren(category)" v-show="category.open">'+
				'<category v-for="(c,j) in getChildren(category)" v-bind:index="index+j+1" v-bind:category="c" v-on:danji="itemClick(category,$event)" v-on:xuanze="itemSelect(category,$event)"></category>'+
			'</div></div>',
	props : {
		category : {
			type : Object,
			required : true
		},
		index : {
			type : Number,
			default : 1
		}
	},
	created : function() {
		if(this.category.open == null || this.category.open == undefined){
			this.category.open = false
		}
		if(!this.category.icon ){
			this.category.icon =  this.hasChildren() ? "fa fa-folder-o":"fa fa-file-text"
		}
		if(!this.category.openIcon){
			this.category.openIcon = this.category.icon
		}
		if(!this.category.closeIcon){
			this.category.closeIcon = this.category.icon
		}
		
	},
	methods : {
		select : function(category,event){
			this.$emit("xuanze",category)
			// this.$emit("danji",category)
		},
		itemClick : function(category,event){ 
			this.$emit("danji",event)
		},
		itemSelect : function(category,event){ 
			this.$emit("xuanze",event)
		},
		toggle : function(category){
			// this.$set(category,'open',! category.open)
			category.open = ! category.open
		},
		getHint : function(){
			return this.category.open ? "fa fa-angle-down" :"fa fa-angle-right"
		},
		getFirstIcon : function(){
			return this.category.open ? this.category.openIcon : this.category.closeIcon
		},
		hasChildren : function(category){
			return category.children && category.children.length 
		}
	}
})

Vue.component("tree",{
	template : '<div :class="data.style">'+
			'<template v-for="(category,index) in data.categories">'+
				'<category v-bind:category="category" v-bind:index="index" v-on:danji="itemClick(category,$event)"'+
				 'v-on:xuanze="itemSelect(category,$event)"></category>'+
			'</template>'+
		'</div>',
	props : {
		data : {
			type : Object,
			required : true
		}
	},
	methods : {
		itemClick : function(category,event){ 
			if(this.data.itemClick)this.data.itemClick(event)
		},
		itemSelect : function(category,event){
			
			if(this.data.url && ( event.children == [] || event.children == null || event.children == undefined)){
				$.ajax({
					url : this.data.url,
					method : this.data.method ? this.data.method : 'get',
					data : this.data.param,
					xhrFields : {
						withCredentials : true
					},
					success : (res)=>{
						if(this.data.request) this.data.request(res,event)
					}
				})
			}
		}
	}
})


