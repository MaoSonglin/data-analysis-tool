	Vue.http.options.withCredentials = true;
	Vue.http.options.emulateJSON = true;
	var headerNav = new Vue({
		el : "#header",
		data : {
			searchMsg : null
		},
		methods : {
			search :function(event){
				alert(JSON.stringify(event))
			}
		}
	})
	var headerVue = new Vue({
		el : "#admin-offcanvas",
		data : {
			menus : [],
			tabs : []
		},
		created : function(){
			this.$http.get(basePath+"menu/").then(function(res){
				// alert(JSON.stringify(res.body))
				this.menus = res.body.data
			},function(error){
				console.log(error)
			})
		},
		methods : {
			to:function(menu,event){
				if(menu.link !== "#"){
					// TODO 添加菜单
					addTab(menu.link,menu.text)
					event.stopPropagation()
				}
			}
		}
	})
	var adminContentBody = new Vue({
		el : "#admin-content-body",
		data : {
			firstpage : null
		},
		created: function(){
			
			this.$http.get("frame/first.html").then(function(res){
				this.firstpage = res.body
			})
		}
	})
	var tabCounter = 0;
	var $tab = $('#doc-tab-demo-1');
	var $nav = $tab.find('.am-tabs-nav');
	var $bd = $tab.find('.am-tabs-bd');
	var tabs = new Array("首页");
	function addTab(link,title) {
		var index = Index(title)
		if(index === -1){
			var nav = '<li><a href="javascript: void(0)">' + 
			title + '<span class="am-icon-close"></span></a></li>';
			var content = '<div class="am-tab-panel"><iframe id="iframe_'+title+'" src="'+link+'" width="100%" height="98%"></iframe></div>';
			$nav.append(nav);
			$bd.append(content); 
			tabs.push(title)
			index = tabs.length - 1
			$tab.tabs('refresh')
		}else{
			document.getElementById("iframe_"+title).src=link;
		}
		$tab.tabs('open',index)
		$tab.tabs('refresh');
	}

	function Index(title){
		for(let i = 0; i < tabs.length; i++){
			if(tabs[i] === title){
				return i
			}
		}
		return -1
	}

	// 动态添加标签页
	// 		$('.admin-sidebar-list li').on('click', function(e) {
	// 		  addTab();
	// 		});

	// 移除标签页
	$nav.on('click', '.am-icon-close', function() {
	  var $item = $(this).closest('li');
	  var index = $nav.children('li').index($item);
	  $item.remove();
	  $bd.find('.am-tab-panel').eq(index).remove();
	  
	  $tab.tabs('open', index > 0 ? index - 1 : index + 1);
	  $tab.tabs('refresh');
	  
	  // 修改标签页的标头
	  var tmp = new Array()
	  for(let i = 0; i < tabs.length; i++){
		  if(i !== index){
			  tmp.push(tabs[i])
		  }
	  }
	  tabs = tmp
	});
	
	var dom = document.querySelector("#admin-offcanvas")
	Vue.component("menu",{
		template:dom.outerHTML
	})