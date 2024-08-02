package com.example.jobmatch;

import java.util.Date;

public class ChatListHandler {

    private String name;

    private String jobTitle;

    private String lastMessage;

    private String otherUser;

    private Date lastMessageTimestamp;

    private String currentUser;

    private boolean isParticipant1;

    public boolean isParticipant1() {
        return isParticipant1;
    }

    public void setParticipant1(boolean participant1) {
        isParticipant1 = participant1;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(String otherUser) {
        this.otherUser = otherUser;
    }

    public ChatListHandler() {
        // Required empty constructor for Firestore deserialization
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
