package com.example.jobmatch;

import com.google.firebase.Timestamp;

public class ChatMessageHandler {

    String nameOfSender, chatMessage, chatTime;
    String senderUID;
    Timestamp timestamp;

    public ChatMessageHandler(String nameOfSender, String chatMessage) {
        this.nameOfSender = nameOfSender;
        this.chatMessage = chatMessage;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderUID() {
        return senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }

    public String getNameOfSender() {
        return nameOfSender;
    }

    public void setNameOfSender(String nameOfSender) {
        this.nameOfSender = nameOfSender;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

}
