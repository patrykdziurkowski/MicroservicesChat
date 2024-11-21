package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class InvitedUserModel {
    @NotNull
    private UUID userId;

    public InvitedUserModel() {}

    public InvitedUserModel(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getUserId() {
        return this.userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
}
