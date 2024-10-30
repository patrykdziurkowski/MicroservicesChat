package com.patrykdziurkowski.microserviceschat.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

public interface UserMessageRepository {

    List<UserMessage> getByAmount(UUID chatId, int lastMessageId, int messagesToRetrieve);
    Optional<UserMessage> getById(UUID messageId);
    List<UserMessage> getByOwnerId(UUID messageOwnerId);
    void save(UserMessage message);
    
}
