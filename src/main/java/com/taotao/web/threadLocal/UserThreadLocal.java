package com.taotao.web.threadLocal;

import com.taotao.web.bean.User;

/**
 * UserThreadLocal的作用：将CheckLoginInterceptor拦截器中获取的user对象，
 * 	放入当前请求的线程中，以便拦截器之后的handler可以获取user对象来使用。
 * tomcat服务器通常都是为一个请求绑定一个线程，当然，线程可以复用，可以从线程池中获取，
 * 	这因为这一点，所以将user对象放入THREAD_LOCAL容器前，最好先将容器置空：UserThreadLocal.set(null)。
 */
public class UserThreadLocal {
	
	/**
	 * 这里的THREAD_LOCAL是个容器，它随着线程的创建而创建，随着线程的销毁而销毁。
	 */
	private static final ThreadLocal<User> THREAD_LOCAL = new ThreadLocal<>();
	
	//把构造方法私有化，外面不能去new该UserThreadLocal对象，只能调用它的下面的两个方法。
	private UserThreadLocal(){
		
	}
	
	public static void set(User user){
		THREAD_LOCAL.set(user);
	}
	
	public static User get(){
		return THREAD_LOCAL.get();
	}
}
