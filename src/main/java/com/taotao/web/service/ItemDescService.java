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
import com.taotao.manage.pojo.ItemDesc;

@Service
public class ItemDescService {

	@Autowired
	private ApiService apiService;

	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private StringRedisTemplate redisTemplate;

	private static final Integer SECONDS = 60 * 60 * 24 * 7;// 数据在redis中缓存7天。

	/**
	 * 查询商品描述数据
	 */
	public ItemDesc queryItemDescByItemId(Long itemId) {
		
		// 先尝试从redis中拿数据：
		try {
			String cacheData = this.redisTemplate.opsForValue().get(RedisKeyConstant.ITEM_DESC_KEY + itemId);
			if (StringUtils.isNotEmpty(cacheData)) {
				this.redisTemplate.expire(RedisKeyConstant.ITEM_DESC_KEY + itemId, SECONDS, TimeUnit.SECONDS);
				return MAPPER.readValue(cacheData, ItemDesc.class);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			String url = TAOTAO_MANAGE_URL + "/rest/item/desc/" + itemId;
			String jsonData = this.apiService.doGet(url);
			
			try {
				this.redisTemplate.opsForValue().set(RedisKeyConstant.ITEM_DESC_KEY + itemId, jsonData);
				this.redisTemplate.expire(RedisKeyConstant.ITEM_DESC_KEY + itemId, SECONDS, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return MAPPER.readValue(jsonData, ItemDesc.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
