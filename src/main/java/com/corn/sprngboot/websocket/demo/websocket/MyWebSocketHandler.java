package com.corn.sprngboot.websocket.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: MyWebSocketHandler
 * @Package com.corn.sprngboot.websocket.demo.websocket
 * @Description: TODO
 * @date 2020/9/28 16:40
 */

@Component
public class MyWebSocketHandler implements WebSocketHandler {


    /**统计在线数**/
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**websocket session列表**/
    private static final ArrayList<WebSocketSession> sessions = new ArrayList<>();

    /**用户id session**/
    private static final Map<String,WebSocketSession> sessionsMap = new ConcurrentHashMap<>();

    /***
     * websocket建立连接之后
     * @param webSocketSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        sessionsMap.put(webSocketSession.getAttributes().get("userId").toString(),webSocketSession);
        sessions.add(webSocketSession);
        int onlineNum = addOnlineCount();
        System.out.println("Oprn a WebSocket. Current connection number: " + onlineNum);

    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        TextMessage textMessage = (TextMessage) webSocketMessage;
        System.out.println("Receive a message from client: " + textMessage.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        System.out.println("Exception occurs on webSocket connection. disconnecting....");
        if (webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        sessionsMap.remove(webSocketSession.getAttributes().get("userId").toString());
        sessions.remove(webSocketSession);
        subOnlineCount();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        sessionsMap.remove(webSocketSession.getAttributes().get("userId").toString());
        sessions.remove(webSocketSession);
        int onlineNum = subOnlineCount();
        System.out.println("Close a webSocket. Current connection number: " + onlineNum);
    }

    /**
     * 给某个用户发送消息
     * @param message
     * @param userId
     * @return
     * @throws IOException
     */
    public boolean sendMessage(String message,String userId) throws IOException {
        if(sessionsMap.containsKey(userId)){
            TextMessage textMessage = new TextMessage(message);
            sessionsMap.get(userId).sendMessage(textMessage);
            return true;
        } else {
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

    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static int addOnlineCount() {
        return onlineCount.incrementAndGet();
    }

    public static int subOnlineCount() {
        return onlineCount.decrementAndGet();
    }
}
