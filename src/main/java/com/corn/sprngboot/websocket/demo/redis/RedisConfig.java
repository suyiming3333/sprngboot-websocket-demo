package com.corn.sprngboot.websocket.demo.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: RedisConfig
 * @Package com.corn.sprngboot.websocket.demo.config
 * @Description: TODO
 * @date 2020/10/9 17:08
 */

@Configuration
public class RedisConfig {


    /**
     * 创建监听队列的容器
     * @param redisConnectionFactory
     * @param messageListenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new PatternTopic(RedisKey.REDIS_MQ_CHAT.getRedisKey()));
        return redisMessageListenerContainer;
    }

    /**
     * 注入队列的监听者
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MyMessageListener receiver) {
        return new MessageListenerAdapter(receiver);
    }
}
