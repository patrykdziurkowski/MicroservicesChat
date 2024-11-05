package com.patrykdziurkowski.microserviceschat.domain.domainevents;

import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public record MessageDeletedEvent() implements DomainEvent {
}
