package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

@Service
public class FavoritesQuery {
    private final FavoriteChatRepository favoriteChatRepository;

    public FavoritesQuery(FavoriteChatRepository favoriteChatRepository) {
        this.favoriteChatRepository = favoriteChatRepository;
    }

    public Optional<List<FavoriteChatRoom>> execute(UUID currentUserId) {
        final List<FavoriteChatRoom> favoriteChats = favoriteChatRepository.getByUserId(currentUserId);

        if(favoriteChats.isEmpty() == false) {
            return Optional.ofNullable(favoriteChats);
        }

        return Optional.empty();
    }
}
