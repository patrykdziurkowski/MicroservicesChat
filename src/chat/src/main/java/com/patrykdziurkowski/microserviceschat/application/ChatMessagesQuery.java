package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@Service
public class ChatMessagesQuery {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public ChatMessagesQuery(MessageRepository messageRepository,
            ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public Optional<List<UserMessage>> execute(UUID currentUserId, UUID chatId, int lastMessagePosition,
            int messagesToRetrive) {
        Optional<ChatRoom> chatResult = chatRepository.getById(chatId);
        if (chatResult.isEmpty()) {
            return Optional.empty();
        }
        ChatRoom chat = chatResult.orElseThrow();

        if (chat.getMemberIds().contains(currentUserId) == false) {
            return Optional.empty();
        }

        List<UserMessage> messages = messageRepository.getByAmount(chatId, lastMessagePosition, messagesToRetrive);
        return Optional.of(messages);
    }
}
