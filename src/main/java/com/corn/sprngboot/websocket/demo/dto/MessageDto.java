package com.corn.sprngboot.websocket.demo.dto;

/**
 * @author suyiming3333@gmail.com
 * @version V1.0
 * @Title: MessageDto
 * @Package com.corn.sprngboot.websocket.demo.dto
 * @Description: TODO
 * @date 2020/10/10 9:28
 */
public class MessageDto {

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String userId;

    private String roomId;

    private String message;
}
