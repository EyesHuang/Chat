package com.yt.chat.Model;

public class Users implements Comparable {
    private String name;
    private String image;
    private String status;
    private String user_id;

    public Users(String name, String image, String status, String user_id) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public String getUser_id() {
        return user_id;
    }


    @Override
    public int compareTo(Object o) {
        Users user = (Users) o;
        String name = ((Users) o).name;

        return this.name.compareTo(name);
    }
}
