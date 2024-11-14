package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
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
        Optional<ChatRoom> chat = chatRepository.getById(chatId);
        if (chat.isEmpty()) {
            return false;
        }

        Optional<FavoriteChatRoom> favoriteChatRoom = FavoriteChatRoom.set(currentUserId, chat.orElseThrow());
        if (favoriteChatRoom.isEmpty()) {
            return false;
        }

        favoriteChatRepository.save(favoriteChatRoom.get());
        return true;
    }

}
