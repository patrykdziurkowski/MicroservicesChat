package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@Service
public class ChatMessagesQuery {
    private final MessageRepository messageRepository;

    public ChatMessagesQuery(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Optional<List<UserMessage>> execute(UUID chatId, int lastMessagePosition, int messagesToRetrive) {
        List<UserMessage> retrivedMessages = messageRepository
            .getByAmount(chatId, lastMessagePosition, messagesToRetrive);
        if(retrivedMessages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(retrivedMessages);
    }
}
