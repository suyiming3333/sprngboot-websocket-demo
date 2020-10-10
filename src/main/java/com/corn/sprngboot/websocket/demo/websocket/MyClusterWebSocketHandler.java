package com.corn.sprngboot.websocket.demo.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.corn.sprngboot.websocket.demo.dto.MessageDto;
import com.corn.sprngboot.websocket.demo.redis.RedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: MyWebSocketHandler
 * @Package com.corn.sprngboot.websocket.demo.websocket
 * @Description: TODO
 * @date 2020/9/28 16:40
 */

@Component
public class MyClusterWebSocketHandler implements WebSocketHandler {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 保存当前集群节点的session信息：
     *                   roomId,
     *                                    userId,WebSocketSession
     * ConcurrentHashMap<String, List<Map<String,WebSocketSession>>>
     *
     */
    private ConcurrentHashMap<String, Map<String,WebSocketSession>> roomSessionListMap = new ConcurrentHashMap<String, Map<String,WebSocketSession>>();

    private Map<String,WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    /**获取整个集群所有的在线数**/
    private Long getClusterOnlineCount(){
        return (Long) redisTemplate.opsForValue().get(RedisKey.CLUSTER_ONLINE_COUNT.getRedisKey());
    }

    /**集群在线数累加**/
    private Long incrClusterOnlineCount(){
        return redisTemplate.opsForValue().increment(RedisKey.CLUSTER_ONLINE_COUNT.getRedisKey());
    }

    /**集群在线数累减**/
    private Long decrClusterOnlineCount(){
        return redisTemplate.opsForValue().decrement(RedisKey.CLUSTER_ONLINE_COUNT.getRedisKey());
    }

    /**获取房间的在线数**/
    private Long getOnlineCountByRoom(String roomId){
        return (Long) redisTemplate.opsForValue().get(RedisKey.ROOM_ONLINE_COUNT.getRedisKey()+roomId);
    }

    /**单个房间在线数累加**/
    private Long incrSingleRoomOnlineCount(String roomId){
        return redisTemplate.opsForValue().increment(RedisKey.ROOM_ONLINE_COUNT.getRedisKey()+roomId);
    }

    /**单个房间在线数累减**/
    private Long decrSingleRoomOnlineCount(String roomId){
        return redisTemplate.opsForValue().decrement(RedisKey.ROOM_ONLINE_COUNT.getRedisKey()+roomId);
    }


    /***
     * 每个websocket建立连接之后
     * @param webSocketSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        String roomId = (String)webSocketSession.getAttributes().get("roomId");
        String userId = (String)webSocketSession.getAttributes().get("userId");
        //根据roomId获取sessions
        Map<String, WebSocketSession> sessions = roomSessionListMap.get(roomId);

        Map tmpMap = new HashMap();
        tmpMap.put(userId,webSocketSession);

        if(sessions!=null){
            sessions.put(userId,webSocketSession);
        }else{
            sessions = new ConcurrentHashMap<>();
            sessions.put(userId,webSocketSession);
            roomSessionListMap.put(roomId,sessions);
        }

        /**在线统计累加**/
        Long clusterOnlineCount = this.incrClusterOnlineCount();
        Long singleRoomOnlineCount = this.incrSingleRoomOnlineCount(roomId);
        //todo 用户上线群通知
        String result = StrUtil.format("Open a new WebSocket. Current clustner Online Count:{},and single Room:{} Online Count :{}",clusterOnlineCount,roomId,singleRoomOnlineCount);
        System.out.println(result);

    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        MessageDto dto = new MessageDto();
        String roomId = (String)webSocketSession.getAttributes().get("roomId");
        String userId = (String)webSocketSession.getAttributes().get("userId");

        //收到客户端发送的信息
        TextMessage textMessage = (TextMessage) webSocketMessage;
        System.out.println("Receive a message from client: " + textMessage.getPayload());
        dto.setMessage(textMessage.getPayload());
        dto.setRoomId(roomId);
        dto.setUserId(userId);
        this.publishMessageToQueue(dto);
        //todo 记录历史的消息记录


    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        System.out.println("Exception occurs on webSocket connection. disconnecting....");
        if (webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        String roomId = (String)webSocketSession.getAttributes().get("roomId");
        String userId = (String)webSocketSession.getAttributes().get("userId");

        //根据roomId获取sessions
        Map<String, WebSocketSession> sessions = roomSessionListMap.get(roomId);
        sessions.remove(userId);
        /**在线统计累减**/
        Long clusterOnlineCount = this.decrClusterOnlineCount();
        Long singleRoomOnlineCount = this.decrSingleRoomOnlineCount(roomId);
        //todo 用户离线群通知
        String result = StrUtil.format("WebSocket closed. Current clustner Online Count:{},and single Room:{} Online Count :{}",clusterOnlineCount,roomId,singleRoomOnlineCount);
        System.out.println(result);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        String roomId = (String)webSocketSession.getAttributes().get("roomId");
        String userId = (String)webSocketSession.getAttributes().get("userId");

        //根据roomId获取sessions
        Map<String, WebSocketSession> sessions = roomSessionListMap.get(roomId);
        sessions.remove(userId);
        /**在线统计累减**/
        Long clusterOnlineCount = this.decrClusterOnlineCount();
        Long singleRoomOnlineCount = this.decrSingleRoomOnlineCount(roomId);
        //todo 用户离线群通知
        String result = StrUtil.format("WebSocket closed. Current clustner Online Count:{},and single Room:{} Online Count :{}",clusterOnlineCount,roomId,singleRoomOnlineCount);
        System.out.println(result);

    }


    /**
     * 发布消息到redis队列
     * @param dto
     */
    public void publishMessageToQueue(MessageDto dto){
        stringRedisTemplate.convertAndSend(RedisKey.REDIS_MQ_CHAT.getRedisKey(),JSONUtil.toJsonStr(dto));
    }

    /**
     * 给某个用户发送消息
     * @param MessageDto dto
     * @return
     * @throws IOException
     */
    public boolean sendMessageToOne(MessageDto dto) throws IOException {
        //根据roomId获取sessions
        Map<String, WebSocketSession> sessions = roomSessionListMap.get(dto.getRoomId());

        if(sessions!=null && sessions.size()>0){
            sessions.keySet().stream().forEach(vo->{

                if(vo.equals(dto.getUserId())){
                    TextMessage textMessage = new TextMessage(dto.getMessage());
                    try {
                        sessions.get(vo.toString()).sendMessage(textMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            return true;
        }else{
            return false;
        }
    }

    /**
     * 给同一个房间的所有用户发送通知
     * @param dto
     * @return
     * @throws IOException
     */
    public boolean sendMessageToAllByRoom(MessageDto dto) throws IOException {
        //根据roomId获取sessions
        Map<String, WebSocketSession> sessions = roomSessionListMap.get(dto.getRoomId());

        if(sessions!=null){
            TextMessage textMessage = new TextMessage(dto.getMessage());
            sessions.keySet().stream().forEach(vo->{
                try {
                    sessions.get(vo.toString()).sendMessage(textMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            return true;
        }else{
            return false;
        }
    }

    /*
     * 是否支持消息拆分发送：如果接收的数据量比较大，最好打开(true), 否则可能会导致接收失败。
     * 如果出现WebSocket连接接收一次数据后就自动断开，应检查是否是这里的问题。
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
