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

    public JoinChatCommand(ChatRepository chatRepository, PasswordEncoder passwordEncoder) {
        this.chatRepository = chatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean execute(UUID currentUserId, UUID chatId, String currentUserUsername, Optional<String> givenChatPassword) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        if(checkPassword(chat.getPasswordHash(), givenChatPassword) == false) {
            return false;
        }
        if(chat.join(currentUserId, currentUserUsername) == false) {
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
