package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class CreateChatCommand {
    private final ChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateChatCommand(ChatRepository chatRepository, PasswordEncoder passwordEncoder) {
        this.chatRepository = chatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ChatRoom execute(UUID currentUserId, String chatName, boolean isPublic, Optional<String> chatPassword) {
        ChatRoom chat;
        if(chatPassword.isPresent()) {
            final String encodedPassword = passwordEncoder.encode(chatPassword.get());
            chat = new ChatRoom(currentUserId, chatName, isPublic, encodedPassword);
        } else {
            chat = new ChatRoom(currentUserId, chatName, isPublic);
        }
        
        chatRepository.save(chat);
        return chat;
    }
}
