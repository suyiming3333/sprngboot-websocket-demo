package com.corn.sprngboot.websocket.demo.controller;

import com.corn.sprngboot.websocket.demo.dto.MessageDto;
import com.corn.sprngboot.websocket.demo.netty.MyNettyWebSocketHandler;
import com.corn.sprngboot.websocket.demo.websocket.MyClusterWebSocketHandler;
import com.corn.sprngboot.websocket.demo.websocket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.plugin2.message.Message;

import java.io.IOException;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: SendMsgController
 * @Package com.corn.sprngboot.websocket.demo.controller
 * @Description: TODO
 * @date 2020/9/29 9:17
 */
@RestController
@RequestMapping("/websocket")
public class SendMsgController {

    @Autowired
    private MyNettyWebSocketHandler myNettyWebSocketHandler;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Autowired
    private MyClusterWebSocketHandler myClusterWebSocketHandler;


    @RequestMapping("/sendMsg")
    public void sendMsgToUser(@RequestParam("roomId") String roomId,
                              @RequestParam("userId") String userId,
                              @RequestParam("message")String message){
        try {
            MessageDto dto = new MessageDto();
            dto.setUserId(userId);
            dto.setRoomId(roomId);
            dto.setMessage(message);
            myClusterWebSocketHandler.sendMessageToOne(dto);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
