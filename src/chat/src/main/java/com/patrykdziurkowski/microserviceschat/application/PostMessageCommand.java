package com.patrykdziurkowski.microserviceschat.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@Service
public class PostMessageCommand {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public PostMessageCommand(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public Optional<UserMessage> execute(UUID chatId, String text, UUID currentUserId) {
        final Optional<ChatRoom> retrievedChat = chatRepository.getById(chatId);
        if(retrievedChat.isEmpty()) {
            return Optional.empty();
        }
        final ChatRoom chat = retrievedChat.get();
        if(chat.getMemberIds().contains(currentUserId) == false) {
            return Optional.empty();
        }
        UserMessage message = new UserMessage(chatId, text, currentUserId);
        messageRepository.save(message);
        return Optional.of(message);
    }
}
