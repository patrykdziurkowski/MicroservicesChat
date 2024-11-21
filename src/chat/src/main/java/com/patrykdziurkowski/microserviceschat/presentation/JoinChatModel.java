package com.patrykdziurkowski.microserviceschat.presentation;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class JoinChatModel {
    @Nullable
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*\\d)(?=.*\\W).*$") // needs at least one digit and one non-alphanumeric character
    private String password;

    public JoinChatModel() {}
    
    public JoinChatModel(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
