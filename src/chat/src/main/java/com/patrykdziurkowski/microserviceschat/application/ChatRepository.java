package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

public interface ChatRepository {
    
    List<ChatRoom> get();
    Optional<ChatRoom> getById(UUID chatId);
    List<ChatRoom> getByMemberId(UUID memberId);
    void save (ChatRoom chatRoom);

}
