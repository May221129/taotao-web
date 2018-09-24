package com.taotao.web.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.web.service.ItemService;

@Component
public class ItemSubscribeByRedisMessageListener implements MessageListener {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private ItemService itemService;
    
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());// this.getClass()是子类的
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
    	byte[] channel = message.getChannel();
        byte[] body = message.getBody();// 请使用valueSerializer
        // 请参考配置文件，本例中key，value的序列化方式均为string。
        // 其中key必须为stringSerializer。和redisTemplate.convertAndSend对应
        String msgContent = (String) redisTemplate.getValueSerializer().deserialize(body);
        String topic = (String) redisTemplate.getStringSerializer().deserialize(channel);
        
        LOGGER.info("msgContent=" + msgContent + ", topic=" + topic);
        
        //获取通道消息后，响应：
        redisTemplate.delete(RedisKeyConstant.ITEM_KEY + msgContent);
        Long itemId = (long)Integer.valueOf(msgContent);
        this.itemService.queryItemById(itemId);
    }

}