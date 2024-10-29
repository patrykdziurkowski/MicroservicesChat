package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "userMessage")
public class UserMessage extends Message {
    private UUID ownerId;

    UserMessage() {}

    public UserMessage(UUID chatRoomId, String text, UUID ownerId) {
        super(chatRoomId, text);
        this.ownerId = ownerId;
    }

    public UserMessage(UUID chatRoomId, String text, UUID ownerId, LocalDateTime datePosted) {
        super(chatRoomId, text, datePosted);
        this.ownerId = ownerId;
    }

    public boolean delete(UUID currentUserId, UUID chatRoomOwnerId) {
        boolean hasDeletePermissions = currentUserId == ownerId || currentUserId == chatRoomOwnerId;
        if (hasDeletePermissions == false) {
            return false;
        }
        raiseDomainEvent(new MessageDeletedEvent());
        return true;
    }
    
    public UUID getOwnerId() {
        return ownerId;
    }
}
