package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class LeaveChatCommand {
    private final ChatRepository chatRepository;
    private final UserApiClient apiClient;

    public LeaveChatCommand(ChatRepository chatRepository,
            UserApiClient apiClient) {
        this.chatRepository = chatRepository;
        this.apiClient = apiClient;
    }

    public Optional<ChatRoom> execute(UUID currentUserId, UUID chatId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if (retrievedChat.isEmpty()) {
            return Optional.empty();
        }
        ChatRoom chat = retrievedChat.get();
        Optional<String> currentUserName = apiClient.sendUserNameRequest(currentUserId);
        if (currentUserName.isEmpty()) {
            return Optional.empty();
        }
        if (chat.leave(currentUserId, currentUserName.orElseThrow()) == false) {
            return Optional.empty();
        }
        chatRepository.save(chat);
        return Optional.of(chat);
    }
}
