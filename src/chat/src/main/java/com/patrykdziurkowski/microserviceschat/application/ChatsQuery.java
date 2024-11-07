package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class ChatsQuery {
    private final ChatRepository chatRepository;

    public ChatsQuery(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Optional<List<ChatRoom>> execute(UUID currentUserId) {
        final List<ChatRoom> chats = chatRepository.getByMemberId(currentUserId);

        if(chats.isEmpty() == false) {
            return Optional.ofNullable(chats);
        }

        return Optional.empty();
    }
}
