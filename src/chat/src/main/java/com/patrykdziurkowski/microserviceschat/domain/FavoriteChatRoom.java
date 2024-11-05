package com.patrykdziurkowski.microserviceschat.domain;

import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.FavoriteUnsetEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.AggreggateRoot;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FavoriteChatRoom extends AggreggateRoot {
    @Id
    private UUID id;
    private UUID chatRoomId;
    private UUID userId;

    FavoriteChatRoom() {
    }

    public FavoriteChatRoom(UUID chatRoomId, UUID currentUserId) {
        this.id = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.userId = currentUserId;
    }

    public boolean unsetFavorite(UUID currentUserId) {
        if (currentUserId != userId) {
            return false;
        }
        raiseDomainEvent(new FavoriteUnsetEvent());
        return true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public UUID getUserId() {
        return userId;
    }

}
