package com.taotao.web.interceptor;

import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.common.service.ApiService;
import com.taotao.common.util.CookieUtils;
import com.taotao.web.annotation.IgnoreCheckLogin;
import com.taotao.web.bean.User;
import com.taotao.web.threadLocal.UserThreadLocal;

/**
 * 拦截器：将所有需要做登录校验的请求都拦截下来，在这里统一做登录校验。 该拦截器中写了做登录验证的两种实现方式：preHandle和preHandle2.
 * 1.preHandle：C端自己连接Redis去完成token的校验，以验证用户是否登录。 
 * 	优点：校验速度快。
 * 	缺点：如果token数据很安全，那么就不能让每个系统都直连Redis去做token的校验，而应该交给sso端专业做。
 * 2.preHandle2：C端发送请求到sso端，让sso端去做token的校验。 
 * 	优点：专事专做。 
 * 	缺点：多了一层网络请求，速度慢。
 */
@Component
public class CheckLoginInterceptor implements HandlerInterceptor {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;// 用于操作Redis

	public static final String COOKIE_TOKEN = "TAOTAO_TOKEN";// cookie中token的key

	private static final long REDIS_SECONDS = 60 * 30;// token放入Redis后的存活时间

	private static final ObjectMapper MEPPER = new ObjectMapper();// 用于序列化和反序列化

//	private static final String USER_FROM_REDIS = "USER_FROM_REDIS";// 从Redis中通过token获取的User对象，放入request中的命名。

	private static final int TOKEN_FROM_COOKIE_SECONDS = 60 * 30;// cookie中token的存活时长：30分钟
	
	@Value("${TAOTAO_SSO_URL}")
	private String TAOTAO_SSO_URL;

	@Value("${LOGIN_URL}")
	private String LOGIN_URL;

	@Value("${CHECKOUT_TOKEN_URL}")
	private String CHECKOUT_TOKEN_URL;

	@Autowired
	private ApiService apiService;

	private static final String REAL_REQUEST_URL = "REAL_REQUEST_URL";

	/**
	 * 验证是否登录的第一种实现方式：C端自己连接Redis进行token的验证。
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
			if (null != ((HandlerMethod) handler).getMethodAnnotation(IgnoreCheckLogin.class)) {
				return true;
			} else {
				if (null != request.getCookies()) {// 防止用户恶意删除cookie，造成空指针异常。
					// 看cookie中是否有token：
					String token = CookieUtils.getCookieValue(request, COOKIE_TOKEN);
					if (StringUtils.isNotEmpty(token)) {
						// 看Redis中是否有该用户token对应的user记录：
						String jsonData = this.stringRedisTemplate.opsForValue().get(RedisKeyConstant.getToken(token));
						if (StringUtils.isNotEmpty(jsonData)) {// 有：
							User user = MEPPER.readValue(jsonData, User.class);
							this.stringRedisTemplate.expire(RedisKeyConstant.getToken(token), REDIS_SECONDS, TimeUnit.SECONDS);
							this.stringRedisTemplate.expire(RedisKeyConstant.getUserId(user.getId()), REDIS_SECONDS, TimeUnit.SECONDS);
							CookieUtils.setCookie(request, response, COOKIE_TOKEN, token, TOKEN_FROM_COOKIE_SECONDS);
							//将从redis中获取的user对象放入request中，方便后面执行的handler获取：
//							request.setAttribute(USER_FROM_REDIS, user);
							//将从redis中获取的user对象放入自定义的UserThreadLocal的容器中，该容器的作用见该UserThreadLocal类：
							UserThreadLocal.set(user);
							return true;
						}
					}
				}
			}
		}
		// 放入header不行，因为这里进行了重定向，放入response中的数据会丢失：
		// response.addHeader(REAL_REQUEST_URL, request.getRequestURL().toString());//将真实请求的url放入response的请求头中。
		// 放入cookie中就很合适：cookie是taotao.com这个二级域名共用的，sso端或其他端也能获取到：
		CookieUtils.setCookie(request, response, REAL_REQUEST_URL, request.getRequestURL().toString());
		response.sendRedirect(TAOTAO_SSO_URL + LOGIN_URL);
		return false;
	}

	/**
	 * 验证是否登录的第二种实现方式：让sso端完成token的验证。
	 */
	public boolean preHandle2(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
			if (null != ((HandlerMethod) handler).getMethodAnnotation(IgnoreCheckLogin.class)) {
				return true;
			} else {
				for (Cookie cookie : request.getCookies()) {
					// 看cookie中是否有token：
					if (cookie.getName().equalsIgnoreCase(COOKIE_TOKEN)) {
						String result = apiService.doGet(TAOTAO_SSO_URL + CHECKOUT_TOKEN_URL);
						if (null != result) {
							request.setAttribute("user", MEPPER.readValue(result, User.class));
							return true;
						}
					}
				}
			}
		}
		response.sendRedirect(TAOTAO_SSO_URL + LOGIN_URL);
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		//因为线程可能是从线程池中获取的，是可复用的，所以为了防止线程中还保存着上一次留下的user对象，而导致存储在该容器中的user对象混乱，所以先进行置空。
		UserThreadLocal.set(null);
	}

}
