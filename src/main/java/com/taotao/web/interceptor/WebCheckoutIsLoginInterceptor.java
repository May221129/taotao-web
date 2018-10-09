package com.taotao.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.taotao.common.interceptor.SupCheckoutIsLoginInterceptor;
import com.taotao.common.util.CookieUtils;

/**
 * 拦截器.
 * 1.将所有请求都拦截下来；
 * 2.判断请求是否访问“带@CheckoutToken注解”的handler，
 * 		若有，则通过调用SupCheckoutIsLoginInterceptor父类中的preHandle()方法做登录校验；
 * 		若没有，则直接放行，继续访问controller层的handler方法。
 */
@Component
public class WebCheckoutIsLoginInterceptor extends SupCheckoutIsLoginInterceptor{

	@Value("${TAOTAO_SSO_URL}")
	private String TAOTAO_SSO_URL;

	@Value("${LOGIN_URL}")
	private String LOGIN_URL;

	private static final String REAL_REQUEST_URL = "REAL_REQUEST_URL";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(super.preHandle(request, response, handler)){//true：无@CheckoutToken注解，或已登录：
			return true;
		}else{//false：未登录
			/* response.addHeader(REAL_REQUEST_URL, request.getRequestURL().toString());//将真实请求的url放入response的请求头中。
			 * 放入header不行，因为这里进行了重定向，放入response中的数据会丢失。
			 * 放入cookie中就很合适：cookie是taotao.com这个二级域名共用的，sso端或其他端也能获取到。 */
			CookieUtils.setCookie(request, response, REAL_REQUEST_URL, request.getRequestURL().toString());
			response.sendRedirect(TAOTAO_SSO_URL + LOGIN_URL);
			return false;
		}
	}
}
