Vue.http.options.withCredentials = true
Vue.http.options.emulateJSON = true



var validateOption = {
	onkeyup: false,
	rules: {
		name: {
			required: true,
			validateName: true,
			rangelength: [4, 20]
		},
		chinese: {
			maxlength: 50
		},
		remask: {
			maxlength: 100
		},
		formula: {
			required: true,
			remote: {
				url: basePath + 'vc/formula',
				type: 'GET',
				dataType: 'json',
				data: {
					formula: function() {
						return $("#formula2").val()
					}
				},
				dataFilter: function(data){
					data = JSON.parse(data)
					return data.code === 1
				}
			}
		}
	},
	messages: {
		name: {
			required: "请输入字段名称",
			rangelength: "字段名称能少于4个字符，不能多于20个字符"
		},
		chinese: {
			maxlength: "中文名称不能大于50个字符"
		},
		remask: {
			maxlength: "描述信息不能多于100个字符"
		},
		formula: {
			required: "请选择字段",
			remote: '公式不合法'
		}
	},
	errorPlacement: function(error, element) {
		layer.tips(error.html(), element[0], {
			tips: 3,
			tipsMore: true
		})
	},
	submitHandler: function() {
		vue.save()
	}
}

let vue = new Vue({
	el: "#分类区",
	data: {
		tables: [], // 数据表集合
		table: {}, // 被选择的数据表
		field: {},
		alogrithm: ""
	},
	created: function() {
		try {
			this.tables = JSON.parse(sessionStorage.tables)
		} catch (e) {
			console.log(e)
		}
		this.$on("fileInfo", function(res) {
			this.file = res.data
		})
	},
	methods: {
		changeTable: function(event) {
			let index = event.target.value
			if (!isNaN(index)) {
				this.table = this.tables[index]
				this.$http.get(basePath + "vt/vc/" + this.table.id).then(function(res) {
					this.$set(this.table, "columns", res.body.data)
				})
			}
		},
		save: function() {
			if (!this.file) {
				layer.msg("请上传分类标准")
				return
			}
			this.field.table = JSON.parse(sessionStorage.table)
			param = tile(concat(this.field, {
				categoryFile: this.file,
				classifier: "dat.util.RondomClassify"
			}))
			this.$http.post(basePath + "vc/add/classify", param).then(function(res) {
				layer.msg(res.body.message)
				if (res.body.code) {
					this.field = {}
					delete this.file
					setTimeout(function() {
						if (window.parent.closeActive) {
							window.parent.closeActive()
						} else {
							window.close()
						}
					}, 2000)
				}
			})
		},
		setField: function() {
			let id = this.field.formula
			for (let i in this.table.columns) {
				if (this.table.columns[i].id == id) {
					this.field.typeName = this.table.columns[i].typeName
					this.field.state = this.table.columns[i].state
				}
			}
		}
	},
	mounted: function() {
		$("#form1").validate(validateOption)
	},
	updated: function() {
		$("#form1").validate(validateOption)
	}
})

$.validator.addMethod("validateName", function(value, element, param) {
	return /[_a-zA-Z][a-zA-Z0-9_]*/.test(value)
}, "字段名称只能包含英文字符、数字和下划线，且不能以数字开始")



$("#form1").validate(validateOption)

let vue2 = new Vue({
	el: "#普通区",
	data: {
		tables: [],
		column: {},
		array: [],
		stack: [],
		formula: "",
		formulaStack: []
	},
	created: function() {
		try {
			this.tables = JSON.parse(sessionStorage.tables)
		} catch (e) {
			console.log(e)
		}
		this.$on("send", function(column) {
			if (!this.column.typeName) {
				this.column.typeName = column.typeName
			} else {
				if (column.typeName == 'String' && this.column.typeName == 'Number') {
					this.column.typeName = 'String'
				}
			}
			this.formula += (column.chinese ? column.chinese : column.name)
			this.formulaStack.push(column.chinese ? column.chinese : column.name)
			this.stack.push("(" + column.formula + ")")
			// this.stack.push(column.formula)
			this.$http.get(basePath + "vc/tab/" + column.id).then(function(res) {
				for (let i in res.body.data) {
					this.array.push(res.body.data[i])
				}
			})
		})
	},
	methods: {
		save: function() {
			if ($("#form2").valid()) {
				this.column.refColumns = this.array
				this.column.formula = this.getFormula()
				this.column.state = 1
				this.column.table = JSON.parse(sessionStorage.table)
				alert(JSON.stringify(this.column))
				layer.open({
					title: "确认",
					content: "您确认要提交吗？",
					btn: ["确定", "取消"],
					btn1: (index, layero) => {
						layer.close(index)
						this.$http.post(basePath + "vc/add", tile(this.column)).then(function(res) {
							layer.msg(res.body.message)
							if (res.body.code == 1) {
								setTimeout(function() {
									if (window.parent.closeActive) {
										window.parent.closeActive()
									} else {
										window.close()
									}
								}, 2000)
							}
						})
					}
				})
			}
		},
		input: function(e) {
			// if(key)
			// console.log(e)
			if (e.target.nodeName === 'TD') {
				switch (e.target.innerHTML) {
					case '←':
						this.stack.pop()
						this.formulaStack.pop() 
						break;
					default:
						this.stack.push(e.target.innerHTML)
						this.formulaStack.push(e.target.innerHTML)
						break;
				}
				this.formula = this.formulaStack.join('')
			}
			// this.stack.push(e.key)
		},
		input2: function(e) {
			this.stack.push(e.key)
		},
		getFormula: function() {
			return this.stack.join('')
		}
	},
	mounted: function() {
		$("#form2").validate(validateOption)
	},
	watch: {
		"formula": function(newVal, oldVal) {
			// 			if(newVal.length > oldVal.length){
			// 				this.stack.push(newVal.substring(oldVal.length))
			// 			}else{
			// 				this.stack.pop()
			// 			}
			// 			this.column.formula = this.stack.join('');

		}
	}
})

new Vue({
	el: "#addField",
	data: {
		tables: [],
		columns: [],
		columnIndex: 0
	},
	created: function() {
		try {
			this.tables = JSON.parse(sessionStorage.tables)
		} catch (e) {
			console.log(e)
		}
	},
	methods: {
		getField: function(event) {
			this.$http.get(basePath + "vt/vc/" + event.target.value).then(function(res) {
				this.$set(this, "columns", res.body.data)
			})
		},
		confirm: function() {
			// alert(this.columnIndex)
			$("#addField").modal("hide")
			vue2.$emit("send", this.columns[this.columnIndex])
			this.columnIndex = 0
		}
	}
})


var v2 = {
	onkeyup: false,
	rules: {
		name: {
			required: true,
			validateName: true,
			rangelength: [4, 20]
		},
		chinese: {
			maxlength: 50
		},
		remask: {
			maxlength: 100
		},
		formula: {
			required: true
		}
	},
	messages: {
		name: {
			required: "请输入字段名称",
			rangelength: "字段名称能少于4个字符，不能多于20个字符"
		},
		chinese: {
			maxlength: "中文名称不能大于50个字符"
		},
		remask: {
			maxlength: "描述信息不能多于100个字符"
		},
		formula: {
			required: "请选择字段"
		}
	},
	errorPlacement: function(error, element) {
		layer.tips(error.html(), element[0], {
			tips: 3,
			tipsMore: true
		})
	},
	submitHandler: function() {
		vue.save()
	}
}
