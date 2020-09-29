package com.corn.sprngboot.websocket.demo.controller;

import com.corn.sprngboot.websocket.demo.websocket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private MyWebSocketHandler myWebSocketHandler;


    @RequestMapping("/sendMsg")
    public void sendMsgToUser(@RequestParam("userId") String userId,@RequestParam("message")String message){
        try {
            myWebSocketHandler.sendMessage(message,userId);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
