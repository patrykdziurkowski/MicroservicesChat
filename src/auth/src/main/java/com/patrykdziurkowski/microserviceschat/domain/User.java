package com.patrykdziurkowski.microserviceschat.domain;

import java.util.UUID;

public class User {
    private UUID id;
    private String userName;
    private String passwordHash;

    User() {

    }

    public User(String userName, String passwordHash) {
        this.id = UUID.randomUUID();
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    public boolean login(String userName, String passwordHash) {
        return this.userName == userName && this.passwordHash == passwordHash;
    }

    public UUID getId() {
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }
}
