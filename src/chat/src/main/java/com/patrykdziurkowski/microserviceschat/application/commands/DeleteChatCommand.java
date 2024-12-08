package com.patrykdziurkowski.microserviceschat.application.commands;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.ChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

@Service
public class DeleteChatCommand {
    private final ChatRepository chatRepository;

    public DeleteChatCommand(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean execute(UUID currentUserId, UUID chatId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if (retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        if (chat.dissolve(currentUserId) == false) {
            return false;
        }
        chatRepository.save(chat);
        return true;
    }
}
