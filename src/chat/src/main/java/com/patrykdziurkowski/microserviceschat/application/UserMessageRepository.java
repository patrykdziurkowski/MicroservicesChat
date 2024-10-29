package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

public interface UserMessageRepository {

    List<UserMessage> get();
    Optional<List<UserMessage>> getByAmount(UUID chatId, Optional<UUID> lastMessageId, int messagesToRetrive);
    Optional<UserMessage> getById(UUID messageId);
    List<UserMessage> getByOwnerId(UUID messageOwnerId);
    void save(UserMessage message);
    
}
