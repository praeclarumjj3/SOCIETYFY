package com.example.societyfy.Activities.models;

public class Chat {

    private String id;
    private String chatRoomName;
    private String senderImage;
    private String senderId;
    private String senderName;
    private String message;
    private long sent;

    public Chat(String id, String chatRoomName, String userId , String userImage, String senderName, String message, long sent) {
        this.id = id;
        this.chatRoomName = chatRoomName;
        this.senderId = userId;
        this.senderImage = userImage;
        this.senderName = senderName;
        this.message = message;
        this.sent = sent;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomId(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public String getSenderId() {
        return  senderId;
    }

    public void setUserId(String senderId) {
        this.senderId = senderId;
    }

    public String getUserImage() {
        return senderImage;
    }

    public void setUserImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }
}