package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private UUID id = UUID.randomUUID();
    private String text;
    private LocalDateTime dataPosted;

    Message() {}
    public Message(String text) {
        this.id = UUID.randomUUID();
        this.text = text;
        this.dataPosted = LocalDateTime.now();
    }
    public String getText() {
        return text;
    }
    public LocalDateTime getDataPosted() {
        return dataPosted;
    }
    public UUID getId() {
        return id;
    }
}
