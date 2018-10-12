function showDialog(title,message){
	alert(title,message)
}

function pagination(selector,options,jump){
	
	let $ul = $("<ul class='pagination'></ul>")
	let $pre = $("<li><a href='javascript:;'>&laquo;</a></li>")
	$ul.append($pre)
	let $next = $("<li><a href='javascript:;'>&raquo;</a></li>")
	
	let totalPage = Math.ceil(options.totalCount / options.pageSize)
	options.totalPage = totalPage
	let arrays = new Array($pre)
	for(let i = 1; i <= totalPage; i++){
		let $li = $("<li><a href='javascript:;'>"+i+"</a></li>")
		if(i == options.curPage){
			$li.addClass("active")
		}
		$li.bind("click",function(event){
			if(i != options.curPage){
				changeActive(i)
				options.curPage = i
				if(jump){
					jump(options)
				}
				changeDisabled(i)
			}
		})
		arrays.push($li)
		$ul.append($li)
	}
	$pre.click(function(event){
		if(options.curPage>1){
			options.curPage--
		}
		changeIndex(options.curPage)
	})
	$next.click(function(event){
		if(options.curPage < totalPage){
			options.curPage ++
		}
		changeIndex(options.curPage)
	})
	$ul.append($next)
	$(selector).append($ul)
	
	function changeIndex(index){
		changeDisabled(index)
		changeActive(index)
		if(jump){
			jump(options)
		}
	}
	
	function changeDisabled(i){
		if(i==1){
			$pre.addClass("disabled")
		}else{
			$pre.removeClass("disabled")
		}
		if(i==totalPage){
			$next.addClass("disabled")
		}else{
			$next.removeClass("disabled")
		}
	}
	function changeActive(i){
		for(let index = 1; index < arrays.length; index++){
			arrays[index].removeClass("active")
		}
		arrays[i].addClass("active")
	}
	if(jump){
		jump(options)
	}
}

window.alert = function(title,str) {
	var alertBox = document.createElement("div");
	alertBox.id="alertBox";
	alertBox.style.position = "absolute";
	alertBox.style.width = "400px";
	alertBox.style.background = "#F2F2F2";
	alertBox.style.border = "1px solid grey";
	alertBox.style.left = "50%";
	alertBox.style.top = "50%";
	alertBox.style.transform = "translate(-50%, -50%)";
	alertBox.style.textAlign = "center";
	alertBox.style.zIndex = "100";
	var strHtml = "";
	strHtml += '<div id="title">'+title+'<div id="close" onclick="certainFunc()"></div></div>';
	strHtml += '<div id="content">'+str+'</div>';
	strHtml += '<div id="certain"><input id="btn" type="button" value="确 定" onclick="certainFunc()" onhover="hoverFunc()"/></div>';
	alertBox.innerHTML = strHtml;
	document.body.appendChild(alertBox);
	var title = document.getElementById("title");
	title.style.textAlign = "left";
	title.style.marginTop = "20px";
	title.style.paddingLeft = "20px";
	title.style.height = "30px";
	title.style.fontSize = "15px";
	var close = document.getElementById("close");
	close.style.width = "16px";
	close.style.height = "16px";
	close.style.marginRight = "20px";
	close.style.background = "url('images/close.png')";
	close.style.float = "right";
	var content = document.getElementById("content");
	content.style.margin = "20px";
	content.style.fontSize = "12px";
	var certain = document.getElementById("certain");
	certain.style.position = "relative";
	certain.style.height = "50px";
	certainFunc = function() {
		alertBox.parentNode.removeChild(alertBox);
	};
	var btn = document.getElementById("btn");
	btn.style.width = "60px";
	btn.style.height = "30px";
	btn.style.background = "#cccccc";
	btn.style.border = "1px solid grey";
	btn.style.position = "absolute";
	btn.style.borderRadius = "5px";
	btn.style.right = "20px";
	btn.style.bottom = "20px";
	btn.style.marginTop = "10px";
	btn.style.cursor = "pointer";
	btn.style.color = "#333";
	hoverFunc = function() {
		btn.style.border = "1px blue solid";
	};
}
