package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

public interface FavoriteChatRepository {

    List<FavoriteChatRoom> get();
    Optional<FavoriteChatRoom> getById(UUID chatId);
    List<FavoriteChatRoom> getByUserId(UUID userId);
    void save(FavoriteChatRoom chatRoom);
    
}
