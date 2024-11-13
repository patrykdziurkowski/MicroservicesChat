package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

@Service
public class SetFavoriteCommand {
    private final FavoriteChatRepository favoriteChatRepository;
    private final ChatRepository chatRepository;

    public SetFavoriteCommand(FavoriteChatRepository favoriteChatRepository, ChatRepository chatRepository) {
        this.favoriteChatRepository = favoriteChatRepository;
        this.chatRepository = chatRepository;
    }

    public boolean execute(UUID currentUserId, UUID chatId) {
        Optional<FavoriteChatRoom> favoriteChatRoom = FavoriteChatRoom.set(currentUserId, chatRepository.getById(chatId));

        if (favoriteChatRoom.isPresent()) {
            favoriteChatRepository.save(favoriteChatRoom.get());
            return true;
        }
        
        return false;
    }
    
}

