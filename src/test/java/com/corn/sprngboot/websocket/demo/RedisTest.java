package com.corn.sprngboot.websocket.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: RedisTest
 * @Package com.corn.sprngboot.websocket.demo
 * @Description: TODO
 * @date 2020/10/10 11:17
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testIncrease(){
        Long count = redisTemplate.opsForValue().increment("count");
        System.out.println("total:"+count);
    }

}
