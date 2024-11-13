package com.patrykdziurkowski.microserviceschat.domain;

import java.util.Optional;
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

    private FavoriteChatRoom() {}

    public boolean unsetFavorite(UUID currentUserId) {
        if (currentUserId != userId) {
            return false;
        }
        raiseDomainEvent(new FavoriteUnsetEvent());
        return true;
    }

    public static Optional<FavoriteChatRoom> set(UUID currentUserId, Optional<ChatRoom> retrievedChat) {
        if (retrievedChat.isEmpty() || retrievedChat.get().getMemberIds().contains(currentUserId) == false) {
            return Optional.empty();
        }

        FavoriteChatRoom favoriteChatRoom = new FavoriteChatRoom();
        favoriteChatRoom.setId(UUID.randomUUID());
        favoriteChatRoom.setChatRoomId(retrievedChat.get().getId());
        favoriteChatRoom.setUserId(currentUserId);
        
        return Optional.of(favoriteChatRoom);
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

    private void setId(UUID id) {
        this.id = id;
    }

    private void setChatRoomId(UUID chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    private void setUserId(UUID userId) {
        this.userId = userId;
    }

}
