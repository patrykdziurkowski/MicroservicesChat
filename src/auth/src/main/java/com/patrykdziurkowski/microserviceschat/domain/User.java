package com.patrykdziurkowski.microserviceschat.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users")
public class User {
    @Id
    private UUID id;
    @Column(unique = true)
    private String userName;
    private String passwordHash;

    User() {

    }

    public User(String userName, String passwordHash) {
        this.id = UUID.randomUUID();
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
