package com.uggnproduction.messenger;

public class Chat {
    private int id;
    private String name;
    private String lastMessage;
    private String time;
    private String avatarUrl;
    private int unreadCount;

    public Chat(int id, String name, String lastMessage, String time, String avatarUrl, int unreadCount) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.avatarUrl = avatarUrl;
        this.unreadCount = unreadCount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getUnreadCount() { return unreadCount; }
}
