	var reportid = getParameter("id")
	var vue = new Vue({
		el:"#wrapper",
		data:{
			report:{},
			graphs : []
		},
		created : function (){
			// 获取报表信息
			this.$http.get(basePath+"report?id="+reportid).then(function(res){
				if(res.body.code == 0){
					layer.msg(res.body.message)
				}else{
					this.report = res.body.data
				}
			},function(error){
				layer.msg("网络错误")
			})
			// 获取报表中的图表信息
			this.$http.get(basePath+"report/graphs/"+reportid).then(function(res){
				if(res.body.code == 1)
					this.$set(this,'graphs',res.body.data)
				else{
					layer.msg(res.body.message)
				}
			},function(error){
				layer.msg("网络错误，获取图表信息失败")
			})
		},
		methods : {
			getPosition : function(graph){
				 
				var style = "position:absolute;top:"+0+"px;right:"+0+"px;bottom:"+0+"px;left:"+0+"px";
				console.log(style)
				return style
			}
		},
		updated:function(){
			for(let i in this.graphs){
				let dom = document.querySelector("#graph-"+i)
				let echart = echarts.init(dom)
				echart.setOption(JSON.parse(this.graphs[i].options))
				echart.showLoading()
				this.$http.get(basePath+"graph/data/"+this.graphs[i].id).then(function(res){
					echart.hideLoading()
					echart.setOption({
						dataset : {
							source : res.body.data
						}
					})
					if(res.body.data[0].length>5){
						var dataZoom = [
							{   // 这个dataZoom组件，默认控制x轴。
								type: 'slider', // 这个 dataZoom 组件是 slider 型 dataZoom 组件
								start: 10,      // 左边在 10% 的位置。
								end: 60         // 右边在 60% 的位置。
							}
						]
						echart.setOption({
							dataZoom : dataZoom
						})
					}
				},function(error){
					echart.hideLoading()
					layer.msg("网络异常")
				})
			}
		}
	})