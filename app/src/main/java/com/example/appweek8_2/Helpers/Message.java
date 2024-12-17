package com.example.appweek8_2.Helpers;

public class Message {
    private int senderId;
    private String messageText;
    private String timestamp;

    public Message(int senderId, String messageText, String timestamp) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId=" + senderId +
                ", messageText='" + messageText + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
