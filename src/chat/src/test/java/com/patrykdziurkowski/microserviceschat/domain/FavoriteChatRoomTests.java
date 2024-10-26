package com.patrykdziurkowski.microserviceschat.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.FavoriteUnsetEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public class FavoriteChatRoomTests {
    @Test
    public void unsetFavorite_givenValidData_changesIsFlaggedForDeletionToTrue() {
        UUID chatRoomId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        FavoriteChatRoom favoriteChatRoom = new FavoriteChatRoom(chatRoomId, ownerId);
        UUID currentUserId = ownerId;

        favoriteChatRoom.unsetFavorite(currentUserId);

        DomainEvent event = favoriteChatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof FavoriteUnsetEvent);
    }

    @Test
    public void unsetFavorite_givenInvalidData_doesNotChangeIsFlaggedForDeletion() {
        UUID chatRoomId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        FavoriteChatRoom favoriteChatRoom = new FavoriteChatRoom(chatRoomId, ownerId);
        UUID currentUserId = UUID.randomUUID();

        favoriteChatRoom.unsetFavorite(currentUserId);

        assertTrue(favoriteChatRoom.getDomainEvents().isEmpty());
    }
}
