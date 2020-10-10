package com.corn.sprngboot.websocket.demo.redis;

import cn.hutool.json.JSONUtil;
import com.corn.sprngboot.websocket.demo.dto.MessageDto;
import com.corn.sprngboot.websocket.demo.websocket.MyClusterWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: MyMessageListener
 * @Package com.corn.sprngboot.websocket.demo.redis
 * @Description: 消息订阅监听器，用于处理队列消息
 * @date 2020/10/9 17:12
 */


@Component
public class MyMessageListener implements MessageListener{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MyClusterWebSocketHandler myClusterWebSocketHandler;


    @Override
    public void onMessage(Message message, byte[] bytes) {
        //获取序列化器
        RedisSerializer<String> valueSerializer = redisTemplate.getStringSerializer();
        //将redis的数据反序列化
        String value = valueSerializer.deserialize(message.getBody());
        System.out.println("listener result:"+value);

        MessageDto dto = JSONUtil.toBean(value, MessageDto.class);
        try {
            //消息推送到某个放假的所有用户
            myClusterWebSocketHandler.sendMessageToAllByRoom(dto);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
