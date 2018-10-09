package com.taotao.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.httpClient.HttpResult;
import com.taotao.common.service.ApiService;
import com.taotao.sso.query.bean.User;
import com.taotao.web.bean.Order;

@Service
public class OrderService {
	
	@Autowired
	private ApiService apiService;
	
	@Value("${TAOTAO_ORDER_URL}")
	private String TAOTAO_ORDER_URL;
	
	@Value("${API_CREAD_ORDER_URL}")
	private String API_CREAD_ORDER_URL;
	
	@Value("${API_QUERY_ORDER_URL}")
	private String API_QUERY_ORDER_URL;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public String submitOrder(Order order, User user) {
		try {
			//填充order中的用户信息：
			order.setUserId(user.getId());
			order.setBuyerNick(user.getUsername());
			String url = TAOTAO_ORDER_URL + API_CREAD_ORDER_URL;
			HttpResult httpResult = apiService.doPostHaveJsonParams(url, MAPPER.writeValueAsString(order));
			
			if(httpResult.getCode().intValue() == 200){
				return httpResult.getBody();//body是指访问Order端的创建订单接口后，返回的订单编号。
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Order queryOrderByOrderId(String orderId) {
		String url = TAOTAO_ORDER_URL + API_QUERY_ORDER_URL + orderId;
		try {
			String jsonData = this.apiService.doGet(url);
			if(StringUtils.isNotEmpty(jsonData)){
				return MAPPER.readValue(jsonData, Order.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
