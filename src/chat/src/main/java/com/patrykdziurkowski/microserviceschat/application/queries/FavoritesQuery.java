package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.FavoriteChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

@Service
public class FavoritesQuery {
    private final FavoriteChatRepository favoriteChatRepository;

    public FavoritesQuery(FavoriteChatRepository favoriteChatRepository) {
        this.favoriteChatRepository = favoriteChatRepository;
    }

    public List<FavoriteChatRoom> execute(UUID currentUserId) {
        return favoriteChatRepository.getByUserId(currentUserId);
    }
}
