package com.taotao.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.manage.pojo.ItemDesc;
import com.taotao.web.bean.Item;
import com.taotao.web.service.ItemDescService;
import com.taotao.web.service.ItemParamItemService;
import com.taotao.web.service.ItemService;


/**
 * url:http://www.taotao.com/item/{itemId}.html
 */
@RequestMapping("item")
@Controller
public class ItemController {
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private ItemDescService itemDescService;
	
	@Autowired
	private ItemParamItemService itemParamItemService;
	
	/**
	 * 查询商品详情
	 * 1.怎么确定返回值是ResponseEntity、还是ModelAndView、还是String、还是其他呢？
	 * 答：通过item.jsp页面可以发现，里面有用到el表达式和item对象(既有页面又有模型师数据)，所有是返回ModelAndView。
	 * 2.
	 */
	@RequestMapping(value="{itemId}", method = RequestMethod.GET)
	public ModelAndView itemDetail(@PathVariable("itemId")Long itemId){
		
		//通过这行代码知道转到item.jsp页面的：
		ModelAndView mv = new ModelAndView("item");
		
		//设置模型数据：
		Item item = this.itemService.queryItemById(itemId);
		mv.addObject("item", item);
		
		//添加商品描述：
		ItemDesc itemDesc = this.itemDescService.queryItemDescByItemId(itemId);
		mv.addObject("itemDesc", itemDesc);
		
		//添加商品规格参数：
		String itemParamItem = this.itemParamItemService.queryItemParamItemByItemId(itemId);
		mv.addObject("itemParamItem", itemParamItem);
		
		return mv;
	}
}
