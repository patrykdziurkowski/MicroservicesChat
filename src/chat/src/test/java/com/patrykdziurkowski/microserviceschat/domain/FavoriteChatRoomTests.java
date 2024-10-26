package com.patrykdziurkowski.microserviceschat.domain;

import java.util.UUID;

import org.junit.Test;

public class FavoriteChatRoomTests {
    @Test
    public void unsetFavorite_givenValidData_changesIsFlaggedForDeletionToTrue() {
        UUID chatRoomId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        FavoriteChatRoom favoriteChatRoom = new FavoriteChatRoom(chatRoomId, ownerId);
        UUID currentUserId = ownerId;

        favoriteChatRoom.unsetFavorite(currentUserId);

        assert(favoriteChatRoom.getIsFlaggedForDeletion() == true);
    }

    @Test
    public void unsetFavorite_givenInvalidData_doesNotChangeIsFlaggedForDeletion() {
        UUID chatRoomId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        FavoriteChatRoom favoriteChatRoom = new FavoriteChatRoom(chatRoomId, ownerId);
        UUID currentUserId = UUID.randomUUID();

        favoriteChatRoom.unsetFavorite(currentUserId);

        assert(favoriteChatRoom.getIsFlaggedForDeletion() == false);
    }
}
