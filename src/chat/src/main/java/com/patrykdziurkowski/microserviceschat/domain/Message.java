package com.patrykdziurkowski.microserviceschat.domain;

import java.time.Instant;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.shared.AggreggateRoot;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Message extends AggreggateRoot {
    @Id
    protected UUID id;
    private UUID chatRoomId;
    private String text;
    private Instant datePosted;

    Message() {
    }

    public Message(UUID chatRoomId, String text) {
        this(chatRoomId, text, Instant.now());
    }

    public Message(UUID chatRoomId, String text, Instant datePosted) {
        this.id = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.text = text;
        this.datePosted = datePosted;
    }

    public String getText() {
        return text;
    }

    public Instant getDatePosted() {
        return datePosted;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

}
