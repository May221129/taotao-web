package com.taotao.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.web.service.IndexService;

/**
 * 提问：为什么这里不可以写PageController类，来做页面的转发？
 */
@RequestMapping("index")
@Controller
public class IndexController {

	@Autowired
	private IndexService indexService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index() {
		
		ModelAndView mv = new ModelAndView();
		
		// 首页最大的广告：
		String indexAD1 = this.indexService.queryIndexAD1();
		mv.addObject("indexAD1", indexAD1);
		
		// 首页右上角的广告：
		String indexAD2 = this.indexService.queryIndexAD2();
		mv.addObject("indexAD2", indexAD2);
		
		return mv;
	}

}
