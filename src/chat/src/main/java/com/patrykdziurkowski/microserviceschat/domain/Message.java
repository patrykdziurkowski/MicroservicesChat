package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.shared.AggreggateRoot;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "message")
public class Message extends AggreggateRoot {
    @Id
    protected UUID id;
    private UUID chatRoomId;
    private String text;
    private LocalDateTime datePosted;

    Message() {}

    public Message(UUID chatRoomId, String text) {
        this(chatRoomId, text, LocalDateTime.now());
    }

    public Message(UUID chatRoomId, String text, LocalDateTime datePosted) {
        this.id = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
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

    public UUID getChatRoomId() {
        return chatRoomId;
    }
    
}
