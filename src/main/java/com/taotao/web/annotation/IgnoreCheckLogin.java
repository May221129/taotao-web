package com.taotao.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解。
 * 作用：用于Controller中不需要验证是否已经登录了的Handler方法，加了该注解，表示忽略验证。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreCheckLogin {
	
}
