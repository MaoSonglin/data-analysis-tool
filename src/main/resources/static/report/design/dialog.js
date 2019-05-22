Vue.component('chooseColumn', {
	template: '<div class="modal fade" :id="id"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><button type="button"class="close"data-dismiss="modal">&times;</button><h3 class="modal-title">添加字段</h3></div><div class="modal-body"><form action="#"class="form-horizontal"><div class="form-group"><label for=""class="control-label col-xs-3">选择数据表</label><div class="col-xs-7"><select name=""id=""class="form-control"@change="getField($event)"><option value="">请选择数据表</option><option:value="table.id"v-for="table in tables"v-html="table.chinese?table.chinese:table.name"></option></select></div></div><div class="form-group"><label for=""class="control-label col-xs-3">选择字段</label><div class="col-xs-7"><select class="form-control"name=""id=""class="control-label"v-model="columnIndex"><option:value="index"v-for="(field,index) in columns"v-html="field.chinese?field.chinese:field.name"></option></select></div></div></form></div><div class="modal-footer"><button class="btn btn-default"@click="confirm()">确定</button><button class="btn btn-default"data-dismiss="modal">取消</button></div></div></div></div>',
	props: {
		id: {
			type: String,
			default: 'addField'
		}
	},
	data: {
		tables: [],
		columns: [],
		columnIndex: 0
	},
	created: function() {
		this.$http.get(basePath + 'pkg/tab/' + getParameter('pkgId')).then(res => {
			this.$set(this, 'tables', res.body.data)
		})
	},
	methods: {
		getField: function(event) {
			this.$http.get(basePath + "vt/vc/" + event.target.value).then(function(res) {
				this.$set(this, "columns", res.body.data)
			})
		},
		confirm: function() {
			// alert(this.columnIndex)
			$('#'+this.id).modal("hide")
			this.$emit("send", this.columns[this.columnIndex])
			this.columnIndex = 0
		}
	}
})
