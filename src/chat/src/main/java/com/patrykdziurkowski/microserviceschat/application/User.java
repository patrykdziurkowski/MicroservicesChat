package com.patrykdziurkowski.microserviceschat.application;

import java.util.UUID;

public class User {
    private UUID id;
    private String userName;

    public User() {
    }

    public User(UUID id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
