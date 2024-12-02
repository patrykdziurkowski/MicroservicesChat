package com.patrykdziurkowski.microserviceschat.application;

import java.util.UUID;

public class User {
    private UUID userId;
    private String userName;

    public User() {
    }

    public User(UUID id, String userName) {
        this.userId = id;
        this.userName = userName;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public void setUserId(UUID id) {
        this.userId = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
