package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

@Service
public class UnsetFavoriteCommand {
    private final FavoriteChatRepository favoriteChatRepository;

    public UnsetFavoriteCommand(FavoriteChatRepository favoriteChatRepository) {
        this.favoriteChatRepository = favoriteChatRepository;
    }

    public boolean execute(UUID currentUserId, UUID chatId) {
        final List<FavoriteChatRoom> retrievedFavoriteChats = favoriteChatRepository.getByUserId(currentUserId);
        if(retrievedFavoriteChats.isEmpty()) {
            return false;
        }
        Optional<FavoriteChatRoom> foundFavoriteChat = findFavoriteChatById(chatId, retrievedFavoriteChats);
        boolean operationSucceeded = foundFavoriteChat.isPresent() && foundFavoriteChat.get().unsetFavorite(currentUserId);
        if(operationSucceeded == false) {
            return false;
        }
        favoriteChatRepository.save(foundFavoriteChat.get());
        return true;
        
    }

    private Optional<FavoriteChatRoom> findFavoriteChatById(UUID chatId, List<FavoriteChatRoom> listOfFavoriteChats) {
        return listOfFavoriteChats.stream()
                .filter(favoriteChatRoom -> favoriteChatRoom.getChatRoomId().equals(chatId))
                .findFirst();
    }
}
