package com.patrykdziurkowski.microserviceschat.presentation.models;

import java.util.UUID;

public class UserClaims {
    private UUID id;
    private String userName;

    public UserClaims(UUID id, String userName) {
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
