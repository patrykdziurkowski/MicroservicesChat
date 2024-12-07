package com.patrykdziurkowski.microserviceschat.application.commands;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.ChatRepository;
import com.patrykdziurkowski.microserviceschat.application.interfaces.MessageRepository;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

@Service
public class RemoveMessageCommand {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public RemoveMessageCommand(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public boolean execute(UUID currentUserId, UUID messageId) {
        final Optional<UserMessage> retrievedMessage = messageRepository.getById(messageId);
        if (retrievedMessage.isEmpty()) {
            return false;
        }
        UserMessage message = retrievedMessage.get();
        final ChatRoom chat = chatRepository.getById(message.getChatRoomId()).get();
        if (message.delete(currentUserId, chat.getOwnerId()) == false) {
            return false;
        }
        messageRepository.save(message);
        return true;
    }
}
