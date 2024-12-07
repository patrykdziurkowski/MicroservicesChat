package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.ChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class ChatQuery {
    private final ChatRepository chatRepository;

    public ChatQuery(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Optional<ChatRoom> execute(UUID chatId) {
        return chatRepository.getById(chatId);
    }
}
