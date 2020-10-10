package com.corn.sprngboot.websocket.demo.redis;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: RedisKey
 * @Package com.corn.sprngboot.websocket.demo.redis
 * @Description: TODO
 * @date 2020/10/10 11:41
 */
public enum RedisKey {

    CLUSTER_ONLINE_COUNT("CLUSTER_ONLINE_COUNT","总用户在线数key"),
    REDIS_MQ_CHAT("REDIS_MQ_CHAT","redis队列"),
    ROOM_ONLINE_COUNT("ROOM_ONLINE_COUNT","节点在线数key");

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String redisKey;

    private String description;

    RedisKey(String redisKey,String description){
        this.redisKey = redisKey;
        this.description = description;
    }
}
