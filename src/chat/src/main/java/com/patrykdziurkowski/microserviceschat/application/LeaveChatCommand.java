package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class LeaveChatCommand {
    private final ChatRepository chatRepository;
    private final AuthenticationApiClient apiClient;

    public LeaveChatCommand(ChatRepository chatRepository,
            AuthenticationApiClient apiClient) {
        this.chatRepository = chatRepository;
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, UUID chatId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        Optional<String> currentUserName = apiClient.sendUserNameRequest(currentUserId);
        if(currentUserName.isEmpty()) {
            return false;
        }
        if(chat.leave(currentUserId, currentUserName.orElseThrow()) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }
}
