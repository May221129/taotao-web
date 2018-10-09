package com.taotao.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.taotao.common.annotation.CheckoutToken;
import com.taotao.common.threadLocal.UserThreadLocal;
import com.taotao.sso.query.bean.User;
import com.taotao.web.bean.Cart;
import com.taotao.web.bean.Item;
import com.taotao.web.bean.Order;
import com.taotao.web.service.CartService;
import com.taotao.web.service.ItemService;
import com.taotao.web.service.OrderService;

@RequestMapping("order")
@Controller
public class OrderController {
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CartService cartService;
	
	/**
	 * 订单确认
	 * @param itemId
	 * @return mv
	 */
	@CheckoutToken
	@RequestMapping(value = "{itemId}", method = RequestMethod.GET)
	public ModelAndView itemToOrder(@PathVariable("itemId")Long itemId){
		ModelAndView mv = new ModelAndView("order");
		Item item = this.itemService.queryItemById(itemId);
		mv.addObject("item", item);
		return mv;
	}
	
	/**
	 * 将购物车中的商品进行下单
	 * 注意：这里做的是对购物车中所有商品一起购买，而非只对选中商品进行购买。
	 * 	        所以前端是没有传递商品id等参数过来的，而是通过数据库查cart表。
	 * @param itemId
	 * @return mv
	 */
	@CheckoutToken
	@RequestMapping(value = "create", method = RequestMethod.GET)
	public ModelAndView cartToOrder(){
		ModelAndView mv = new ModelAndView("order-cart");
		List<Cart> carts = this.cartService.queryCartList();
		mv.addObject("carts", carts);
		return mv;
	}
	
	/**
	 * 提交订单到订单系统。
	 * 提问：前端提交订单的时候，就是提交json数据过来，order系统对应的接口也是接收json数据的。
	 * 	那么，这里为什么不能直接接收json数据，再传给Order系统去做订单的新增；
	 * 	而是将json转为order对象，再将order对象转为json数据传给order系统。
	 * 答：因为我们需要对前端传过来的数据进行校验，转为order对象时，order对象有做数据校验。
	 * 	确保我们接收的数据是对的，再传给order系统。
	 * @param order
	 * @param request ==> 现在该入参可以不用了，因为在拦截器中将user对象放入了UserThreadLocal中，而非放入request中。
	 */
	@CheckoutToken
	@RequestMapping(value = "submit", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> submitOrder(Order order){
		//拦截器中将user对象放入了UserThreadLocal中，这里可以通过该对象直接获取：
		User user = UserThreadLocal.get();
		//提交订单给order系统：
		String orderId = this.orderService.submitOrder(order, user);
		Map<String, Object> result = new HashMap<>();
		if(StringUtils.isNotEmpty(orderId)){//订单创建成功
			result.put("data", orderId);
			result.put("status", 200);
		}else{//订单创建失败
			result.put("status", 500);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	/**
	 * 完成订单确认后，跳转到成功页。
	 * 怎么确定mv中需要放入哪些数据：看成功页中需要哪些数据：/WEB-INF/views/success.jsp
	 */
	@CheckoutToken
	@RequestMapping(value = "success", method = RequestMethod.GET)
	public ModelAndView success(@RequestParam("id")String orderId){
		ModelAndView mv = new ModelAndView("success");
		Order order = this.orderService.queryOrderByOrderId(orderId);
		mv.addObject("order", order);
		//添加预计送货时间，实际实现时，该时间是由仓库所在地和买家所在地所计算出来的.
		//现在的项目中没有这些，所以我们就按照当前时间往后移5天作为预计送货时间。
		//这里就需要用到时间操作组件：joda
		mv.addObject("date", new DateTime().plusDays(5).toString("MM月dd日"));
		return mv;
	}
}
