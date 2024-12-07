package com.patrykdziurkowski.microserviceschat.application.queries;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.ChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class ChatsQuery {
    private final ChatRepository chatRepository;

    public ChatsQuery(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<ChatRoom> execute(UUID currentUserId, int lastChatPosition, int chatsToRetrieve) {
        return chatRepository.getByMemberId(currentUserId, lastChatPosition, chatsToRetrieve);
    }
}
