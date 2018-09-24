package com.taotao.web.service;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.common.service.ApiService;
import com.taotao.manage.pojo.ItemParamItem;

@Service
public class ItemParamItemService {

	@Autowired
	private ApiService apiService;

	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private StringRedisTemplate redisTemplate;

	private static final Integer SECONDS = 60 * 60 * 24 * 7;// 数据在redis中缓存7天。

	public String queryItemParamItemByItemId(Long itemId) {

		// 先尝试从redis中拿数据：
		try {
			String cacheData = this.redisTemplate.opsForValue().get(RedisKeyConstant.ITEM_PARAM_ITEM_KEY + itemId);
			if (StringUtils.isNotEmpty(cacheData)) {
				this.redisTemplate.expire(RedisKeyConstant.ITEM_PARAM_ITEM_KEY + itemId, SECONDS, TimeUnit.SECONDS);
				return cacheData;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			String url = TAOTAO_MANAGE_URL + "/rest/aip/item/param/item/" + itemId;
			String jsonData = this.apiService.doGet(url);
			if (StringUtils.isNoneEmpty(jsonData)) {
				// 将json数据反序列化为ItemParamItem对象：
				ItemParamItem itemParamItem = MAPPER.readValue(jsonData, ItemParamItem.class);
				// 通过itemParamItem对象拿到规格参数属性：paramData：
				String paramData = itemParamItem.getParamData();
				// 解析paramData：下面的代码是直接copy老师的
				ArrayNode arrayNode = (ArrayNode) MAPPER.readTree(paramData);

				StringBuilder sb = new StringBuilder();
				sb.append(
						"<table cellpadding=\"0\" cellspacing=\"1\" width=\"100%\" border=\"0\" class=\"Ptable\"><tbody>");

				for (JsonNode param : arrayNode) {
					sb.append("<tr><th class=\"tdTitle\" colspan=\"2\">" + param.get("group").asText() + "</th></tr>");
					ArrayNode params = (ArrayNode) param.get("params");
					for (JsonNode p : params) {
						sb.append("<tr><td class=\"tdTitle\">" + p.get("k").asText() + "</td><td>" + p.get("v").asText()
								+ "</td></tr>");
					}
				}

				sb.append("</tbody></table>");
				
				//将数据放入到Redis中：
				try {
					this.redisTemplate.opsForValue().set(RedisKeyConstant.ITEM_PARAM_ITEM_KEY + itemId, sb.toString());
					this.redisTemplate.expire(RedisKeyConstant.ITEM_PARAM_ITEM_KEY + itemId, SECONDS, TimeUnit.SECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
