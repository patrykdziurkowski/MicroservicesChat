package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class JoinChatCommand {
    private final ChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationApiClient apiClient;

    public JoinChatCommand(ChatRepository chatRepository,
            PasswordEncoder passwordEncoder, 
            AuthenticationApiClient apiClient) {
        this.chatRepository = chatRepository;
        this.passwordEncoder = passwordEncoder;
        this.apiClient = apiClient;
    }

    public boolean execute(UUID currentUserId, UUID chatId, Optional<String> givenChatPassword) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        if(checkPassword(chat.getPasswordHash(), givenChatPassword) == false) {
            return false;
        }
        Optional<String> currentUserName = apiClient.sendUserNameRequest(currentUserId);
        if(currentUserName.isEmpty()) {
            return false;
        }
        if(chat.join(currentUserId, currentUserName.orElseThrow()) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }

    private boolean checkPassword(Optional<String> chatPassword, Optional<String> givenChatPassword) {
        if(chatPassword.isEmpty()) {
            return true;
        }
        if(givenChatPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(givenChatPassword.get(), chatPassword.get());
    }
}
