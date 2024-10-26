package com.patrykdziurkowski.microserviceschat.domain.domainevents;

import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public record MessageDeletedEvent(UUID messageId) implements DomainEvent {
}
