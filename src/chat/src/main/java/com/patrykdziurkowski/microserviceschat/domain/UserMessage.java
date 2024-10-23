package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserMessage extends Message {
    private UUID ownerId;
    private boolean isDeleted;

    UserMessage() {}
    public UserMessage(String text, UUID ownerId) {
        super(text);
        this.isDeleted = false;
        this.ownerId = ownerId;
    }
    public UserMessage(String text, UUID ownerId, LocalDateTime datePosted) {
        super(text, datePosted);
        this.isDeleted = false;
        this.ownerId = ownerId;
    }
    public UUID getOwnerId() {
        return ownerId;
    }
    public boolean getIsDeleted() {
        return isDeleted;
    }
}
