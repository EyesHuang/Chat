package com.yt.chat.Model;

public class Friends {
    private String name;
    private String user_id;

    public Friends() {}

    public Friends(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    public String getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }
}
