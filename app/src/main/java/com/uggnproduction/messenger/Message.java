package com.uggnproduction.messenger;

public class Message {
    private String text;
    private boolean isSentByMe;
    private long timestamp;

    public Message(String text, boolean isSentByMe, long timestamp) {
        this.text = text;
        this.isSentByMe = isSentByMe;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
