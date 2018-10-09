var TT = TAOTAO = {
	checkLogin : function(){
		//$.cookie("TAOTAO_TOKEN")之所以可以这里用，
		//是因为我们使用了第三方的jQuery的插件jquery.cookie.js(/taotao-web/src/main/webapp/js/jquery.cookie.js)
		//如果不使用这个插件，我们也是可以获取到cookie的，这个插件只是帮我们做了封装。
		//自己写更麻烦。
		var _token = $.cookie("TAOTAO_TOKEN");
		if(!_token){
			return ;
		}
		$.ajax({
			//用了dubbo后，最终的url：
			url : "http://ssoquery.taotao.com/user/"  + _token,
			//sso端的接口被我改了，所有这里的url也跟着改变：
//			url : "http://sso.taotao.com/service/user/checkoutToken",
		//	url : "http://sso.taotao.com/service/user/" + _token,
			dataType : "jsonp",
			type : "GET",
			success : function(data){
				var html = data.username+"，欢迎来到淘淘！<a href=\"http://sso.taotao.com/user/logout.html\" class=\"link-logout\">[退出]</a>";
				$("#loginbar").html(html);
			}
		});
	}
}

$(function(){
	// 查看是否已经登录，如果已经登录查询登录信息
	TT.checkLogin();
});