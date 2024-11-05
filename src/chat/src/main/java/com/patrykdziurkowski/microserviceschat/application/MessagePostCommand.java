package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@Service
public class MessagePostCommand {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public MessagePostCommand(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public boolean execute(UUID chatId, String text, UUID currentUserId) {
        Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return false;
        }
        ChatRoom chat = retrievedChat.get();
        if(chat.getMemberIds().contains(currentUserId) == false) {
            return false;
        }
        UserMessage message = new UserMessage(chatId, text, currentUserId);
        messageRepository.save(message);
        return true;
    }
}
