package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class ChatsQuery {
    private final ChatRepository chatRepository;

    public ChatsQuery(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<ChatRoom> execute(UUID currentUserId) {
        return chatRepository.getByMemberId(currentUserId);
    }
}
