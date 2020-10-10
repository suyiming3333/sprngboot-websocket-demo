package com.corn.sprngboot.websocket.demo.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: MyHasdShakeInterceptor
 * @Package com.corn.sprngboot.websocket.demo.websocket
 * @Description: 自定义 handshake拦截器
 * @date 2020/9/28 15:43
 */


public class MyHasdShakeInterceptor extends HttpSessionHandshakeInterceptor {

    /***
     * websocket监理连接之前的拦截(可作授权处理)
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        /**
         * 获取url传递的参数，通过attributes在Interceptor处理结束后传递给WebSocketHandler,
         * WebSocketHandler可以通过WebSocketSession的getAttributes()方法获取参数
         * ws://127.0.0.1:8080/testWebSocket?userId=1&userName=corn&roomId=1
         */
        ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
        String userId = serverRequest.getServletRequest().getParameter("userId");
        String userName = serverRequest.getServletRequest().getParameter("userName");
        String roomId = serverRequest.getServletRequest().getParameter("roomId");

        //鉴权
        if (true) {
            System.out.println("Validation passed. WebSocket connecting.... ");
            attributes.put("userId", userId);
            attributes.put("userName", userName);
            attributes.put("roomId", roomId);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        } else {
            System.out.println("Validation failed. WebSocket will not connect. ");
            return false;
        }    }
}
