package com.patrykdziurkowski.microserviceschat.application.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

public interface MessageRepository {

    List<UserMessage> getByAmount(UUID chatId, int lastMessageId, int messagesToRetrieve);

    Optional<UserMessage> getById(UUID messageId);

    void save(UserMessage message);

}
