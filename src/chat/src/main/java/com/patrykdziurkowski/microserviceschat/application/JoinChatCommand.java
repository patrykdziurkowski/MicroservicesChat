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

    public Optional<ChatRoom> execute(UUID currentUserId, UUID chatId, Optional<String> givenChatPassword) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if (retrievedChat.isEmpty()) {
            return Optional.empty();
        }

        ChatRoom chat = retrievedChat.get();
        if (passwordDoesntPass(chat.getPasswordHash(), givenChatPassword)) {
            return Optional.empty();
        }

        Optional<String> currentUserName = apiClient.sendUserNameRequest(currentUserId);
        if (currentUserName.isEmpty()) {
            return Optional.empty();
        }

        if (chat.join(currentUserId, currentUserName.orElseThrow()) == false) {
            return Optional.empty();
        }
        chatRepository.save(chat);
        return Optional.of(chat);
    }

    private boolean passwordDoesntPass(Optional<String> chatPassword, Optional<String> givenChatPassword) {
        if (chatPassword.isEmpty()) {
            return false;
        }
        if (givenChatPassword.isEmpty()) {
            return true;
        }
        return !passwordEncoder.matches(givenChatPassword.get(), chatPassword.get());
    }
}
