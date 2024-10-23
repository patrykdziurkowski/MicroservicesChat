package com.patrykdziurkowski.microserviceschat.domain;

import java.util.UUID;

public class FavoriteChatRoom {
    private UUID id;
    private UUID chatRoomId;
    private UUID userId;
    private boolean isFlaggedForDeletion;

    FavoriteChatRoom() {}
    public FavoriteChatRoom(UUID chatRoomId, UUID currentUserId) {
        this.id = UUID.randomUUID();
        this.chatRoomId = chatRoomId;
        this.userId = currentUserId;
        this.isFlaggedForDeletion = false;
    }
    public boolean unsetFavorite(UUID currentUserId) {
        if (currentUserId != userId) {
            return false;
        }
        this.isFlaggedForDeletion = true;
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
    public boolean getIsFlaggedForDeletion() {
        return isFlaggedForDeletion;
    }
}
