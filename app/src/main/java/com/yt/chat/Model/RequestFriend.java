package com.yt.chat.Model;

public class RequestFriend {
    private String name;
    private String image;
    private String user_id;

    public RequestFriend(String name, String image, String user_id) {
        this.name = name;
        this.image = image;
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getUserId() {
        return user_id;
    }
}
