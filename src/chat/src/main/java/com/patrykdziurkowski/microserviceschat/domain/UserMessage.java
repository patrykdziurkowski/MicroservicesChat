package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserMessage extends Message {
    private UUID ownerId;

    UserMessage() {
    }

    public UserMessage(String text, UUID ownerId) {
        super(text);
        this.ownerId = ownerId;
    }

    public UserMessage(String text, UUID ownerId, LocalDateTime datePosted) {
        super(text, datePosted);
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
