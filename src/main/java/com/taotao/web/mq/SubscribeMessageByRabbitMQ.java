package com.taotao.web.mq;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.web.service.ItemService;

public class SubscribeMessageByRabbitMQ {
	
	@Autowired
    private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	private ItemService itemService;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	 
	/**
	 * 具体执行业务的方法。
	 * @param message:是一个json格式的map，其中存储了“操作类型（CRUD）、被操作对象的id、修改时间”这三个数据。
	 */
    public void listen(String message) {
        if(StringUtils.isNotEmpty(message)){
        	 try {
        		 JsonNode jsonNode = MAPPER.readTree(message);
                 Long id = jsonNode.get("id").asLong();
				//删除Redis中该数据的缓存：
                Collection<String> keys = new ArrayList<>();
                keys.add(RedisKeyConstant.ITEM_KEY + id);
                keys.add(RedisKeyConstant.ITEM_DESC_KEY + id);
                keys.add(RedisKeyConstant.ITEM_PARAM_ITEM_KEY + id);
				this.redisTemplate.delete(keys);
				//重新根据该数据的id进行查询：如果查得到，就会重新将查到的信息放入Redis中
	            this.itemService.queryItemById(id);
			} catch (Exception e) {
				e.printStackTrace();
			} 
        }
    }
}
