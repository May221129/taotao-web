package com.taotao.web.bean;

import org.apache.commons.lang3.StringUtils;

public class Item extends com.taotao.manage.pojo.Item{

	public String[] getImages() {
		
//		if(null == super.getImage()){
//			return null;
//		}
		/*
		 * jdk提供的split性能更低：但它除了可以通过字符串进行分割，还能通过正则表达式进行分割，功能更强大。
		 * 该方法也没有进行判空，还需要自己判空。
		 */
//		return super.getImage().split(",");
		
		/*
		 * StringUtils.split的性能更高。但它只能通过字符串进行分割，功能单一。
		 * 点进这个方法去看，可以看到该方法有判空，所以前面我们自己写的判空就去注掉了。
		 */
		return StringUtils.split(super.getImage(), ',');
	}

}
