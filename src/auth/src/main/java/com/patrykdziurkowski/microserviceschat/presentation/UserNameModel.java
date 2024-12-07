package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserNameModel {
    @NotNull
    private UUID userId;
    @NotBlank
    @Size(min = 3, max = 15)
    @Pattern(regexp = "^\\w+$") // alphanumeric only
    private String userName;

    public UserNameModel() {

    }

    public UserNameModel(UUID userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

}
