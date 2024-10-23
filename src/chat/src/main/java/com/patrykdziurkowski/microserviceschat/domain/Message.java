package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private UUID id = UUID.randomUUID();
    private String text;
    private LocalDateTime datePosted;

    Message() {}
    public Message(String text) {
        this.id = UUID.randomUUID();
        this.text = text;
        this.datePosted = LocalDateTime.now();
    }
    public Message(String text, LocalDateTime datePosted) {
        this.id = UUID.randomUUID();
        this.text = text;
        this.datePosted = datePosted;
    }
    public String getText() {
        return text;
    }
    public LocalDateTime getDatePosted() {
        return datePosted;
    }
    public UUID getId() {
        return id;
    }
}
