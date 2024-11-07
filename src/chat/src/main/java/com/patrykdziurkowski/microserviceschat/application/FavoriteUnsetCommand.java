package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

@Service
public class FavoriteUnsetCommand {
    private final FavoriteChatRepository favoriteChatRepository;

    public FavoriteUnsetCommand(FavoriteChatRepository favoriteChatRepository) {
        this.favoriteChatRepository = favoriteChatRepository;
    }

    public boolean execute(UUID currentUserId, UUID chatId) {
        List<FavoriteChatRoom> retrievedFavoriteChats = favoriteChatRepository.getByUserId(currentUserId);
        if(retrievedFavoriteChats.isEmpty()) {
            return false;
        }
        Optional<FavoriteChatRoom> foundFavoriteChat = findFavoriteChatById(chatId, retrievedFavoriteChats);
        boolean operationSucceeded = foundFavoriteChat.isPresent() && foundFavoriteChat.get().unsetFavorite(currentUserId);
        if(operationSucceeded) {
            favoriteChatRepository.save(foundFavoriteChat.get());
            return true;
        }
        return false;
    }

    private Optional<FavoriteChatRoom> findFavoriteChatById(UUID chatId, List<FavoriteChatRoom> listOfFavoriteChats) {
        return listOfFavoriteChats.stream()
                .filter(favoriteChatRoom -> favoriteChatRoom.getChatRoomId().equals(chatId))
                .findFirst();
    }
}
