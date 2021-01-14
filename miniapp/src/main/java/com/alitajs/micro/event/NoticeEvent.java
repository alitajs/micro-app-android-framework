package com.alitajs.micro.event;

public class NoticeEvent {

    String name;
    String userInfo;

    public NoticeEvent(String name, String userInfo) {
        this.name = name;
        this.userInfo = userInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
