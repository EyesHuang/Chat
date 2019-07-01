package com.yt.chat.Model;

public class Conversation {
    private boolean seen;
    private long timestamp, message_time;

    public Conversation(boolean seen, long timestamp, long message_time) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.message_time = message_time;
    }

    public boolean getSeen() {
        return seen;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public long getMessageTime() {
        return message_time;
    }
}
