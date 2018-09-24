package com.taotao.web.service;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.common.service.ApiService;
import com.taotao.web.bean.Item;

@Service
public class ItemService {

	@Autowired
	private ApiService apiService;

	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private static final Integer SECONDS = 60 * 60 * 24 * 7;//数据在redis中缓存7天。
	
	/**
	 * 根据商品id查询商品详情。<br>
	 * C端去访问B端接口，B端连接数据库将查询的数据返回给C端。
	 */
	public Item queryItemById(Long itemId) {
		//1.先尝试从redis中拿数据：
		try {
			String cacheData = this.redisTemplate.opsForValue().get(RedisKeyConstant.ITEM_KEY + itemId);
			if(StringUtils.isNotEmpty(cacheData)){
				this.redisTemplate.expire(RedisKeyConstant.ITEM_KEY + itemId, SECONDS, TimeUnit.SECONDS);
				return MAPPER.readValue(cacheData, Item.class);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		//2.连接数据库查询数据：
		try {
			String url = TAOTAO_MANAGE_URL + "/rest/api/item/" + itemId;
			String jsonData = this.apiService.doGet(url);
			if (StringUtils.isEmpty(jsonData)) {
				return null;
			}
			
			//3.将数据放入到Redis中：
			try {
				this.redisTemplate.opsForValue().set(RedisKeyConstant.ITEM_KEY + itemId, jsonData);
				this.redisTemplate.expire(RedisKeyConstant.ITEM_KEY + itemId, SECONDS, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//4.将json数据反序列化为item对象：
			return MAPPER.readValue(jsonData, Item.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

}
