Vue.http.options.withCredentials=true
Vue.http.options.emulateJSON=true



var validateOption = {
	onkeyup : false,
	rules : {
		name : {required : true,validateName : true, rangelength : [4,20]},
		chinese : {maxlength: 50},
		remask : {maxlength : 100},
		formula : {required : true}
	},
	messages : {
		name : {required : "请输入字段名称",rangelength : "字段名称能少于4个字符，不能多于20个字符"},
		chinese : {maxlength : "中文名称不能大于50个字符"},
		remask : {maxlength:"描述信息不能多于100个字符"},
		formula : {required : "请选择字段"}
	},
	errorPlacement : function(error,element){
		layer.tips(error.html(),element[0],{
			tips : 3,
			tipsMore: true
		})
	},
	submitHandler: function(){
		vue.save()
	}
}

let vue = new Vue({
	el : "#分类区",
	data : {
		tables : [], // 数据表集合
		table : {} ,// 被选择的数据表
		field : {},
		alogrithm : ""
	},
	created : function(){
		try{
			this.tables = JSON.parse(sessionStorage.tables)
		}catch(e){
			console.log(e)
		}
		this.$on("fileInfo",function(res){
			this.file = res.data
		})
	},
	methods : {
		changeTable : function(event){
			let index = event.target.value
			if(!isNaN(index)){
				this.table = this.tables[index]
				this.$http.get(basePath+"vt/vc/"+this.table.id).then(function(res){
					this.$set(this.table,"columns",res.body.data)
				})
			}
		},
		save : function(){
			if(!this.file){
				layer.msg("亲上传分类标准")
				return 
			}
			this.field.table = JSON.parse(sessionStorage.table)
			param = tile(concat(this.field,{categoryFile:this.file,classifier:"dat.util.RondomClassify"}))
			this.$http.post(basePath+"vc/add/classify",param).then(function(res){
				layer.msg(res.body.message)
				if(res.body.code){
					this.field = {}
					delete this.file
					setTimeout(function(){
						if(window.parent.closeActive){
							window.parent.closeActive()
						}else{
							window.close()
						}
					},2000)
				}
			})
		},
		setField : function(){
			let id = this.field.formula
			for(let i in this.table.columns){
				if(this.table.columns[i].id == id){
					this.field.typeName = this.table.columns[i].typeName
					this.field.state = this.table.columns[i].state
				}
			}
		}
	},
	mounted : function(){
		$("#form1").validate(validateOption)
	},
	updated : function(){
		$("#form1").validate(validateOption)
	}
})

$.validator.addMethod("validateName",function(value,element,param){
	return /[_a-zA-Z][a-zA-Z0-9_]*/.test(value)
},"字段名称只能包含英文字符、数字和下划线，且不能以数字开始")



$("#form1").validate(validateOption)

