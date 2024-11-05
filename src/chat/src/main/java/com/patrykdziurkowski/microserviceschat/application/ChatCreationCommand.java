package com.patrykdziurkowski.microserviceschat.application;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class ChatCreationCommand {
    private final ChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;

    public ChatCreationCommand(ChatRepository chatRepository, PasswordEncoder passwordEncoder) {
        this.chatRepository = chatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean execute(UUID currentUserId, String chatName, boolean isPublic, String chatPassword) {
        if(chatPassword != null) {
            chatPassword = passwordEncoder.encode(chatPassword);
        }
        ChatRoom chat = new ChatRoom(currentUserId, chatName, isPublic, chatPassword);
        chatRepository.save(chat);
        return true;
    }
}
