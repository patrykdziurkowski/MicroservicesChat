package com.patrykdziurkowski.microserviceschat.presentation.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangeUserNameModel {
    @NotBlank
    @Size(min = 3, max = 15)
    @Pattern(regexp = "^\\w+$") // alphanumeric only
    private String userName;

    public ChangeUserNameModel() {
    }

    public ChangeUserNameModel(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
